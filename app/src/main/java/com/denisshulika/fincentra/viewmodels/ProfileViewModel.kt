package com.denisshulika.fincentra.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val repository = FinanceRepository()

    private val _totalBalance = MutableStateFlow(0.0)
    val totalBalance: StateFlow<Double> = _totalBalance.asStateFlow()

    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> = _totalIncome.asStateFlow()

    private val _totalExpenses = MutableStateFlow(0.0)
    val totalExpenses: StateFlow<Double> = _totalExpenses.asStateFlow()

    init {
        fetchAndCalculate()
    }

    fun fetchAndCalculate() {
        viewModelScope.launch {
            val transactions = repository.getAllTransactions()

            var incomeSum = 0.0
            var expenseSum = 0.0

            transactions.forEach { tx ->
                if (tx.isExpense) {
                    expenseSum += tx.amount
                } else {
                    incomeSum += tx.amount
                }
            }

            _totalIncome.value = incomeSum
            _totalExpenses.value = expenseSum
            _totalBalance.value = incomeSum - expenseSum
        }
    }
}