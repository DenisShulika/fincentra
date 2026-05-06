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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class BudgetsViewModel : ViewModel() {
    private val financeRepository = DependencyProvider.financeRepository
    private val budgetRepository = DependencyProvider.budgetRepository

    private val _amount = MutableStateFlow("")
    val amount = _amount.asStateFlow()

    private val _selectedCategory = MutableStateFlow(TransactionCategory.FOOD)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedCurrency = MutableStateFlow(980)
    val selectedCurrency = _selectedCurrency.asStateFlow()

    private val _editingBudgetId = MutableStateFlow<String?>(null)
    val editingBudgetId = _editingBudgetId.asStateFlow()

    private val _showAddSheet = MutableStateFlow(false)
    val showAddSheet = _showAddSheet.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val budgetProgressList: StateFlow<List<BudgetProgress>> = combine(
        financeRepository.budgets,
        financeRepository.transactions,
        flow { emit(DependencyProvider.currencyRepository.getRates()) }
    ) { budgets, transactions, rates ->
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        budgets.map { budget ->
            val spentInBudgetCurrency = transactions.filter { tx ->
                val txCal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                tx.isExpense &&
                        tx.category.name == budget.categoryName &&
                        txCal.get(Calendar.MONTH) == currentMonth &&
                        txCal.get(Calendar.YEAR) == currentYear
            }.sumOf { tx ->
                DependencyProvider.currencyRepository.convert(
                    amount = tx.amount,
                    from = tx.currencyCode,
                    to = budget.currencyCode,
                    rates = rates
                ) ?: 0.0
            }

            BudgetProgress(
                budget = budget,
                spentAmount = spentInBudgetCurrency,
                remainingAmount = (budget.limitAmount - spentInBudgetCurrency).coerceAtLeast(0.0),
                progress = if (budget.limitAmount > 0) {
                    (spentInBudgetCurrency / budget.limitAmount).toFloat().coerceIn(0f, 1.1f)
                } else 0f
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleAddSheet(show: Boolean) {
        _showAddSheet.value = show
        if (!show) resetForm()
    }

    fun onAmountChange(v: String) {
        _amount.value = v.filter { it.isDigit() || it == '.' }
    }

    fun onCategoryChange(cat: TransactionCategory) {
        _selectedCategory.value = cat
    }

    fun onCurrencyChange(code: Int) {
        _selectedCurrency.value = code
    }

    fun prepareForEdit(budget: Budget) {
        _amount.value = budget.limitAmount.toInt().toString()
        _selectedCategory.value =
            TransactionCategory.entries.find { it.name == budget.categoryName }
                ?: TransactionCategory.OTHERS
        _selectedCurrency.value = budget.currencyCode
        _editingBudgetId.value = budget.id
        _showAddSheet.value = true
    }

    fun saveBudget() {
        val amt = _amount.value.toDoubleOrNull() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val cal = Calendar.getInstance()
            val monthYear = "${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.YEAR)}"

            val budget = Budget(
                id = _editingBudgetId.value
                    ?: "${_selectedCategory.value.name}_${System.currentTimeMillis()}",
                categoryName = _selectedCategory.value.name,
                limitAmount = amt,
                currencyCode = _selectedCurrency.value,
                monthYear = monthYear
            )
            budgetRepository.saveBudget(budget)
            toggleAddSheet(false)
            _isLoading.value = false
        }
    }

    fun resetForm() {
        _amount.value = ""
        _selectedCategory.value = TransactionCategory.FOOD
        _selectedCurrency.value = 980
        _editingBudgetId.value = null
    }

    fun deleteBudget() {
        val id = _editingBudgetId.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            budgetRepository.deleteBudget(id)
            toggleAddSheet(false)
            _isLoading.value = false
        }
    }
}