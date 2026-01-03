package com.denisshulika.fincentra.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.models.BankAccount
import com.denisshulika.fincentra.data.models.BankProviderInfo
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IntegrationsViewModel : ViewModel() {
    private val repository = DependencyProvider.repository
    private val monoService = DependencyProvider.monobankProvider

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _syncStatus = MutableStateFlow("")
    val syncStatus = _syncStatus.asStateFlow()

    private val _showAccountSelection = MutableStateFlow(false)
    val showAccountSelection = _showAccountSelection.asStateFlow()

    private val _availableAccounts = MutableStateFlow<List<BankAccount>>(emptyList())
    val availableAccounts = _availableAccounts.asStateFlow()

    private val _isBankConnected = MutableStateFlow(false)
    val isBankConnected = _isBankConnected.asStateFlow()

    private val _monoToken = MutableStateFlow("")
    val monoToken = _monoToken.asStateFlow()

    private val _isMonoInputVisible = MutableStateFlow(false)
    val isMonoInputVisible = _isMonoInputVisible.asStateFlow()

    private val _events = MutableSharedFlow<IntegrationsUiEvent>()
    val events = _events.asSharedFlow()

    val lastSyncTime = repository.getLastGlobalSyncTimeFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    private val savedAccounts = repository.getAccountsFlow().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedBank = MutableStateFlow<BankProviderInfo?>(null)
    val selectedBank = _selectedBank.asStateFlow()

    private val _showDeleteConfirmation = MutableStateFlow(false)
    val showDeleteConfirmation = _showDeleteConfirmation.asStateFlow()

    private val _syncProgress = MutableStateFlow(0f)
    val syncProgress = _syncProgress.asStateFlow()

    init { checkConnectionStatus() }

    private fun checkConnectionStatus() {
        viewModelScope.launch {
            val token = repository.getMonoToken()
            _isBankConnected.value = !token.isNullOrBlank()
        }
    }

    fun selectBank(bank: BankProviderInfo) {
        _selectedBank.value = bank
        if (_isBankConnected.value) {
            openAccountSettings()
        }
    }

    fun closeBankDetails() {
        _selectedBank.value = null
    }

    fun askDeleteConfirmation() {
        _showDeleteConfirmation.value = true
    }

    fun dismissDeleteConfirmation() {
        _showDeleteConfirmation.value = false
    }

    fun refreshAccountsInDetails() {
        viewModelScope.launch {
            if (_isLoading.value) return@launch
            _isLoading.value = true
            _syncStatus.value = "Оновлюємо дані з банку..."

            try {
                val token = repository.getMonoToken() ?: return@launch
                val actualAccounts = monoService.fetchAccounts(token)
                if (actualAccounts.isNotEmpty()) {
                    repository.saveAccounts(actualAccounts, updateSelection = false)
                    _availableAccounts.value = actualAccounts
                    _events.emit(IntegrationsUiEvent.ShowToast("Рахунки оновлено"))
                }
            } catch (e: Exception) {
                _events.emit(IntegrationsUiEvent.ShowToast("Помилка оновлення"))
            } finally {
                _isLoading.value = false
                _syncStatus.value = ""
            }
        }
    }

    fun openAccountSettings() {
        viewModelScope.launch {
            if (_isLoading.value) return@launch
            _isLoading.value = true

            try {
                var accounts = savedAccounts.value

                if (accounts.isEmpty()) {
                    accounts = repository.getAccountsOnce()
                }

                if (accounts.isNotEmpty()) {
                    _availableAccounts.value = accounts
                    _showAccountSelection.value = true
                } else {
                    _events.emit(IntegrationsUiEvent.ShowToast("Спочатку підключіть банк"))
                    _isMonoInputVisible.value = true
                }
            } catch (e: Exception) {
                _events.emit(IntegrationsUiEvent.ShowToast("Помилка завантаження даних"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun syncMonobank() {
        viewModelScope.launch {
            if (_isLoading.value) return@launch
            _isLoading.value = true
            _syncProgress.value = 0f

            try {
                val token = repository.getMonoToken() ?: return@launch
                val actualAccounts = monoService.fetchAccounts(token)

                val selectedIds = repository.getSelectedAccountIds()
                val totalAccounts = selectedIds.size

                if (totalAccounts == 0) {
                    _isLoading.value = false
                    return@launch
                }

                for ((index, id) in selectedIds.withIndex()) {
                    val account = actualAccounts.find { it.id == id } ?: continue

                    _syncStatus.value = "Синхронізація: ${account.name}..."

                    val lastSync = repository.getLastSyncTimestamp(id)
                    val fromTime = if (lastSync == 0L) 0L else (lastSync / 1000 + 1)

                    monoService.fetchTransactionsForAccount(
                        token = token,
                        accountId = id,
                        accountCurrency = account.currencyCode,
                        fromTimeSeconds = fromTime,
                        onProgress = { status -> _syncStatus.value = status },
                        onBatchLoaded = { batch ->
                            repository.addTransactionsBatch(batch)
                            repository.saveLastSyncTimestamp(id, batch.maxOf { it.timestamp })
                        }
                    )
                    _syncProgress.value = (index + 1).toFloat() / totalAccounts.toFloat()

                    if (index < selectedIds.size - 1) {
                        for (i in 60 downTo 1) {
                            _syncStatus.value = "Наступна карта через ${i}с..."
                            delay(1000)
                        }
                    }
                }

                repository.saveLastGlobalSyncTime(System.currentTimeMillis())
                _syncStatus.value = "Готово!"
            } catch (e: Exception) {
                _syncStatus.value = "Помилка мережі"
            } finally {
                _isLoading.value = false
                delay(3000)
                _syncStatus.value = ""
                _syncProgress.value = 0f
            }
        }
    }

    fun connectNewBank() {
        viewModelScope.launch {
            if (_isLoading.value) return@launch
            _isLoading.value = true
            try {
                val token = _monoToken.value.trim()
                val apiAccounts = monoService.fetchAccounts(token)
                if (apiAccounts.isNotEmpty()) {
                    repository.saveMonoToken(token)
                    repository.saveAccounts(apiAccounts, updateSelection = false)
                    _isBankConnected.value = true
                    _isMonoInputVisible.value = false
                    _monoToken.value = ""

                    _availableAccounts.value = repository.getAccountsOnce()
                    _showAccountSelection.value = true
                }
            } catch (e: Exception) { _events.emit(IntegrationsUiEvent.ShowToast("Помилка")) }
            finally { _isLoading.value = false }
        }
    }

    fun confirmAccountSelection() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.saveAccounts(_availableAccounts.value, updateSelection = true)
                _showAccountSelection.value = false
                checkConnectionStatus()
                syncMonobank()
            } finally { _isLoading.value = false }
        }
    }

    fun openMonobankAuth() {
        viewModelScope.launch {
            _events.emit(IntegrationsUiEvent.OpenUrl("https://api.monobank.ua/"))
        }
    }

    fun toggleAccountSelection(accountId: String) {
        _availableAccounts.value = _availableAccounts.value.map { if (it.id == accountId) it.copy(selected = !it.selected) else it }
    }

    fun toggleAccountBottomSheet(show: Boolean) {
        _showAccountSelection.value = show
    }

    fun onTokenChange(newToken: String) {
        _monoToken.value = newToken
    }

    fun toggleMonoInput(visible: Boolean) {
        _isMonoInputVisible.value = visible
    }

    fun disconnectBank() {
        viewModelScope.launch {
            if (_isLoading.value) return@launch
            _isLoading.value = true
            _syncStatus.value = "Відключення банку..."

            try {
                repository.clearMonobankData()
                _isBankConnected.value = false
                _events.emit(IntegrationsUiEvent.ShowToast("Monobank відключено. Дані приховано."))
            } catch (e: Exception) {
                _events.emit(IntegrationsUiEvent.ShowToast("Помилка при відключенні"))
            } finally {
                _isLoading.value = false
                _syncStatus.value = ""
            }
        }
    }
}