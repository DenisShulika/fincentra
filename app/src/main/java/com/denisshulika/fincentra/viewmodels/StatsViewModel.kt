package com.denisshulika.fincentra.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.models.domain.BankAccount
import com.denisshulika.fincentra.data.models.domain.Budget
import com.denisshulika.fincentra.data.models.domain.BudgetProgress
import com.denisshulika.fincentra.data.models.domain.Dream
import com.denisshulika.fincentra.data.models.domain.Transaction
import com.denisshulika.fincentra.data.models.domain.TransactionCategory
import com.denisshulika.fincentra.data.models.state.CategoryStat
import com.denisshulika.fincentra.data.models.state.CurrencyStats
import com.denisshulika.fincentra.data.models.state.StatsPeriod
import com.denisshulika.fincentra.data.models.state.StatsUiState
import com.denisshulika.fincentra.data.models.state.SubCategoryStat
import com.denisshulika.fincentra.data.util.FilterConstants
import com.denisshulika.fincentra.data.util.TransactionConstants
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

    private val _selectedBank = MutableStateFlow(FilterConstants.ALL)
    val selectedBank = _selectedBank.asStateFlow()

    private val _selectedAccountId = MutableStateFlow<String?>(null)
    val selectedAccountId = _selectedAccountId.asStateFlow()

    private val _isExpenseMode = MutableStateFlow(true)
    val isExpenseMode = _isExpenseMode.asStateFlow()

    private val _selectedCurrencyIndex = MutableStateFlow(0)
    val selectedCurrencyIndex = _selectedCurrencyIndex.asStateFlow()

    val availableAccounts = financeRepository.accounts
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val currencyRepository = DependencyProvider.currencyRepository

    private val _healthScore = MutableStateFlow(100)
    val healthScore = _healthScore.asStateFlow()

    val uiState: StateFlow<StatsUiState> = combine(
        financeRepository.transactions,
        availableAccounts,
        settingsRepository.getSelectedAccountIdsFlow(),
        settingsRepository.getDisplayCurrencyFlow(),
        financeRepository.dream,
        financeRepository.budgets,
        _selectedDateRange,
        _selectedBank,
        _selectedAccountId,
        _isExpenseMode
    ) { args ->
        val txs = args[0] as List<Transaction>
        val accs = args[1] as List<BankAccount>
        val activeIds = args[2] as List<String>
        val displayCurrency = args[3] as Int
        val dream = args[4] as Dream?
        val budgetsFromFlow = args[5] as List<Budget>
        val range = args[6] as LongRange?
        val bank = args[7] as String
        val accId = args[8] as String?
        val mode = args[9] as Boolean

        withContext(Dispatchers.Default) {
            val rates = DependencyProvider.currencyRepository.getRates()
            val baseState = calculateOptimizedStats(txs, accs, range, bank, accId, mode, activeIds)

            if (baseState.currencyData.isNotEmpty()) {
                val totalCard =
                    calculateTotalStats(baseState.currencyData, displayCurrency, rates, mode)

                val currentBudgets = budgetsFromFlow.map { budget ->
                    val spent = txs.filter {
                        it.category.name == budget.categoryName &&
                                it.isExpense &&
                                it.currencyCode == budget.currencyCode
                    }.sumOf { it.amount }

                    BudgetProgress(
                        budget,
                        spent,
                        (budget.limitAmount - spent).coerceAtLeast(0.0),
                        if (budget.limitAmount > 0) (spent / budget.limitAmount).toFloat() else 0f
                    )
                }

                val actualDreamProgress = if (dream != null) {
                    val trackedBankBalance = accs.filter { activeIds.contains(it.id) }
                        .sumOf {
                            DependencyProvider.currencyRepository.convert(
                                it.balance,
                                it.currencyCode,
                                dream.currencyCode,
                                rates
                            ) ?: 0.0
                        }

                    val trackedCashBalance =
                        txs.filter { it.accountId == TransactionConstants.ACCOUNT_ID_MANUAL }
                            .groupBy { it.currencyCode }.map { (code, mtxs) ->
                                val sum = mtxs.sumOf { if (it.isExpense) -it.amount else it.amount }
                                DependencyProvider.currencyRepository.convert(
                                    sum,
                                    code,
                                    dream.currencyCode,
                                    rates
                                ) ?: 0.0
                            }.sum()

                    val available =
                        (trackedBankBalance + trackedCashBalance - dream.safetyBuffer).coerceAtLeast(
                            0.0
                        )
                    if (dream.targetAmount > 0) (available / dream.targetAmount).toFloat()
                        .coerceIn(0f, 1f) else 0f
                } else 0f

                _healthScore.value =
                    if (totalCard.totalIncome == 0.0 && totalCard.totalExpense == 0.0) 100
                    else calculateFinancialHealth(
                        totalCard.totalIncome,
                        totalCard.totalExpense,
                        currentBudgets,
                        actualDreamProgress
                    )

                StatsUiState(listOf(totalCard) + baseState.currencyData, range)
            } else {
                _healthScore.value = 100
                baseState
            }
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

        val baseAccounts = accounts.filter { activeIds.contains(it.id) }

        if (baseAccounts.isEmpty()) return StatsUiState()

        val filteredAccounts = baseAccounts.filter { acc ->
            (bankFilter == FilterConstants.ALL || acc.provider == bankFilter) &&
                    (accountIdFilter == null || acc.id == accountIdFilter)
        }

        val accountCurrencies = filteredAccounts.map { it.currencyCode }.toSet()
        val manualCurrencies = allTx
            .filter { it.accountId == com.denisshulika.fincentra.data.util.TransactionConstants.ACCOUNT_ID_MANUAL }
            .map { it.currencyCode }
            .toSet()

        val allRelevantCurrencies = (accountCurrencies + manualCurrencies).toList()

        val currencyData = allRelevantCurrencies.map { code ->
            val txsInPeriod = allTx.filter { tx ->
                tx.currencyCode == code && (
                        tx.accountId == com.denisshulika.fincentra.data.util.TransactionConstants.ACCOUNT_ID_MANUAL ||
                                filteredAccounts.any { it.id == tx.accountId }
                        ) && (range == null || tx.timestamp in range)
            }

            val bankEndBalance =
                filteredAccounts.filter { it.currencyCode == code }.sumOf { it.balance }
            val manualEndBalance = allTx
                .filter { it.accountId == com.denisshulika.fincentra.data.util.TransactionConstants.ACCOUNT_ID_MANUAL && it.currencyCode == code }
                .sumOf { if (it.isExpense) -it.amount else it.amount }

            val endBalance = bankEndBalance + manualEndBalance

            var calculatedStartBalance = 0.0

            filteredAccounts.filter { it.currencyCode == code }.forEach { acc ->
                val oldestTx =
                    txsInPeriod.filter { it.accountId == acc.id }.minByOrNull { it.timestamp }
                if (oldestTx?.balance != null) {
                    calculatedStartBalance += if (oldestTx.isExpense) oldestTx.balance + oldestTx.amount
                    else oldestTx.balance - oldestTx.amount
                } else {
                    val periodInc = txsInPeriod.filter { it.accountId == acc.id && !it.isExpense }
                        .sumOf { it.amount }
                    val periodExp = txsInPeriod.filter { it.accountId == acc.id && it.isExpense }
                        .sumOf { it.amount }
                    calculatedStartBalance += (acc.balance - periodInc + periodExp)
                }
            }

            if (manualCurrencies.contains(code)) {
                val periodInc =
                    txsInPeriod.filter { it.accountId == com.denisshulika.fincentra.data.util.TransactionConstants.ACCOUNT_ID_MANUAL && !it.isExpense }
                        .sumOf { it.amount }
                val periodExp =
                    txsInPeriod.filter { it.accountId == com.denisshulika.fincentra.data.util.TransactionConstants.ACCOUNT_ID_MANUAL && it.isExpense }
                        .sumOf { it.amount }
                calculatedStartBalance += (manualEndBalance - periodInc + periodExp)
            }

            val periodIncome = txsInPeriod.filter { !it.isExpense }.sumOf { it.amount }
            val periodExpense = txsInPeriod.filter { it.isExpense }.sumOf { it.amount }

            val categoryMap = mutableMapOf<TransactionCategory, Double>()
            val subCategoryMap = mutableMapOf<TransactionCategory, MutableMap<Int, Double>>()

            txsInPeriod.filter { it.isExpense == isExpenseMode }.forEach { tx ->
                categoryMap[tx.category] = (categoryMap[tx.category] ?: 0.0) + tx.amount
                val subs = subCategoryMap.getOrPut(tx.category) { mutableMapOf() }
                subs[tx.subCategoryRes] = (subs[tx.subCategoryRes] ?: 0.0) + tx.amount
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

    private fun calculateTotalStats(
        data: List<CurrencyStats>,
        target: Int,
        rates: Map<Int, Double>,
        isExpenseMode: Boolean
    ): CurrencyStats {

        fun safeSum(selector: (CurrencyStats) -> Double): Double {
            return data.sumOf { item ->
                DependencyProvider.currencyRepository.convert(
                    selector(item),
                    item.currencyCode,
                    target,
                    rates
                ) ?: 0.0
            }
        }

        val categoryMap = mutableMapOf<TransactionCategory, Double>()

        data.forEach { currencyStat ->
            currencyStat.categories.forEach { catStat ->
                val convertedAmount = DependencyProvider.currencyRepository.convert(
                    catStat.amount, currencyStat.currencyCode, target, rates
                ) ?: 0.0
                categoryMap[catStat.category] =
                    (categoryMap[catStat.category] ?: 0.0) + convertedAmount
            }
        }

        val totalForPercentage =
            if (isExpenseMode) safeSum { it.totalExpense } else safeSum { it.totalIncome }

        val aggregatedCategories = categoryMap.map { (cat, amount) ->
            CategoryStat(
                category = cat,
                amount = amount.round(2),
                percentage = if (totalForPercentage > 0) (amount / totalForPercentage).toFloat() else 0f,
                subCategories = emptyList()
            )
        }.sortedByDescending { it.amount }

        return CurrencyStats(
            currencyCode = target,
            startPeriodBalance = safeSum { it.startPeriodBalance }.round(2),
            endPeriodBalance = safeSum { it.endPeriodBalance }.round(2),
            totalIncome = safeSum { it.totalIncome }.round(2),
            totalExpense = safeSum { it.totalExpense }.round(2),
            categories = aggregatedCategories
        )
    }

    private fun calculateFinancialHealth(
        income: Double,
        expense: Double,
        budgets: List<BudgetProgress>,
        dreamProgress: Float
    ): Int {
        val deltaScore = if (income >= expense) 50 else {
            ((income / expense) * 50).toInt().coerceAtLeast(0)
        }

        val avgProgress =
            if (budgets.isEmpty()) 0f else budgets.map { it.progress }.average().toFloat()
        val budgetScore = ((1f - avgProgress.coerceIn(0f, 1f)) * 40).toInt()

        val dreamScore = (dreamProgress * 10).toInt()

        return (deltaScore + budgetScore + dreamScore).coerceIn(0, 100)
    }
}