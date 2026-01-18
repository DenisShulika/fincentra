package com.denisshulika.fincentra.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.models.domain.BankAccount
import com.denisshulika.fincentra.data.models.events.IntegrationsUiEvent
import com.denisshulika.fincentra.data.models.ui.BankProviderInfo
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
    private val settingsRepository = DependencyProvider.settingsRepository
    private val financeRepository = DependencyProvider.financeRepository
    private val monobankService = DependencyProvider.monobankProvider

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

    private val _monobankToken = MutableStateFlow("")
    val monobankToken = _monobankToken.asStateFlow()

    private val _isMonobankInputVisible = MutableStateFlow(false)
    val isMonobankInputVisible = _isMonobankInputVisible.asStateFlow()

    private val _events = MutableSharedFlow<IntegrationsUiEvent>()
    val events = _events.asSharedFlow()

    val lastSyncTime = settingsRepository.getLastGlobalSyncTimeFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val savedAccounts = financeRepository.accounts
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedBank = MutableStateFlow<BankProviderInfo?>(null)
    val selectedBank = _selectedBank.asStateFlow()

    private val _showDeleteConfirmation = MutableStateFlow(false)
    val showDeleteConfirmation = _showDeleteConfirmation.asStateFlow()

    init {
        refreshConnectionStatus()
    }

    private suspend fun getMappedAccounts(): List<BankAccount> {
        val savedAccounts = financeRepository.getAccountsOnce()
        val selectedIds = settingsRepository.getSelectedAccountIds()
        return savedAccounts.map { acc ->
            acc.copy(selected = selectedIds.contains(acc.id))
        }.sortedBy { it.id }
    }

    fun refreshConnectionStatus() {
        viewModelScope.launch {
            val token = settingsRepository.getMonobankApiToken()
            _isBankConnected.value = !token.isNullOrBlank()

            if (_isBankConnected.value) {
                _availableAccounts.value = getMappedAccounts()
            }
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

    fun syncMonobankData() {
        viewModelScope.launch {
            if (_isLoading.value && _syncStatus.value.isNotEmpty()) return@launch
            _isLoading.value = true
            _syncProgress.value = 0f
            var needsCooldown = false

            try {
                val token = settingsRepository.getMonobankApiToken() ?: return@launch

                _syncStatus.value = "Оновлення балансів..."
                val actualAccounts = monobankService.fetchAccounts(token)
                needsCooldown = true

                if (actualAccounts.isNotEmpty()) {
                    financeRepository.saveAccounts(actualAccounts, updateSelection = false)
                }

                val selectedIds = settingsRepository.getSelectedAccountIds()
                val accountsToSync = actualAccounts.filter { selectedIds.contains(it.id) }

                if (accountsToSync.isEmpty()) {
                    _syncStatus.value = "Рахунки не вибрані"
                    _events.emit(IntegrationsUiEvent.ShowToast("Будь ласка, виберіть рахунки в налаштуваннях"))
                    delay(2000)
                } else {
                    for ((index, account) in accountsToSync.withIndex()) {
                        if (index > 0) {
                            waitForApiCooldown(60, "Наступна карта через")
                        }

                        _syncStatus.value = "Синхронізація: ${account.name}..."

                        val lastSync = settingsRepository.getLastSyncTimestamp(account.id)
                        val fromTime =
                            if (lastSync == 0L) (System.currentTimeMillis() / 1000) - 2682000L else (lastSync / 1000 + 1)

                        monobankService.fetchTransactionsForAccount(
                            token = token,
                            accountId = account.id,
                            accountCurrency = account.currencyCode,
                            fromTimeSeconds = fromTime,
                            onProgress = { status -> _syncStatus.value = status },
                            onBatchLoaded = { batch ->
                                financeRepository.addTransactionsBatch(batch)
                                settingsRepository.saveLastSyncTimestamp(
                                    account.id,
                                    batch.maxOf { it.timestamp }
                                )
                            }
                        )
                        _syncProgress.value = (index + 1).toFloat() / accountsToSync.size.toFloat()
                    }

                    settingsRepository.saveLastGlobalSyncTime(System.currentTimeMillis())
                    _syncStatus.value = "Готово!"
                    _syncProgress.value = 1f
                    delay(2000)
                }
            } catch (e: Exception) {
                _syncStatus.value = "Помилка API"
                delay(2000)
            } finally {
                if (needsCooldown) {
                    _syncProgress.value = 0f
                    waitForApiCooldown(60, "Відпочинок API")
                }
                _syncStatus.value = ""
                _isLoading.value = false
            }
        }
    }

    private suspend fun waitForApiCooldown(seconds: Int, statusPrefix: String) {
        for (i in seconds downTo 1) {
            _syncStatus.value = "$statusPrefix: $i с..."
            delay(1000)
        }
    }

    fun refreshMonobankAccounts() {
        viewModelScope.launch {
            if (_isLoading.value && _syncStatus.value.isNotEmpty()) return@launch
            _isLoading.value = true
            try {
                val token = settingsRepository.getMonobankApiToken() ?: return@launch
                val actualAccounts = monobankService.fetchAccounts(token)
                if (actualAccounts.isNotEmpty()) {
                    val selectedIds = settingsRepository.getSelectedAccountIds()
                    val mergedAccounts = actualAccounts.map { acc ->
                        acc.copy(selected = selectedIds.contains(acc.id))
                    }.sortedBy { it.id }

                    financeRepository.saveAccounts(mergedAccounts, updateSelection = false)
                    _availableAccounts.value = mergedAccounts
                    _events.emit(IntegrationsUiEvent.ShowToast("Оновлено"))
                }
                waitForApiCooldown(60, "Кулдаун")
            } finally {
                _isLoading.value = false
                _syncStatus.value = ""
            }
        }
    }

    fun confirmAccountSelection() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentList = _availableAccounts.value
                val selectedIds = currentList.filter { it.selected }.map { it.id }

                settingsRepository.saveSelectedAccountIds(selectedIds)
                financeRepository.saveAccounts(currentList, updateSelection = true)

                syncMonobankData()
            } catch (e: Exception) {
                _isLoading.value = false
                _events.emit(IntegrationsUiEvent.ShowToast("Помилка"))
            }
        }
    }

    fun openAccountSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _availableAccounts.value = getMappedAccounts()
            } finally {
                if (_syncStatus.value.isEmpty()) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun connectMonobankAccount() {
        viewModelScope.launch {
            if (_isLoading.value && _syncStatus.value.isNotEmpty()) return@launch
            _isLoading.value = true
            try {
                val token = _monobankToken.value.trim()
                val apiAccounts = monobankService.fetchAccounts(token)
                if (apiAccounts.isNotEmpty()) {
                    val selectedIds = settingsRepository.getSelectedAccountIds()

                    settingsRepository.saveMonobankApiToken(token)

                    val markedAccounts = apiAccounts.map { acc ->
                        acc.copy(selected = selectedIds.contains(acc.id))
                    }

                    financeRepository.saveAccounts(markedAccounts, updateSelection = false)
                    _availableAccounts.value = markedAccounts.sortedBy { it.id }

                    _isBankConnected.value = true
                    _isMonobankInputVisible.value = false
                    _monobankToken.value = ""
                    _showAccountSelection.value = true
                }
            } catch (e: Exception) {
                _events.emit(IntegrationsUiEvent.ShowToast("Помилка токена"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun openMonobankAuth() {
        viewModelScope.launch { _events.emit(IntegrationsUiEvent.OpenUrl("https://api.monobank.ua/")) }
    }

    fun toggleAccountSelection(id: String) {
        _availableAccounts.value =
            _availableAccounts.value.map { if (it.id == id) it.copy(selected = !it.selected) else it }
    }

    fun toggleAccountBottomSheet(show: Boolean) {
        _showAccountSelection.value = show
    }

    fun onMonobankTokenChange(newToken: String) {
        _monobankToken.value = newToken
    }

    fun toggleMonobankInput(visible: Boolean) {
        _isMonobankInputVisible.value = visible
    }

    fun askDeleteConfirmation() {
        _showDeleteConfirmation.value = true
    }

    fun dismissDeleteConfirmation() {
        _showDeleteConfirmation.value = false
    }

    fun removeMonobankIntegration() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                settingsRepository.saveMonobankApiToken(null)
                settingsRepository.saveSelectedAccountIds(emptyList())
                financeRepository.deleteMonobankAccounts()

                _isBankConnected.value = false
                _availableAccounts.value = emptyList()
                _selectedBank.value = null
                _events.emit(IntegrationsUiEvent.ShowToast("Банк відключено"))
            } finally {
                _isLoading.value = false
                _showDeleteConfirmation.value = false
            }
        }
    }
}