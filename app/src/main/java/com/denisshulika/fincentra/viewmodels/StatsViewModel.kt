package com.denisshulika.fincentra.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.models.*
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class StatsViewModel : ViewModel() {
    private val repository = DependencyProvider.repository

    private val _selectedDateRange = MutableStateFlow<LongRange?>(null)
    val selectedDateRange = _selectedDateRange.asStateFlow()

    private val _selectedCurrencyIndex = MutableStateFlow(0)
    val selectedCurrencyIndex = _selectedCurrencyIndex.asStateFlow()

    val uiState: StateFlow<StatsUiState> = combine(
        repository.transactions,
        repository.getAccountsFlow(),
        _selectedDateRange
    ) { transactions, accounts, range ->
        withContext(Dispatchers.Default) {
            calculateOptimizedStats(transactions, accounts, range)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState()
    )

    private val _selectedPeriod = MutableStateFlow(StatsPeriod.MONTH)
    val selectedPeriod = _selectedPeriod.asStateFlow()

    init {
        setPeriod(StatsPeriod.MONTH)
    }

    private fun calculateOptimizedStats(
        allTx: List<Transaction>,
        accounts: List<BankAccount>,
        range: LongRange?
    ): StatsUiState {
        if (allTx.isEmpty()) return StatsUiState()

        val currencyData = allTx.groupBy { it.currencyCode }.map { (code, transactions) ->
            var periodIncome = 0.0
            var periodExpense = 0.0
            var totalRealSpending = 0.0

            val categoryMap = mutableMapOf<TransactionCategory, Double>()
            val subCategoryMap = mutableMapOf<TransactionCategory, MutableMap<String, Double>>()

            transactions.forEach { tx ->
                val isInRange = range == null || tx.timestamp in range
                if (isInRange) {
                    if (tx.isExpense) periodExpense += tx.amount else periodIncome += tx.amount

                    if (tx.isExpense && tx.category != TransactionCategory.TRANSFERS) {
                        totalRealSpending += tx.amount
                        categoryMap[tx.category] = (categoryMap[tx.category] ?: 0.0) + tx.amount
                        val subs = subCategoryMap.getOrPut(tx.category) { mutableMapOf() }
                        subs[tx.subCategoryName] = (subs[tx.subCategoryName] ?: 0.0) + tx.amount
                    }
                }
            }

            val selectedAccounts = accounts
                .filter { it.currencyCode == code && it.selected }
                .distinctBy { it.id }

            val endBalance = selectedAccounts.sumOf { it.balance }
            val startBalance = endBalance - periodIncome + periodExpense

            val categoryStats = categoryMap.map { (cat, catSum) ->
                val subStats = subCategoryMap[cat]?.map { (subName, subSum) ->
                    SubCategoryStat(subName, subSum, if (catSum > 0) (subSum / catSum).toFloat() else 0f)
                }?.sortedByDescending { it.amount } ?: emptyList()

                CategoryStat(
                    category = cat,
                    amount = catSum,
                    percentage = if (totalRealSpending > 0) (catSum / totalRealSpending).toFloat() else 0f,
                    subCategories = subStats
                )
            }.sortedByDescending { it.amount }

            CurrencyStats(
                currencyCode = code,
                startPeriodBalance = startBalance,
                endPeriodBalance = endBalance,
                totalIncome = periodIncome,
                totalExpense = totalRealSpending,
                categories = categoryStats
            )
        }.filter { it.totalIncome > 0 || it.totalExpense > 0 || it.endPeriodBalance > 0 }
            .sortedByDescending { it.currencyCode == 980 }

        return StatsUiState(currencyData = currencyData, dateRange = range)
    }

    fun setPeriod(period: StatsPeriod) {
        _selectedPeriod.value = period
        if (period == StatsPeriod.CUSTOM) return

        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = now

        val range = when (period) {
            StatsPeriod.WEEK -> {
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -7)
                calendar.timeInMillis..now
            }
            StatsPeriod.MONTH -> {
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.timeInMillis..now
            }
            StatsPeriod.QUARTER -> {
                calendar.add(java.util.Calendar.MONTH, -3)
                calendar.timeInMillis..now
            }
            StatsPeriod.ALL -> null
            StatsPeriod.CUSTOM -> _selectedDateRange.value
        }
        _selectedDateRange.value = range
    }

    fun setCustomDateRange(range: LongRange?) {
        _selectedPeriod.value = StatsPeriod.CUSTOM
        _selectedDateRange.value = range
    }

    fun selectCurrency(index: Int) { _selectedCurrencyIndex.value = index }
    fun setDateRange(range: LongRange?) { _selectedDateRange.value = range }
}