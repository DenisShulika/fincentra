package com.denisshulika.fincentra.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.models.Transaction
import com.denisshulika.fincentra.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionsViewModel : ViewModel() {

    private val repository = FinanceRepository()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    init {
        fetchTransactions()
    }

    private fun fetchTransactions() {
        viewModelScope.launch {
            val result = repository.getAllTransactions()
            if(result.isNotEmpty()) {
                _transactions.value = result
            }
        }
    }

    fun addTestTransaction() {
        viewModelScope.launch {
            val testTx = Transaction(
                amount = (10..1000).random().toDouble(),
                description = "Тестова витрата",
                bankName = "Готівка"
            )
            repository.addTransaction(testTx)
            _transactions.value = listOf(testTx) + _transactions.value
        }
    }
}