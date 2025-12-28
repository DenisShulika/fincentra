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

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet.asStateFlow()

    init {
        fetchTransactions()
    }

    fun onAmountChange(newAmount: String) {
        _amount.value = newAmount
    }

    fun onDescriptionChange(newDesc: String) {
        _description.value = newDesc
    }

    fun toggleBottomSheet(show: Boolean) {
        _showBottomSheet.value = show
        if (!show) {
            _amount.value = ""
            _description.value = ""
        }
    }

    private fun fetchTransactions() {
        viewModelScope.launch {
            val result = repository.getAllTransactions()
            if(result.isNotEmpty()) {
                _transactions.value = result
            }
        }
    }

    fun addTransaction() {
        val amountDouble = _amount.value.toDoubleOrNull() ?: return
        val desc = _description.value

        viewModelScope.launch {
            val newTx = Transaction(
                amount = amountDouble,
                description = desc,
                bankName = "Готівка",
                isExpense = true
            )
            repository.addTransaction(newTx)
            _transactions.value = listOf(newTx) + _transactions.value

            toggleBottomSheet(false)
        }
    }
}