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

class StatsViewModel : ViewModel() {
    private val financeRepository = DependencyProvider.financeRepository
    private val settingsRepository = DependencyProvider.settingsRepository

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

    val availableAccounts = financeRepository.accounts
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val uiState: StateFlow<StatsUiState> = combine(
        financeRepository.transactions,
        availableAccounts,
        settingsRepository.getSelectedAccountIdsFlow(),
        _selectedDateRange,
        _selectedBank,
        _selectedAccountId,
        _isExpenseMode
    ) { args ->
        val txs = args[0] as List<Transaction>
        val accs = args[1] as List<BankAccount>
        val activeIds = args[2] as List<String>
        val range = args[3] as LongRange?
        val bank = args[4] as String
        val accId = args[5] as String?
        val mode = args[6] as Boolean

        withContext(Dispatchers.Default) {
            calculateOptimizedStats(txs, accs, range, bank, accId, mode, activeIds)
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
        isExpenseMode: Boolean,
        activeIds: List<String>
    ): StatsUiState {
        if (accounts.isEmpty()) return StatsUiState()

        val baseAccounts = if (activeIds.isNotEmpty()) {
            accounts.filter { activeIds.contains(it.id) }
        } else {
            accounts.filter { it.selected }
        }

        if (baseAccounts.isEmpty()) return StatsUiState()

        val filteredAccounts = baseAccounts.filter { acc ->
            (bankFilter == com.denisshulika.fincentra.data.util.FilterConstants.ALL || acc.provider == bankFilter) &&
                    (accountIdFilter == null || acc.id == accountIdFilter)
        }

        val currencyData =
            filteredAccounts.groupBy { it.currencyCode }.map { (code, accsInCurrency) ->
                val txsInPeriod = allTx.filter { tx ->
                    tx.currencyCode == code &&
                            accsInCurrency.any { it.id == tx.accountId } &&
                            (range == null || tx.timestamp in range)
                }

                val endBalance = accsInCurrency.sumOf { it.balance }
                var calculatedStartBalance = 0.0

                accsInCurrency.forEach { acc ->
                    val oldestTx =
                        txsInPeriod.filter { it.accountId == acc.id }.minByOrNull { it.timestamp }
                    if (oldestTx != null && oldestTx.balance != null) {
                        calculatedStartBalance += if (oldestTx.isExpense) oldestTx.balance + oldestTx.amount
                        else oldestTx.balance - oldestTx.amount
                    } else {
                        val periodInc =
                            txsInPeriod.filter { it.accountId == acc.id && !it.isExpense }
                                .sumOf { it.amount }
                        val periodExp =
                            txsInPeriod.filter { it.accountId == acc.id && it.isExpense }
                                .sumOf { it.amount }
                        calculatedStartBalance += (acc.balance - periodInc + periodExp)
                    }
                }

                val periodIncome = txsInPeriod.filter { !it.isExpense }.sumOf { it.amount }
                val periodExpense = txsInPeriod.filter { it.isExpense }.sumOf { it.amount }

                val categoryMap = mutableMapOf<TransactionCategory, Double>()

                val subCategoryMap = mutableMapOf<TransactionCategory, MutableMap<Int, Double>>()

                txsInPeriod.filter { it.isExpense == isExpenseMode }.forEach { tx ->
                    categoryMap[tx.category] = (categoryMap[tx.category] ?: 0.0) + tx.amount
                    val subs = subCategoryMap.getOrPut(tx.category) { mutableMapOf() }

                    val subRes = tx.subCategoryRes
                    subs[subRes] = (subs[subRes] ?: 0.0) + tx.amount
                }

                val totalForPercentage = if (isExpenseMode) periodExpense else periodIncome
                val categoryStats = categoryMap.map { (cat, catSum) ->
                    val subStats = subCategoryMap[cat]?.map { (subRes, subSum) ->
                        SubCategoryStat(
                            nameRes = subRes,
                            amount = subSum.round(2),
                            percentageOfParent = if (catSum > 0) (subSum / catSum).toFloat() else 0f
                        )
                    }?.sortedByDescending { it.amount } ?: emptyList()

                    CategoryStat(
                        cat,
                        catSum.round(2),
                        if (totalForPercentage > 0) (catSum / totalForPercentage).toFloat() else 0f,
                        subStats
                    )
                }.sortedByDescending { it.amount }

                CurrencyStats(
                    code,
                    calculatedStartBalance.round(2),
                    endBalance.round(2),
                    periodIncome.round(2),
                    periodExpense.round(2),
                    categoryStats
                )
            }.sortedByDescending { it.currencyCode == 980 }

        return StatsUiState(currencyData, range)
    }

    fun setPeriod(period: StatsPeriod) {
        _selectedPeriod.value = period
        if (period == StatsPeriod.CUSTOM) return
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59); calendar.set(
            Calendar.SECOND,
            59
        ); calendar.set(Calendar.MILLISECOND, 999)
        val endTs = calendar.timeInMillis
        val range = when (period) {
            StatsPeriod.WEEK -> {
                calendar.add(
                    Calendar.DAY_OF_YEAR,
                    -6
                ); calendar.setToStartOfDay(); calendar.timeInMillis..endTs
            }

            StatsPeriod.MONTH -> {
                calendar.set(
                    Calendar.DAY_OF_MONTH,
                    1
                ); calendar.setToStartOfDay(); calendar.timeInMillis..endTs
            }

            StatsPeriod.QUARTER -> {
                calendar.add(
                    Calendar.MONTH,
                    -3
                ); calendar.setToStartOfDay(); calendar.timeInMillis..endTs
            }

            StatsPeriod.ALL -> null
            else -> null
        }
        _selectedDateRange.value = range
    }

    fun setCustomDateRange(range: LongRange?) {
        if (range == null) return
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = range.first; calendar.setToStartOfDay()
        val start = calendar.timeInMillis
        calendar.timeInMillis = range.last; calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(
            Calendar.MINUTE,
            59
        ); calendar.set(Calendar.SECOND, 59); calendar.set(Calendar.MILLISECOND, 999)
        _selectedPeriod.value = StatsPeriod.CUSTOM
        _selectedDateRange.value = start..calendar.timeInMillis
    }

    private fun Calendar.setToStartOfDay() {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(
            Calendar.SECOND,
            0
        ); set(Calendar.MILLISECOND, 0)
    }

    private fun Double.round(decimals: Int): Double {
        var multiplier =
            1.0; repeat(decimals) { multiplier *= 10 }; return kotlin.math.round(this * multiplier) / multiplier
    }

    fun selectCurrency(index: Int) {
        _selectedCurrencyIndex.value = index
    }

    fun toggleMode(isExp: Boolean) {
        _isExpenseMode.value = isExp
    }

    fun onBankFilterChange(bank: String) {
        _selectedBank.value = bank; _selectedAccountId.value = null
    }

    fun onAccountFilterChange(id: String?) {
        _selectedAccountId.value = id
    }
}