package com.denisshulika.fincentra.viewmodels

import android.util.Log
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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.round

class StatsViewModel : ViewModel() {
    private val repository = DependencyProvider.repository

    private val _selectedDateRange = MutableStateFlow<LongRange?>(null)
    val selectedDateRange = _selectedDateRange.asStateFlow()

    private val _selectedCurrencyIndex = MutableStateFlow(0)
    val selectedCurrencyIndex = _selectedCurrencyIndex.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(StatsPeriod.MONTH)
    val selectedPeriod = _selectedPeriod.asStateFlow()

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

            periodIncome = periodIncome.round(2)
            periodExpense = periodExpense.round(2)

            val selectedAccounts = accounts
                .filter { it.currencyCode == code && it.selected }
                .distinctBy { it.id }

            val endBalance = selectedAccounts.sumOf { it.balance }.round(2)

            val startBalance = (endBalance - periodIncome + periodExpense).round(2)

            Log.d("STATS_CHECK", """
                Валюта: $code | Період: ${if (range == null) "Все" else "Custom"}
                Баланс End: $endBalance | Start: $startBalance
                Доходи: $periodIncome | Витрати: $periodExpense
            """.trimIndent())

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
                    percentage = if (periodExpense > 0) (catSum / periodExpense).toFloat() else 0f,
                    subCategories = subStats
                )
            }.sortedByDescending { it.amount }

            CurrencyStats(
                currencyCode = code,
                startPeriodBalance = startBalance,
                endPeriodBalance = endBalance,
                totalIncome = periodIncome,
                totalExpense = periodExpense,
                categories = categoryStats
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

    fun selectCurrency(index: Int) { _selectedCurrencyIndex.value = index }
}