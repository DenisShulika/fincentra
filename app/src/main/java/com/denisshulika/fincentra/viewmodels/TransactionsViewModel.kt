package com.denisshulika.fincentra.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.models.BankAccount
import com.denisshulika.fincentra.data.models.Transaction
import com.denisshulika.fincentra.data.models.TransactionCategory
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionsViewModel : ViewModel() {
    private val repository = DependencyProvider.repository

    val allTransactions: StateFlow<List<Transaction>> = repository.transactions

    private val accounts = repository.getAccountsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

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

    enum class SortOrder(val displayName: String) {
        DATE_DESC("Спочатку нові"),
        DATE_ASC("Спочатку старі"),
        AMOUNT_DESC("Найдорожчі"),
        AMOUNT_ASC("Найдешевші")
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
    val expenseOptions = listOf("Витрата", "Дохід")

    private val _editingTransactionId = MutableStateFlow<String?>(null)
    val editingTransactionId = _editingTransactionId.asStateFlow()
    private var editingTimestamp: Long? = null

    val transactions: StateFlow<List<Transaction>> = combine(
        allTransactions,
        accounts,
        _searchQuery,
        _selectedBankFilter,
        _selectedCategories,
        _selectedDateRange,
        _selectedTypeFilter,
        _selectedSortOrder
    ) { args ->
        val txList = args[0] as List<Transaction>
        val accountList = args[1] as List<BankAccount>
        val query = args[2] as String
        val bankFilter = args[3] as String
        val selectedCats = args[4] as Set<String>
        val dateRange = args[5] as LongRange?
        val typeFilter = args[6] as String
        val sortOrder = args[7] as SortOrder

        val selectedAccountIds = accountList.filter { it.selected }.map { it.id }

        // 1. Фільтрація
        val filtered = txList.filter { tx ->
            val isAccountVisible = tx.accountId == "manual" || selectedAccountIds.contains(tx.accountId)

            val matchesSearch = tx.description.contains(query, ignoreCase = true) ||
                    tx.category.displayName.contains(query, ignoreCase = true) ||
                    tx.subCategoryName.contains(query, ignoreCase = true)

            val matchesBank = if (bankFilter == "Всі") true else tx.bankName == bankFilter

            val matchesType = when (typeFilter) {
                "Витрати" -> tx.isExpense
                "Доходи" -> !tx.isExpense
                else -> true
            }

            val matchesCategory = if (selectedCats.isEmpty()) {
                true
            } else {
                selectedCats.contains(tx.category.displayName) || selectedCats.contains(tx.subCategoryName)
            }

            val matchesDate = if (dateRange == null) {
                true
            } else {
                tx.timestamp in dateRange
            }

            isAccountVisible && matchesSearch && matchesBank && matchesType && matchesCategory && matchesDate
        }

        // 2. Сортування
        when (sortOrder) {
            SortOrder.DATE_DESC -> filtered.sortedByDescending { it.timestamp }
            SortOrder.DATE_ASC -> filtered.sortedBy { it.timestamp }
            SortOrder.AMOUNT_DESC -> filtered.sortedByDescending { it.amount }
            SortOrder.AMOUNT_ASC -> filtered.sortedBy { it.amount }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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
            _selectedBankFilter.value = "Всі"
            _selectedCategories.value = emptySet()
            _selectedDateRange.value = null
            _selectedTypeFilter.value = "Всі"
            _selectedSortOrder.value = SortOrder.DATE_DESC
        }
    }

    fun toggleCategoryFilter(name: String) {
        val current = _selectedCategories.value.toMutableSet()
        val mainCat = categories.find { it.displayName == name }

        if (mainCat != null) {
            val subs = com.denisshulika.fincentra.data.network.common.MccDirectory.getSubcategoriesFor(mainCat)
            if (current.contains(name)) {
                current.remove(name)
                subs.forEach { current.remove(it) }
            } else {
                current.add(name)
                subs.forEach { current.add(it) }
            }
        } else {
            if (current.contains(name)) {
                current.remove(name)
            } else {
                current.add(name)
            }
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
            repository.deleteTransaction(tx.id)
        }
    }

    fun saveTransaction() {
        val amountDouble = _amount.value.toDoubleOrNull() ?: return
        viewModelScope.launch {
            val transaction = Transaction(
                id = _editingTransactionId.value ?: java.util.UUID.randomUUID().toString(),
                amount = amountDouble,
                description = _description.value,
                bankName = "Готівка",
                category = _category.value,
                isExpense = _isExpense.value,
                timestamp = editingTimestamp ?: System.currentTimeMillis(),
                accountId = "manual",
                currencyCode = 980,
                subCategoryName = "Ручне введення"
            )
            repository.addTransaction(transaction)
            toggleBottomSheet(false)
        }
    }
}