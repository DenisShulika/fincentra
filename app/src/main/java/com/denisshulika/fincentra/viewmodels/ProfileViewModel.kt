package com.denisshulika.fincentra.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.models.Transaction
import com.denisshulika.fincentra.data.repository.FinanceRepository
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val repository = DependencyProvider.repository

    private val _totalBalance = MutableStateFlow(0.0)
    val totalBalance: StateFlow<Double> = _totalBalance.asStateFlow()

    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> = _totalIncome.asStateFlow()

    private val _totalExpenses = MutableStateFlow(0.0)
    val totalExpenses: StateFlow<Double> = _totalExpenses.asStateFlow()

    init {
        viewModelScope.launch {
            repository.transactions.collect { list ->
                calculateFinances(list)
            }
        }
    }

    private fun calculateFinances(list: List<Transaction>) {
        val income = list.filter { !it.isExpense }.sumOf { it.amount }
        val expense = list.filter { it.isExpense }.sumOf { it.amount }

        _totalIncome.value = income
        _totalExpenses.value = expense
        _totalBalance.value = income - expense
    }
}