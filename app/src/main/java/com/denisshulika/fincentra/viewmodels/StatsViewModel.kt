package com.denisshulika.fincentra.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.models.CategoryStat
import com.denisshulika.fincentra.data.models.CurrencyStats
import com.denisshulika.fincentra.data.models.StatsUiState
import com.denisshulika.fincentra.data.models.Transaction
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.flow.*

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

        val periodTx = if (range != null) {
            transactions.filter { it.timestamp in range }
        } else {
            transactions
        }

        val currencyData = transactions.groupBy { it.currencyCode }.map { (code, allTxForCurrency) ->
            val filteredTx = allTxForCurrency.filter { tx ->
                if (range == null) true else tx.timestamp in range
            }

            val income = filteredTx.filter { !it.isExpense }.sumOf { it.amount }
            val expense = filteredTx.filter { it.isExpense }.sumOf { it.amount }

            val endBalance = accounts
                .filter { it.currencyCode == code && it.selected }
                .sumOf { it.balance }

            val startBalance = endBalance - income + expense

            CurrencyStats(
                currencyCode = code,
                startPeriodBalance = startBalance,
                endPeriodBalance = endBalance,
                totalIncome = income,
                totalExpense = expense,
                categories = groupByCategory(filteredTx, expense)
            )
        }.filter { it.totalIncome > 0 || it.totalExpense > 0 || it.endPeriodBalance > 0 }
            .sortedByDescending { it.currencyCode == 980 }

        StatsUiState(currencyData = currencyData, dateRange = range)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState()
    )

    private fun groupByCategory(txList: List<Transaction>, totalExpense: Double): List<CategoryStat> {
        return txList
            .filter { it.isExpense }
            .groupBy { it.category }
            .map { (cat, list) ->
                val sum = list.sumOf { it.amount }
                CategoryStat(
                    category = cat,
                    amount = sum,
                    percentage = if (totalExpense > 0) (sum / totalExpense).toFloat() else 0f
                )
            }
            .sortedByDescending { it.amount }
    }

    fun selectCurrency(index: Int) { _selectedCurrencyIndex.value = index }
    fun setDateRange(range: LongRange?) { _selectedDateRange.value = range }
}