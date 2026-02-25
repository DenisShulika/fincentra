package com.denisshulika.fincentra.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun fetchAdvice(userName: String) {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _adviceText.value = ""

            val lang = LanguageManager.getCurrentLanguage()
            val transactions = financeRepository.transactions.value
            val budgets = financeRepository.budgets.value
            val dream = financeRepository.dream.value
            val accounts = financeRepository.accounts.value

            val now = Calendar.getInstance()
            val startOfDay = now.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0) }.timeInMillis
            val startOfLastWeek = now.apply { add(Calendar.DAY_OF_YEAR, -7) }.timeInMillis

            val todaySpent = transactions.filter { it.timestamp >= startOfDay && it.isExpense }.sumOf { it.amount }
            val weekSpent = transactions.filter { it.timestamp >= startOfLastWeek && it.isExpense }.sumOf { it.amount }
            val avgDaily = weekSpent / 7

            val prompt = """
                Language: $lang. User: $userName.
                Today spent: $todaySpent. Weekly average: $avgDaily.
                Dream target: ${dream?.targetAmount ?: 0}.
                Task: Compare today's spending with average. If today is 0, calculate potential savings. 
                Mention if any "Trees" (budgets) are drying up. Give one sharp tip.
            """.trimIndent()

            val result = aiRepository.getAdvice(prompt)

            if (result == null) {
                _adviceText.value = "ERROR_STATE"
            } else {
                var currentText = ""
                result.forEach { char ->
                    currentText += char
                    _adviceText.value = currentText
                    delay(20)
                }
            }
            _isLoading.value = false
        }
    }
}