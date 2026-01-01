package com.denisshulika.fincentra.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.models.BankAccount
import com.denisshulika.fincentra.data.models.Transaction
import com.denisshulika.fincentra.data.network.monobank.MonobankService
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IntegrationsViewModel : ViewModel() {
    private val repository = DependencyProvider.repository
    private val monoService = MonobankService()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isBankConnected = MutableStateFlow(false)
    val isBankConnected: StateFlow<Boolean> = _isBankConnected.asStateFlow()

    private val _monoToken = MutableStateFlow("")
    val monoToken: StateFlow<String> = _monoToken.asStateFlow()

    private val _isMonoInputVisible = MutableStateFlow(false)
    val isMonoInputVisible: StateFlow<Boolean> = _isMonoInputVisible.asStateFlow()

    private val _availableAccounts = MutableStateFlow<List<BankAccount>>(emptyList())
    val availableAccounts: StateFlow<List<BankAccount>> = _availableAccounts.asStateFlow()

    private val _showAccountSelection = MutableStateFlow(false)
    val showAccountSelection: StateFlow<Boolean> = _showAccountSelection.asStateFlow()

    init {
        checkConnectionStatus()
    }

    private fun checkConnectionStatus() {
        viewModelScope.launch {
            val token = repository.getMonoToken()
            _isBankConnected.value = !token.isNullOrBlank()
        }
    }

    fun fetchAccountsAndShowSelection() {
        viewModelScope.launch {
            val token = _monoToken.value.ifBlank { repository.getMonoToken() }
            if (token.isNullOrBlank()) return@launch

            _isLoading.value = true
            val accounts = monoService.fetchAccounts(token)
            val alreadySelectedIds = repository.getSelectedAccountIds()

            _availableAccounts.value = accounts.map { acc ->
                acc.copy(isSelected = alreadySelectedIds.contains(acc.id))
            }

            _isLoading.value = false
            _showAccountSelection.value = true
        }
    }

    fun toggleAccountSelection(accountId: String) {
        _availableAccounts.value = _availableAccounts.value.map {
            if (it.id == accountId) it.copy(isSelected = !it.isSelected) else it
        }
    }

    fun confirmAccountSelection() {
        viewModelScope.launch {
            val selectedIds = _availableAccounts.value.filter { it.isSelected }.map { it.id }

            repository.saveSelectedAccountIds(selectedIds)

            if (_monoToken.value.isNotBlank()) {
                repository.saveMonoToken(_monoToken.value)
                _monoToken.value = ""
            }

            _showAccountSelection.value = false
            _isMonoInputVisible.value = false
            checkConnectionStatus()
        }
    }

    fun toggleAccountBottomSheet(show: Boolean) {
        _showAccountSelection.value = show
    }

    fun syncMonobank() {
        viewModelScope.launch {
            _isLoading.value = true
            val token = repository.getMonoToken() ?: return@launch
            val selectedIds = repository.getSelectedAccountIds()

            try {
                val actualAccounts = monoService.fetchAccounts(token)
                repository.saveAccounts(actualAccounts)

                val allNewTransactions = mutableListOf<Transaction>()

                for (id in selectedIds) {
                    val account = actualAccounts.find { it.id == id }
                    val currency = account?.currencyCode ?: 980

                    Log.d("MONO_SYNC", "Синхронізація карти $id (Валюта: $currency)")

                    try {
                        val txs = monoService.fetchTransactionsForAccount(token, id, currency)
                        allNewTransactions.addAll(txs)
                    } catch (e: Exception) {
                        Log.e("MONO_SYNC", "Карта $id не завантажилась (ліміт або помилка)")
                    }

                    if (id != selectedIds.last()) {
                        kotlinx.coroutines.delay(5000)
                    }
                }

                if (allNewTransactions.isNotEmpty()) {
                    repository.addTransactionsBatch(allNewTransactions)
                }

            } catch (e: Exception) {
                Log.e("MONO_SYNC", "Глобальна помилка: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun disconnectBank() {
        viewModelScope.launch {
            repository.saveMonoToken("")
            _isBankConnected.value = false
        }
    }

    fun openMonobankAuth() {
        viewModelScope.launch {
            _events.emit("https://api.monobank.ua/")
        }
    }

    fun onTokenChange(newToken: String) { _monoToken.value = newToken }
    fun toggleMonoInput(visible: Boolean) { _isMonoInputVisible.value = visible }
}