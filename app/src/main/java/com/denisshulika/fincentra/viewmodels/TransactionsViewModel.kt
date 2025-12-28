package com.denisshulika.fincentra.viewmodels

import androidx.lifecycle.ViewModel
import com.denisshulika.fincentra.data.models.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TransactionsViewModel : ViewModel() {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        _transactions.value = listOf(
            Transaction(amount = 150.0, description = "Кава", bankName = "Monobank"),
            Transaction(amount = 2000.0, description = "Зарплата", bankName = "ПриватБанк", isExpense = false),
            Transaction(amount = 500.0, description = "Продукти", bankName = "Готівка")
        )
    }
}