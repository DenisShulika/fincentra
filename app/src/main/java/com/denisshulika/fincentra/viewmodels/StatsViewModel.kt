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

    private fun calculateOptimizedStats(
        allTx: List<Transaction>,
        accounts: List<BankAccount>,
        range: LongRange?
    ): StatsUiState {
        if (allTx.isEmpty()) return StatsUiState()

        val currencyData = allTx.groupBy { it.currencyCode }.map { (code, transactions) ->
            var periodIncome = 0.0
            var periodExpense = 0.0
            val categoryMap = mutableMapOf<TransactionCategory, Double>()
            val subCategoryMap = mutableMapOf<TransactionCategory, MutableMap<String, Double>>()

            transactions.forEach { tx ->
                val isInRange = range == null || tx.timestamp in range
                if (isInRange) {
                    if (tx.isExpense) {
                        periodExpense += tx.amount
                        categoryMap[tx.category] = (categoryMap[tx.category] ?: 0.0) + tx.amount
                        val subs = subCategoryMap.getOrPut(tx.category) { mutableMapOf() }
                        subs[tx.subCategoryName] = (subs[tx.subCategoryName] ?: 0.0) + tx.amount
                    } else {
                        periodIncome += tx.amount
                    }
                }
            }

            val selectedAccounts = accounts
                .filter { it.currencyCode == code && it.selected }
                .distinctBy { it.id }

            val endBalance = selectedAccounts.sumOf { it.balance }
            val startBalance = endBalance - periodIncome + periodExpense

            Log.d("STATS_CHECK", """
            Валюта: $code
            К-сть вибраних карт: ${selectedAccounts.size}
            Поточний баланс (End): $endBalance
            Доходи за період: $periodIncome
            Витрати за період: $periodExpense
            Розрахований старт: $startBalance
        """.trimIndent())

            val categoryStats = categoryMap.map { (cat, catSum) ->
                val subStats = subCategoryMap[cat]?.map { (subName, subSum) ->
                    SubCategoryStat(subName, subSum, if (catSum > 0) (subSum / catSum).toFloat() else 0f)
                }?.sortedByDescending { it.amount } ?: emptyList()

                CategoryStat(cat, catSum, if (periodExpense > 0) (catSum / periodExpense).toFloat() else 0f, subStats)
            }.sortedByDescending { it.amount }

            CurrencyStats(code, startBalance, endBalance, periodIncome, periodExpense, categoryStats)
        }.filter { it.totalIncome > 0 || it.totalExpense > 0 || it.endPeriodBalance > 0 }
            .sortedByDescending { it.currencyCode == 980 }

        return StatsUiState(currencyData = currencyData, dateRange = range)
    }

    fun selectCurrency(index: Int) { _selectedCurrencyIndex.value = index }
    fun setDateRange(range: LongRange?) { _selectedDateRange.value = range }
}