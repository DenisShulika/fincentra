package com.denisshulika.fincentra.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.models.domain.Budget
import com.denisshulika.fincentra.data.models.domain.BudgetProgress
import com.denisshulika.fincentra.data.models.domain.TransactionCategory
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class BudgetsViewModel : ViewModel() {
    private val repository = DependencyProvider.repository

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    private val currentMonthYear: String
        get() {
            val cal = Calendar.getInstance()
            return "${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.YEAR)}"
        }

    val budgetProgressList: StateFlow<List<BudgetProgress>> = combine(
        repository.getBudgetsFlow(currentMonthYear),
        repository.transactions
    ) { budgets, transactions ->

        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        budgets.map { budget ->
            val spent = transactions.filter { tx ->
                val txCal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                tx.isExpense &&
                        tx.category.displayName == budget.categoryName &&
                        txCal.get(Calendar.MONTH) == currentMonth &&
                        txCal.get(Calendar.YEAR) == currentYear &&
                        tx.currencyCode == budget.currencyCode
            }.sumOf { it.amount }

            BudgetProgress(
                budget = budget,
                spentAmount = spent,
                remainingAmount = (budget.limitAmount - spent).coerceAtLeast(0.0),
                progress = if (budget.limitAmount > 0) (spent / budget.limitAmount).toFloat().coerceIn(0f, 1f) else 0f
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showAddSheet = MutableStateFlow(false)
    val showAddSheet = _showAddSheet.asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount = _amount.asStateFlow()

    private val _selectedCategory = MutableStateFlow(TransactionCategory.FOOD)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedCurrency = MutableStateFlow(980)
    val selectedCurrency = _selectedCurrency.asStateFlow()

    fun toggleAddSheet(show: Boolean) {
        _showAddSheet.value = show
        if (!show) {
            _amount.value = ""
        }
    }

    fun onAmountChange(newAmount: String) {
        _amount.value = newAmount.filter { it.isDigit() }
    }

    fun onCategoryChange(cat: TransactionCategory) { _selectedCategory.value = cat }

    fun onCurrencyChange(code: Int) { _selectedCurrency.value = code }

    fun saveNewBudget() {
        val amt = _amount.value.toDoubleOrNull() ?: return
        setLimit(_selectedCategory.value, amt, _selectedCurrency.value)
        toggleAddSheet(false)
    }

    fun setLimit(category: TransactionCategory, amount: Double, currencyCode: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val budget = Budget(
                id = "${category.name}_$currentMonthYear",
                categoryName = category.displayName,
                limitAmount = amount,
                currencyCode = currencyCode,
                monthYear = currentMonthYear
            )
            repository.saveBudget(budget)
            _isLoading.value = false
        }
    }

    fun deleteBudget(budgetId: String) {
        viewModelScope.launch {
            repository.deleteBudget(budgetId)
        }
    }
}