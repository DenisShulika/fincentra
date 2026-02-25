package com.denisshulika.fincentra.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.models.domain.BudgetProgress
import com.denisshulika.fincentra.data.repository.AiRepository
import com.denisshulika.fincentra.data.util.LanguageManager
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class AiViewModel : ViewModel() {
    private val aiRepository = AiRepository()
    private val financeRepository = DependencyProvider.financeRepository

    private val _adviceText = MutableStateFlow("")
    val adviceText = _adviceText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun fetchAdvice(userName: String, currentBudgets: List<BudgetProgress>) {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _adviceText.value = ""

            val langName = when (LanguageManager.getCurrentLanguage()) {
                "uk" -> "Ukrainian"
                "pl" -> "Polish"
                "de" -> "German"
                else -> "English"
            }

            val txs = financeRepository.transactions.value
            val accounts = financeRepository.accounts.value
            val dream = financeRepository.dream.value

            val now = Calendar.getInstance()
            val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
            val currentDay = now.get(Calendar.DAY_OF_MONTH)
            val monthProgress = (currentDay.toFloat() / daysInMonth.toFloat() * 100).toInt()

            val cal = now.clone() as Calendar
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            val startOfMonth = cal.timeInMillis

            val currentTotal = accounts.filter { it.selected }.sumOf { it.balance }
            val monthTxs = txs.filter { it.timestamp >= startOfMonth }
            val income = monthTxs.filter { !it.isExpense }.sumOf { it.amount }
            val expense = monthTxs.filter { it.isExpense }.sumOf { it.amount }
            val startBalance = currentTotal - income + expense

            val budgetStatus = currentBudgets.joinToString {
                val name =
                    it.budget.categoryName.lowercase().replaceFirstChar { c -> c.uppercase() }
                "$name: ${(it.progress * 100).toInt()}%"
            }

            val dailyBurnRate = if (currentDay > 0) expense / currentDay else 0.0
            val projectedExpense = dailyBurnRate * daysInMonth
            val incomeShortfall = if (projectedExpense > income) projectedExpense - income else 0.0

            val recentTxsSummary = txs.take(5).joinToString {
                "${it.description}: ${it.amount.toInt()}"
            }

            val prompt = """
            CONTEXT: Day $currentDay of $daysInMonth ($monthProgress% of month complete).
            
            PERSONAL FINANCIAL DATA:
            - Monthly Income: ${income.toInt()}
            - Actual Spending: ${expense.toInt()}
            - Projected Deficit: ${incomeShortfall.toInt()}
            - Available Wallet: ${currentTotal.toInt()}
            
            BUDGETS TRACKING:
            $budgetStatus
            
            RECENT PURCHASES:
            $recentTxsSummary
            
            DREAM GOAL: ${dream?.title}
            
            TASK FOR ADVISOR:
            1. Analyze my spending habits for this month. 
            2. Tell me if I am moving toward or away from my "${dream?.title}".
            3. State if my "Tree" is stable or needs attention.
            4. Use simple, direct language. NO corporate jargon. NO NUMBERS.
            RESPONSE LANGUAGE: $langName.
        """.trimIndent()

            val result = aiRepository.getAdvice(prompt)

            if (result == null) {
                _adviceText.value = "ERROR_STATE"
            } else {
                var currentText = ""
                result.forEach { char ->
                    currentText += char
                    _adviceText.value = currentText
                    delay(15)
                }
            }
            _isLoading.value = false
        }
    }
}