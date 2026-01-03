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

    private val _syncProgress = MutableStateFlow(0f)
    val syncProgress = _syncProgress.asStateFlow()

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

    fun syncMonobank() {
        viewModelScope.launch {
            if (_isLoading.value) return@launch
            _isLoading.value = true
            _syncProgress.value = 0f
            Log.d("SYNC_DEBUG", "--- СТАРТ СИНХРОНІЗАЦІЇ ---")

            try {
                val token = repository.getMonoToken() ?: return@launch

                _syncStatus.value = "Оновлення балансів..."
                val actualAccounts = monoService.fetchAccounts(token)
                if (actualAccounts.isNotEmpty()) {
                    repository.saveAccounts(actualAccounts, updateSelection = false)
                }

                val selectedIds = repository.getSelectedAccountIds()
                val accountsToSync = actualAccounts.filter { selectedIds.contains(it.id) }

                if (accountsToSync.isEmpty()) {
                    _syncStatus.value = "Рахунки не вибрані"
                    delay(3000)
                    _isLoading.value = false
                    return@launch
                }

                for ((index, account) in accountsToSync.withIndex()) {
                    _syncStatus.value = "Синхронізація: ${account.name}..."

                    val lastSync = repository.getLastSyncTimestamp(account.id)
                    val fromTime = if (lastSync == 0L) {
                        (System.currentTimeMillis() / 1000) - 2682000L
                    } else {
                        (lastSync / 1000 + 1)
                    }

                    Log.d("SYNC_DEBUG", "Запит для ${account.name} з From: $fromTime")

                    monoService.fetchTransactionsForAccount(
                        token = token,
                        accountId = account.id,
                        accountCurrency = account.currencyCode,
                        fromTimeSeconds = fromTime,
                        onProgress = { status -> _syncStatus.value = status },
                        onBatchLoaded = { batch ->
                            Log.d("SYNC_DEBUG", "!!! ПРИЙШЛО ДЛЯ БАЗИ: ${batch.size} шт.")
                            repository.addTransactionsBatch(batch)
                            repository.saveLastSyncTimestamp(account.id, batch.maxOf { it.timestamp })
                        }
                    )

                    _syncProgress.value = (index + 1).toFloat() / accountsToSync.size.toFloat()

                    if (index < accountsToSync.size - 1) {
                        for (i in 60 downTo 1) {
                            _syncStatus.value = "Наступна карта через ${i}с..."
                            delay(1000)
                        }
                    }
                }

                repository.saveLastGlobalSyncTime(System.currentTimeMillis())
                _syncStatus.value = "Готово!"
                _syncProgress.value = 1f
                _events.emit(IntegrationsUiEvent.ShowToast("Синхронізацію завершено!"))
                delay(3000)

            } catch (e: Exception) {
                Log.e("SYNC_DEBUG", "Помилка: ${e.message}")
                _syncStatus.value = "Помилка API"
                delay(3000)
            } finally {
                _syncProgress.value = 0f
                for (i in 57 downTo 1) {
                    _syncStatus.value = "Відпочинок API: $i с..."
                    delay(1000)
                }
                _isLoading.value = false
                _syncStatus.value = ""
            }
        }
    }

    fun refreshAccountsInDetails() {
        viewModelScope.launch {
            if (_isLoading.value) return@launch
            _isLoading.value = true
            _syncStatus.value = "Отримання даних..."
            try {
                val token = repository.getMonoToken() ?: return@launch
                val actualAccounts = monoService.fetchAccounts(token)

                if (actualAccounts.isNotEmpty()) {
                    val selectedIds = repository.getSelectedAccountIds()

                    val mergedAccounts = actualAccounts.map { acc ->
                        acc.copy(selected = selectedIds.contains(acc.id))
                    }.sortedBy { it.id }

                    repository.saveAccounts(mergedAccounts, updateSelection = false)
                    _availableAccounts.value = mergedAccounts

                    _events.emit(IntegrationsUiEvent.ShowToast("Рахунки оновлено"))
                }
            } catch (e: Exception) {
                _events.emit(IntegrationsUiEvent.ShowToast("Помилка API"))
            } finally {
                _isLoading.value = false
                _syncStatus.value = ""
            }
        }
    }

    fun confirmAccountSelection() {
        viewModelScope.launch {
            try {
                repository.saveAccounts(_availableAccounts.value, updateSelection = true)
                _showAccountSelection.value = false
                checkConnectionStatus()
                delay(200)
                syncMonobank()
            } catch (e: Exception) {
                _events.emit(IntegrationsUiEvent.ShowToast("Помилка"))
            }
        }
    }

    fun openAccountSettings() {
        viewModelScope.launch {
            if (_isLoading.value) return@launch
            _isLoading.value = true
            try {
                val accounts = repository.getAccountsOnce()
                if (accounts.isNotEmpty()) {
                    _availableAccounts.value = accounts
                } else {
                    _isMonoInputVisible.value = true
                }
            } finally { _isLoading.value = false }
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

                    val sortedAccounts = apiAccounts.sortedBy { it.id }

                    repository.saveAccounts(sortedAccounts, updateSelection = false)
                    _isBankConnected.value = true
                    _isMonoInputVisible.value = false
                    _monoToken.value = ""
                    _availableAccounts.value = sortedAccounts
                    _showAccountSelection.value = true
                }
            } finally { _isLoading.value = false }
        }
    }

    fun openMonobankAuth() { viewModelScope.launch { _events.emit(IntegrationsUiEvent.OpenUrl("https://api.monobank.ua/")) } }
    fun toggleAccountSelection(id: String) { _availableAccounts.value = _availableAccounts.value.map { if (it.id == id) it.copy(selected = !it.selected) else it } }
    fun toggleAccountBottomSheet(show: Boolean) { _showAccountSelection.value = show }
    fun onTokenChange(newToken: String) { _monoToken.value = newToken }
    fun toggleMonoInput(visible: Boolean) { _isMonoInputVisible.value = visible }
    fun askDeleteConfirmation() { _showDeleteConfirmation.value = true }
    fun dismissDeleteConfirmation() { _showDeleteConfirmation.value = false }
    fun disconnectBank() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.clearMonobankData()
                _isBankConnected.value = false
                closeBankDetails()
                _events.emit(IntegrationsUiEvent.ShowToast("Банк відключено"))
            } finally {
                _isLoading.value = false
                _showDeleteConfirmation.value = false
            }
        }
    }
}