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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IntegrationsViewModel : ViewModel() {
    private val repository = DependencyProvider.repository
    private val monoService = MonobankService()

    private val _events = MutableSharedFlow<IntegrationsUiEvent>()
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

    val lastSyncTime: StateFlow<Long?> = repository.getLastGlobalSyncTimeFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun openMonobankAuth() {
        viewModelScope.launch {
            _events.emit(IntegrationsUiEvent.OpenUrl("https://api.monobank.ua/"))
        }
    }

    fun syncMonobank() {
        viewModelScope.launch {
            _isLoading.value = true
            val token = repository.getMonoToken() ?: return@launch
            val selectedIds = repository.getSelectedAccountIds()

            try {
                _syncStatus.value = "Оновлення балансів..."
                val actualAccounts = monoService.fetchAccounts(token)
                repository.saveAccounts(actualAccounts)

                if (selectedIds.isNotEmpty()) {
                    val allNewTransactions = mutableListOf<Transaction>()

                    selectedIds.forEachIndexed { index, id ->
                        val account = actualAccounts.find { it.id == id }
                        val name = account?.name ?: "карти"
                        _syncStatus.value = "Завантаження $name..."

                        try {
                            val txs = monoService.fetchTransactionsForAccount(token, id, account?.currencyCode ?: 980)
                            allNewTransactions.addAll(txs)
                        } catch (e: Exception) {
                            Log.e("MONO_SYNC", "Помилка карти $id")
                            _events.emit(IntegrationsUiEvent.ShowToast("Помилка завантаження $name"))
                        }

                        if (id != selectedIds.last()) {
                            kotlinx.coroutines.delay(5000)
                        }
                    }

                    if (allNewTransactions.isNotEmpty()) {
                        repository.addTransactionsBatch(allNewTransactions)
                        _syncStatus.value = "Готово! +${allNewTransactions.size}"
                    }
                }
            } catch (e: Exception) {
                _syncStatus.value = "Помилка"
                _events.emit(IntegrationsUiEvent.ShowToast("Помилка мережі"))
            } finally {
                _isLoading.value = false
                kotlinx.coroutines.delay(3000)
                _syncStatus.value = ""
            }
        }
    }

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

    private val _syncStatus = MutableStateFlow("")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    fun disconnectBank() {
        viewModelScope.launch {
            repository.saveMonoToken("")
            _isBankConnected.value = false
        }
    }

    fun onTokenChange(newToken: String) { _monoToken.value = newToken }
    fun toggleMonoInput(visible: Boolean) { _isMonoInputVisible.value = visible }
}