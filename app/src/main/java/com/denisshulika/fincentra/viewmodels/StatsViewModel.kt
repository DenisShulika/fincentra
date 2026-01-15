package com.denisshulika.fincentra.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.models.domain.BankAccount
import com.denisshulika.fincentra.data.models.domain.Transaction
import com.denisshulika.fincentra.data.models.domain.TransactionCategory
import com.denisshulika.fincentra.data.models.state.CategoryStat
import com.denisshulika.fincentra.data.models.state.CurrencyStats
import com.denisshulika.fincentra.data.models.state.StatsPeriod
import com.denisshulika.fincentra.data.models.state.StatsUiState
import com.denisshulika.fincentra.data.models.state.SubCategoryStat
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.round

class StatsViewModel : ViewModel() {
    private val repository = DependencyProvider.financeRepository

    private val _selectedDateRange = MutableStateFlow<LongRange?>(null)
    val selectedDateRange = _selectedDateRange.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(StatsPeriod.MONTH)
    val selectedPeriod = _selectedPeriod.asStateFlow()

    private val _selectedBank = MutableStateFlow("Всі")
    val selectedBank = _selectedBank.asStateFlow()

    private val _selectedAccountId = MutableStateFlow<String?>(null)
    val selectedAccountId = _selectedAccountId.asStateFlow()

    private val _isExpenseMode = MutableStateFlow(true)
    val isExpenseMode = _isExpenseMode.asStateFlow()

    private val _selectedCurrencyIndex = MutableStateFlow(0)
    val selectedCurrencyIndex = _selectedCurrencyIndex.asStateFlow()

    val availableAccounts = repository.getAccountsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val uiState: StateFlow<StatsUiState> = combine(
        repository.transactions,
        availableAccounts,
        _selectedDateRange,
        _selectedBank,
        _selectedAccountId,
        _isExpenseMode
    ) { args ->
        val transactions = args[0] as List<Transaction>
        val accounts = args[1] as List<BankAccount>
        val range = args[2] as LongRange?
        val bank = args[3] as String
        val accId = args[4] as String?
        val isExpMode = args[5] as Boolean

        withContext(Dispatchers.Default) {
            calculateOptimizedStats(transactions, accounts, range, bank, accId, isExpMode)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState()
    )

    init {
        setPeriod(StatsPeriod.MONTH)
    }

    private fun calculateOptimizedStats(
        allTx: List<Transaction>,
        accounts: List<BankAccount>,
        range: LongRange?,
        bankFilter: String,
        accountIdFilter: String?,
        isExpenseMode: Boolean
    ): StatsUiState {
        if (allTx.isEmpty()) return StatsUiState()

        val currencyData = allTx.groupBy { it.currencyCode }.map { (code, transactions) ->
            var periodIncome = 0.0
            var periodExpense = 0.0
            val categoryMap = mutableMapOf<TransactionCategory, Double>()
            val subCategoryMap = mutableMapOf<TransactionCategory, MutableMap<String, Double>>()

            transactions.forEach { tx ->
                val isInRange = range == null || tx.timestamp in range
                val matchesBank = if (bankFilter == "Всі") true else tx.bankName == bankFilter
                val matchesAccount =
                    if (accountIdFilter == null) true else tx.accountId == accountIdFilter

                if (isInRange && matchesBank && matchesAccount) {
                    if (tx.isExpense) periodExpense += tx.amount else periodIncome += tx.amount

                    if (tx.isExpense == isExpenseMode) {
                        categoryMap[tx.category] = (categoryMap[tx.category] ?: 0.0) + tx.amount
                        val subs = subCategoryMap.getOrPut(tx.category) { mutableMapOf() }
                        subs[tx.subCategoryName] = (subs[tx.subCategoryName] ?: 0.0) + tx.amount
                    }
                }
            }

            periodIncome = periodIncome.round(2)
            periodExpense = periodExpense.round(2)

            val selectedAccounts = accounts
                .filter { it.currencyCode == code && it.selected }
                .filter { if (bankFilter == "Всі") true else it.provider == bankFilter }
                .filter { if (accountIdFilter == null) true else it.id == accountIdFilter }
                .distinctBy { it.id }

            val endBalance = selectedAccounts.sumOf { it.balance }.round(2)
            val startBalance = (endBalance - periodIncome + periodExpense).round(2)

            val totalForPercentage = if (isExpenseMode) periodExpense else periodIncome

            val categoryStats = categoryMap.map { (cat, catSum) ->
                val subStats = subCategoryMap[cat]?.map { (subName, subSum) ->
                    SubCategoryStat(
                        name = subName,
                        amount = subSum.round(2),
                        percentageOfParent = if (catSum > 0) (subSum / catSum).toFloat() else 0f
                    )
                }?.sortedByDescending { it.amount } ?: emptyList()

                CategoryStat(
                    category = cat,
                    amount = catSum.round(2),
                    percentage = if (totalForPercentage > 0) (catSum / totalForPercentage).toFloat() else 0f,
                    subCategories = subStats
                )
            }.sortedByDescending { it.amount }

            CurrencyStats(
                code,
                startBalance,
                endBalance,
                periodIncome,
                periodExpense,
                categoryStats
            )
        }.filter { it.totalIncome > 0 || it.totalExpense > 0 || it.endPeriodBalance > 0 }
            .sortedByDescending { it.currencyCode == 980 }

        return StatsUiState(currencyData = currencyData, dateRange = range)
    }

    fun setPeriod(period: StatsPeriod) {
        _selectedPeriod.value = period
        if (period == StatsPeriod.CUSTOM) return

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endTs = calendar.timeInMillis

        val range = when (period) {
            StatsPeriod.WEEK -> {
                calendar.add(Calendar.DAY_OF_YEAR, -6)
                calendar.setToStartOfDay()
                calendar.timeInMillis..endTs
            }

            StatsPeriod.MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.setToStartOfDay()
                calendar.timeInMillis..endTs
            }

            StatsPeriod.QUARTER -> {
                calendar.add(Calendar.MONTH, -3)
                calendar.setToStartOfDay()
                calendar.timeInMillis..endTs
            }

            StatsPeriod.ALL -> null
            else -> null
        }
        _selectedDateRange.value = range
    }

    fun setCustomDateRange(range: LongRange?) {
        if (range == null) {
            _selectedDateRange.value = null
            return
        }
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = range.first
        calendar.setToStartOfDay()
        val start = calendar.timeInMillis

        calendar.timeInMillis = range.last
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis

        _selectedPeriod.value = StatsPeriod.CUSTOM
        _selectedDateRange.value = start..end
    }

    private fun Calendar.setToStartOfDay() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
    }

    fun selectCurrency(index: Int) {
        _selectedCurrencyIndex.value = index
    }

    fun toggleMode(isExpense: Boolean) {
        _isExpenseMode.value = isExpense
    }

    fun onBankFilterChange(bank: String) {
        _selectedBank.value = bank
        _selectedAccountId.value = null
    }

    fun onAccountFilterChange(id: String?) {
        _selectedAccountId.value = id
    }
}