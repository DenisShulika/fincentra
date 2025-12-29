package com.denisshulika.fincentra.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.models.Transaction
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionsViewModel : ViewModel() {

    private val repository = DependencyProvider.repository

    val transactions: StateFlow<List<Transaction>> = repository.transactions

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet.asStateFlow()

    private val _isExpense = MutableStateFlow(true)
    val isExpense: StateFlow<Boolean> = _isExpense.asStateFlow()

    private val _category = MutableStateFlow("Різне")
    val category: StateFlow<String> = _category.asStateFlow()

    val categories = listOf("Їжа", "Транспорт", "Житло", "Здоров'я", "Розваги", "Зарплата", "Різне")

    val expenseOptions = listOf("Витрата", "Дохід")

    private val _editingTransactionId = MutableStateFlow<String?>(null)
    val editingTransactionId: StateFlow<String?> = _editingTransactionId.asStateFlow()

    private var editingTimestamp: Long? = null

    init {
        viewModelScope.launch {
            repository.fetchTransactions()
        }
    }

    fun onAmountChange(newAmount: String) {
        val standardized = newAmount.replace(',', '.')
        val filtered = standardized.filterIndexed { index, char ->
            char.isDigit() || (char == '.' && standardized.indexOf('.') == index)
        }

        _amount.value = filtered
    }

    fun onDescriptionChange(newDesc: String) {
        _description.value = newDesc
    }

    fun onTypeChange(isExpense: Boolean) {
        _isExpense.value = isExpense
    }

    fun onCategoryChange(category: String) {
        _category.value = category
    }

    fun prepareForEdit(transaction: Transaction) {
        _amount.value = transaction.amount.toString()
        _description.value = transaction.description
        _isExpense.value = transaction.isExpense
        _category.value = transaction.category
        _editingTransactionId.value = transaction.id
        editingTimestamp = transaction.timestamp
        _showBottomSheet.value = true
    }

    fun toggleBottomSheet(show: Boolean) {
        _showBottomSheet.value = show
        if (!show) {
            _amount.value = ""
            _description.value = ""
            _isExpense.value = true
            _category.value = "Різне"
            _editingTransactionId.value = null
            editingTimestamp = null
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction.id)
        }
    }

    fun saveTransaction() {
        val amountDouble = _amount.value.toDoubleOrNull() ?: return
        val id = _editingTransactionId.value ?: java.util.UUID.randomUUID().toString()
        val timestamp = editingTimestamp ?: System.currentTimeMillis()

        viewModelScope.launch {
            val transaction = Transaction(
                id = id,
                amount = amountDouble,
                description = _description.value,
                bankName = "Готівка",
                category = _category.value,
                isExpense = _isExpense.value,
                timestamp = timestamp
            )
            repository.addTransaction(transaction)
            toggleBottomSheet(false)
        }
    }
}