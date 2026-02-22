package com.denisshulika.fincentra.viewmodels

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.models.domain.BankAccount
import com.denisshulika.fincentra.data.models.domain.Transaction
import com.denisshulika.fincentra.data.models.domain.TransactionCategory
import com.denisshulika.fincentra.data.network.common.MccDirectory
import com.denisshulika.fincentra.data.util.DateFormatter
import com.denisshulika.fincentra.data.util.FilterConstants
import com.denisshulika.fincentra.data.util.TransactionConstants
import com.denisshulika.fincentra.data.util.TransactionFilterEngine
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

class TransactionsViewModel : ViewModel() {
    private val financeRepository = DependencyProvider.financeRepository
    private val settingsRepository = DependencyProvider.settingsRepository

    private val allTransactions: StateFlow<List<Transaction>> = financeRepository.transactions
    private val accounts = financeRepository.getAccountsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive = _isSearchActive.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow("Всі")
    val selectedTypeFilter = _selectedTypeFilter.asStateFlow()

    private val _selectedBankFilter = MutableStateFlow("Всі")
    val selectedBankFilter = _selectedBankFilter.asStateFlow()

    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategories = _selectedCategories.asStateFlow()

    private val _selectedDateRange = MutableStateFlow<LongRange?>(null)
    val selectedDateRange = _selectedDateRange.asStateFlow()

    private val _selectedCurrency = MutableStateFlow(980)
    val selectedCurrency = _selectedCurrency.asStateFlow()

    private val _selectedIds = MutableStateFlow<List<String>>(emptyList())

    init {
        viewModelScope.launch {
            settingsRepository.getSelectedAccountIdsFlow().collect { ids ->
                _selectedIds.value = ids
            }
        }
    }

    fun onCurrencyChange(code: Int) {
        _selectedCurrency.value = code
    }

    fun saveTransaction() {
        val amt = _amount.value.toDoubleOrNull() ?: return
        viewModelScope.launch {
            val tx = Transaction(
                id = _editingTransactionId.value ?: UUID.randomUUID().toString(),
                amount = amt, description = _description.value,
                bankName = TransactionConstants.SOURCE_CASH,
                category = _category.value, isExpense = _isExpense.value,
                timestamp = editingTimestamp ?: System.currentTimeMillis(),
                accountId = TransactionConstants.ACCOUNT_ID_MANUAL,
                currencyCode = _selectedCurrency.value,
                subCategoryRes = R.string.mcc_others
            )
            financeRepository.addTransaction(tx)
            toggleBottomSheet(false)
        }
    }

    enum class SortOrder(@StringRes val displayNameRes: Int) {
        DATE_DESC(R.string.sort_date_desc),
        DATE_ASC(R.string.sort_date_asc),
        AMOUNT_DESC(R.string.sort_amount_desc),
        AMOUNT_ASC(R.string.sort_amount_asc)
    }

    private val _selectedSortOrder = MutableStateFlow(SortOrder.DATE_DESC)
    val selectedSortOrder = _selectedSortOrder.asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount = _amount.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet = _showBottomSheet.asStateFlow()

    private val _isExpense = MutableStateFlow(true)
    val isExpense = _isExpense.asStateFlow()

    private val _category = MutableStateFlow(TransactionCategory.OTHERS)
    val category = _category.asStateFlow()

    val categories = TransactionCategory.entries
    val expenseOptions = listOf(R.string.tx_type_expense, R.string.tx_type_income)

    private val _editingTransactionId = MutableStateFlow<String?>(null)
    val editingTransactionId = _editingTransactionId.asStateFlow()
    private var editingTimestamp: Long? = null

    val transactions: StateFlow<List<Transaction>> = combine(
        allTransactions,
        accounts,
        _searchQuery,
        _selectedCategories,
        _selectedBankFilter,
        _selectedTypeFilter,
        _selectedIds,
        _selectedSortOrder
    ) { args ->
        val txList = args[0] as List<Transaction>
        val accountList = args[1] as List<BankAccount>
        val query = args[2] as String
        val selectedCats = args[3] as Set<String>
        val bankFilter = args[4] as String
        val typeFilter = args[5] as String
        val activeIds = args[6] as List<String>
        val sortOrder = args[7] as SortOrder

        TransactionFilterEngine.filter(
            transactions = txList,
            selectedAccountIds = activeIds,
            query = query,
            bankFilter = bankFilter,
            typeFilter = typeFilter,
            selectedCats = selectedCats,
            dateRange = null
        ).let { filtered ->
            when (sortOrder) {
                SortOrder.DATE_DESC -> filtered.sortedByDescending { it.timestamp }
                SortOrder.DATE_ASC -> filtered.sortedBy { it.timestamp }
                SortOrder.AMOUNT_DESC -> filtered.sortedByDescending { it.amount }
                SortOrder.AMOUNT_ASC -> filtered.sortedBy { it.amount }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groupedTransactions: StateFlow<Map<String, List<Transaction>>> = transactions
        .map { list ->
            list.groupBy { tx ->
                val calendar = Calendar.getInstance()
                val now = Calendar.getInstance()
                calendar.timeInMillis = tx.timestamp

                val isToday = calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                        calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)

                val yesterday = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
                val isYesterday = calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                        calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)

                when {
                    isToday -> "DATE_TODAY"
                    isYesterday -> "DATE_YESTERDAY"
                    else -> DateFormatter.formatDayMonth(tx.timestamp)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val categoriesWithSubs: StateFlow<Map<TransactionCategory, List<Int>>> = allTransactions
        .map { _ ->
            TransactionCategory.entries.associateWith { mainCat ->
                MccDirectory.getSubcategoriesFor(mainCat)
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    private val _paginatedTransactions = MutableStateFlow<List<Transaction>>(emptyList())

    private val _viewingTransaction = MutableStateFlow<Transaction?>(null)
    val viewingTransaction = _viewingTransaction.asStateFlow()

    fun showTransactionDetails(tx: Transaction) {
        _viewingTransaction.value = tx
    }

    fun closeTransactionDetails() {
        _viewingTransaction.value = null
    }

    fun onSortOrderChange(order: SortOrder) {
        _selectedSortOrder.value = order
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onBankFilterChange(bank: String) {
        _selectedBankFilter.value = bank
    }

    fun onTypeFilterChange(type: String) {
        _selectedTypeFilter.value = type
    }

    fun setDateRange(range: LongRange?) {
        _selectedDateRange.value = range
    }

    fun toggleSearch(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = ""
            _selectedBankFilter.value = FilterConstants.ALL
            _selectedTypeFilter.value = FilterConstants.ALL
            _selectedCategories.value = emptySet()
            _selectedDateRange.value = null
            _selectedSortOrder.value = SortOrder.DATE_DESC
        }
    }

    fun toggleCategoryFilter(name: String) {
        val current = _selectedCategories.value.toMutableSet()
        if (current.contains(name)) {
            current.remove(name)
        } else {
            current.add(name)
        }
        _selectedCategories.value = current
    }

    fun onAmountChange(newAmount: String) {
        val standardized = newAmount.replace(',', '.')
        _amount.value = standardized.filterIndexed { index, char ->
            char.isDigit() || (char == '.' && standardized.indexOf('.') == index)
        }
    }

    fun onDescriptionChange(newDesc: String) {
        _description.value = newDesc
    }

    fun onTypeChange(isExp: Boolean) {
        _isExpense.value = isExp
    }

    fun onCategoryChange(newCat: TransactionCategory) {
        _category.value = newCat
    }

    fun toggleBottomSheet(show: Boolean) {
        _showBottomSheet.value = show
        if (!show) {
            _amount.value = ""
            _description.value = ""
            _isExpense.value = true
            _category.value = TransactionCategory.OTHERS
            _editingTransactionId.value = null
            editingTimestamp = null
            _selectedCurrency.value = 980
        }
    }

    fun prepareForEdit(tx: Transaction) {
        _amount.value = tx.amount.toString()
        _description.value = tx.description
        _isExpense.value = tx.isExpense
        _category.value = tx.category
        _editingTransactionId.value = tx.id
        editingTimestamp = tx.timestamp
        _showBottomSheet.value = true
    }

    fun deleteTransaction(tx: Transaction) {
        viewModelScope.launch {
            financeRepository.deleteTransaction(tx.id)
        }
    }
}