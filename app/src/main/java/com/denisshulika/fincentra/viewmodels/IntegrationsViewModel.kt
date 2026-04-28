package com.denisshulika.fincentra.viewmodels

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.models.domain.BankAccount
import com.denisshulika.fincentra.data.models.events.IntegrationsUiEvent
import com.denisshulika.fincentra.data.models.ui.BankProviderInfo
import com.denisshulika.fincentra.data.models.ui.EuropeanDemoBanks
import com.denisshulika.fincentra.data.models.ui.EuropeanProvider
import com.denisshulika.fincentra.data.repository.SaltEdgeRepository
import com.denisshulika.fincentra.data.util.BankProviders
import com.denisshulika.fincentra.data.util.DeepLinkHandler
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IntegrationsViewModel : ViewModel() {
    private val settingsRepository = DependencyProvider.settingsRepository
    private val financeRepository = DependencyProvider.financeRepository
    private val monobankService = DependencyProvider.monobankProvider

    private val _isMonoLoading = MutableStateFlow(false)
    val isMonoLoading = _isMonoLoading.asStateFlow()

    private val _isEuroLoading = MutableStateFlow(false)
    val isEuroLoading = _isEuroLoading.asStateFlow()

    private val _showAccountSelection = MutableStateFlow(false)
    val showAccountSelection = _showAccountSelection.asStateFlow()

    private val _selectedIdsInUi = MutableStateFlow<Set<String>>(emptySet())

    private val _availableAccounts = MutableStateFlow<List<BankAccount>>(emptyList())
    val availableAccounts = combine(
        financeRepository.accounts,
        _selectedIdsInUi
    ) { accounts, uiSelectedIds ->
        accounts.map { acc ->
            acc.copy(selected = uiSelectedIds.contains(acc.id))
        }.sortedBy { it.name }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    private val saltEdgeRepo = SaltEdgeRepository()

    private val _isEuropeanMode = MutableStateFlow(false)
    val isEuropeanMode = _isEuropeanMode.asStateFlow()

    private val _europeanBanks = MutableStateFlow(EuropeanDemoBanks)
    val europeanBanks = _europeanBanks.asStateFlow()

    private val _loadingBankId = MutableStateFlow<String?>(null)
    val loadingBankId = _loadingBankId.asStateFlow()

    private var currentConnectingProviderId: String? = null

    private val _monoSyncStatus = MutableStateFlow("")
    val monoSyncStatus = _monoSyncStatus.asStateFlow()

    private val _monoSyncProgress = MutableStateFlow(0f)
    val monoSyncProgress = _monoSyncProgress.asStateFlow()

    private val _euroSyncStatus = MutableStateFlow("")
    val euroSyncStatus = _euroSyncStatus.asStateFlow()

    private val _euroSyncProgress = MutableStateFlow(0f)
    val euroSyncProgress = _euroSyncProgress.asStateFlow()

    private val _isWalletEnabled = MutableStateFlow(false)
    val isWalletEnabled = _isWalletEnabled.asStateFlow()

    val isWalletUserEnabled = settingsRepository.isWalletSyncEnabledFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleWalletSync(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveWalletSyncEnabled(enabled)
        }
    }

    init {
        viewModelScope.launch {
            val initialIds = settingsRepository.getSelectedAccountIds().toSet()
            _selectedIdsInUi.value = initialIds

            refreshConnectionStatus()

            DeepLinkHandler.authSuccessEvent.collect {
                onEuropeanBankRedirected()
            }
        }
    }

    private suspend fun getMappedAccounts(): List<BankAccount> {
        val savedAccounts = financeRepository.getAccountsOnce()
        val selectedIds = settingsRepository.getSelectedAccountIds()
        return savedAccounts.map { acc ->
            acc.copy(selected = selectedIds.contains(acc.id))
        }.sortedBy { it.id }
    }

    suspend fun refreshConnectionStatus() {
        val token = settingsRepository.getMonobankApiToken()
        _isBankConnected.value = !token.isNullOrBlank()
    }


    fun selectBank(bank: BankProviderInfo, context: Context) {
        if (bank.id == BankProviders.GOOGLE_WALLET) {
            checkWalletStatus(context)
        }

        _selectedBank.value = bank
        viewModelScope.launch {
            _availableAccounts.value = getMappedAccounts()
        }
    }

    fun closeBankDetails() {
        _selectedBank.value = null
    }

    fun syncMonobankData() {
        viewModelScope.launch {
            if (_isMonoLoading.value && _monoSyncStatus.value.isNotEmpty()) return@launch
            _isMonoLoading.value = true
            _monoSyncProgress.value = 0f
            var needsCooldown = false

            try {
                val token = settingsRepository.getMonobankApiToken() ?: return@launch

                _monoSyncStatus.value = "UPDATING_BALANCES"
                val actualAccounts = monobankService.fetchAccounts(token)
                needsCooldown = true

                if (actualAccounts.isNotEmpty()) {
                    financeRepository.saveAccounts(actualAccounts, updateSelection = false)
                }

                val selectedIds = settingsRepository.getSelectedAccountIds()
                val accountsToSync = actualAccounts.filter { selectedIds.contains(it.id) }

                if (accountsToSync.isEmpty()) {
                    _events.emit(IntegrationsUiEvent.ShowToast(R.string.error_no_accounts_selected))
                    delay(2000)
                } else {
                    for ((index, account) in accountsToSync.withIndex()) {
                        if (index > 0) {
                            waitForApiCooldown(60)
                        }

                        _monoSyncStatus.value = "SYNCING_ACC:${account.name}"

                        val lastSync = settingsRepository.getLastSyncTimestamp(account.id)
                        val fromTime =
                            if (lastSync == 0L) (System.currentTimeMillis() / 1000) - 2682000L else (lastSync / 1000 + 1)

                        monobankService.fetchTransactionsForAccount(
                            token = token,
                            accountId = account.id,
                            accountCurrency = account.currencyCode,
                            fromTimeSeconds = fromTime,
                            onProgress = { status ->
                                if (status.contains("Ліміт перевищено")) _monoSyncStatus.value =
                                    "LIMIT_EXCEEDED"
                                else if (status.contains("Пауза")) {
                                    val sec = status.filter { it.isDigit() }
                                    _monoSyncStatus.value = "PAUSE:$sec"
                                }
                            },
                            onBatchLoaded = { batch ->
                                financeRepository.addTransactionsBatch(batch)
                                settingsRepository.saveLastSyncTimestamp(
                                    account.id,
                                    batch.maxOf { it.timestamp }
                                )
                            }
                        )
                        _monoSyncProgress.value =
                            (index + 1).toFloat() / accountsToSync.size.toFloat()
                    }

                    settingsRepository.saveLastGlobalSyncTime(System.currentTimeMillis())
                    _monoSyncStatus.value = "DONE"
                    delay(2000)
                }
            } catch (e: Exception) {
                _monoSyncStatus.value = "API_ERROR"
                delay(2000)
            } finally {
                if (needsCooldown) {
                    _monoSyncProgress.value = 0f
                    waitForApiCooldown(60)
                }
                _monoSyncStatus.value = ""
                _isMonoLoading.value = false
            }
        }
    }

    private suspend fun waitForApiCooldown(seconds: Int) {
        for (i in seconds downTo 1) {
            _isMonoLoading.value = true
            _monoSyncStatus.value = "COOLDOWN:$i"
            delay(1000)
        }
    }

    fun refreshMonobankAccounts() {
        viewModelScope.launch {
            if (_isMonoLoading.value && _monoSyncStatus.value.isNotEmpty()) return@launch
            _isMonoLoading.value = true
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
                    _events.emit(IntegrationsUiEvent.ShowToast(R.string.success_updated))
                }
                waitForApiCooldown(60)
            } finally {
                _isMonoLoading.value = false
                _monoSyncStatus.value = ""
            }
        }
    }

    fun confirmMonobankSelection() {
        viewModelScope.launch {
            _isMonoLoading.value = true
            val allAccounts = financeRepository.getAccountsOnce()
            val monoAccountIds =
                allAccounts.filter { it.provider == BankProviders.MONOBANK }.map { it.id }

            val currentAllSelected = settingsRepository.getSelectedAccountIds().toMutableList()
            currentAllSelected.removeAll { monoAccountIds.contains(it) }
            currentAllSelected.addAll(_selectedIdsInUi.value.filter { id ->
                monoAccountIds.contains(
                    id
                )
            })

            settingsRepository.saveSelectedAccountIds(currentAllSelected)
            syncMonobankData()
        }
    }

    fun openAccountSettings() {
        viewModelScope.launch {
            _isMonoLoading.value = true
            try {
                _availableAccounts.value = getMappedAccounts()
            } finally {
                if (_monoSyncStatus.value.isEmpty()) {
                    _isMonoLoading.value = false
                }
            }
        }
    }

    fun connectMonobankAccount() {
        viewModelScope.launch {
            if (_isMonoLoading.value && _monoSyncStatus.value.isNotEmpty()) return@launch
            _isMonoLoading.value = true
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
                _events.emit(IntegrationsUiEvent.ShowToast(R.string.error_token_invalid))
            } finally {
                _isMonoLoading.value = false
            }
        }
    }

    fun openMonobankAuth() {
        viewModelScope.launch { _events.emit(IntegrationsUiEvent.OpenUrl("https://api.monobank.ua/")) }
    }

    fun toggleAccountSelection(id: String) {
        val current = _selectedIdsInUi.value.toMutableSet()
        if (current.contains(id)) current.remove(id) else current.add(id)
        _selectedIdsInUi.value = current
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
            _isMonoLoading.value = true
            try {
                settingsRepository.saveMonobankApiToken(null)
                settingsRepository.saveSelectedAccountIds(emptyList())
                financeRepository.deleteMonobankAccounts()

                _isBankConnected.value = false
                _availableAccounts.value = emptyList()
                _selectedBank.value = null
                _events.emit(IntegrationsUiEvent.ShowToast(R.string.success_updated))
            } finally {
                _isMonoLoading.value = false
                _showDeleteConfirmation.value = false
            }
        }
    }

    fun connectEuropeanBank(provider: EuropeanProvider) {
        viewModelScope.launch {
            _loadingBankId.value = provider.id
            settingsRepository.saveCurrentConnectingProviderId(provider.id)

            val userIdentifier = DependencyProvider.authRepository.getCurrentUser()?.uid ?: ""
            var customerId = settingsRepository.getSaltEdgeCustomerId()

            if (customerId == null) customerId = saltEdgeRepo.findCustomer(userIdentifier)
            if (customerId == null) customerId = saltEdgeRepo.createCustomer(userIdentifier)

            if (customerId != null) {
                settingsRepository.saveSaltEdgeCustomerId(customerId)
                val connectUrl = saltEdgeRepo.getConnectUrl(customerId, provider.providerCode)

                connectUrl?.let {
                    _events.emit(IntegrationsUiEvent.OpenUrl(it))
                }
            } else {
                _events.emit(IntegrationsUiEvent.ShowToast(R.string.error_unknown))
                _loadingBankId.value = null
            }
        }
    }

    fun confirmEuropeanSelection(providerId: String) {
        viewModelScope.launch {
            _isEuroLoading.value = true
            val allAccounts = financeRepository.getAccountsOnce()
            val euroAccountIds = allAccounts.filter { it.provider == providerId }.map { it.id }

            val currentAllSelected = settingsRepository.getSelectedAccountIds().toMutableList()
            currentAllSelected.removeAll { euroAccountIds.contains(it) }
            currentAllSelected.addAll(_selectedIdsInUi.value.filter { id ->
                euroAccountIds.contains(
                    id
                )
            })

            settingsRepository.saveSelectedAccountIds(currentAllSelected)
            _selectedBank.value = null
            _isEuroLoading.value = false
        }
    }

    fun onEuropeanBankRedirected() {
        viewModelScope.launch {
            _isEuroLoading.value = true
            _euroSyncStatus.value = "SYNCING_ASSETS"

            delay(3000)

            val customerId = settingsRepository.getSaltEdgeCustomerId() ?: return@launch
            val providerId = settingsRepository.getCurrentConnectingProviderId() ?: "unknown"

            val providerInfo = EuropeanDemoBanks.find { it.id == providerId }
            if (providerInfo != null) {
                _selectedBank.value = BankProviderInfo(
                    id = providerInfo.id,
                    name = providerInfo.name,
                    logo = R.drawable.ic_launcher_foreground,
                    brandColor = providerInfo.brandColor,
                    subtitle = providerInfo.countryName
                )
            }

            try {
                val remoteAccounts = saltEdgeRepo.fetchAccounts(customerId)
                val domainAccounts = remoteAccounts.map { json ->
                    BankAccount(
                        id = json.getString("id"),
                        provider = providerId,
                        name = json.optString("name", "Account"),
                        balance = json.optDouble("balance", 0.0),
                        currencyCode = mapCurrencyToIso(json.optString("currency_code")),
                        selected = false,
                        sourceType = "AGGREGATOR",
                        type = json.optString("nature", "account")
                    )
                }

                if (domainAccounts.isNotEmpty()) {
                    financeRepository.saveAccounts(domainAccounts)
                }
            } catch (e: Exception) {
                Log.e("SALT_SYNC", "Error: ${e.message}")
            } finally {
                _isEuroLoading.value = false
                _loadingBankId.value = null
                _euroSyncStatus.value = ""
                settingsRepository.saveCurrentConnectingProviderId(null)
            }
        }
    }

    fun startEuropeanAuth(providerId: String) {
        val provider = EuropeanDemoBanks.find { it.id == providerId } ?: return
        viewModelScope.launch {
            currentConnectingProviderId = provider.id
            _isEuroLoading.value = true

            val userIdentifier = DependencyProvider.authRepository.getCurrentUser()?.uid ?: ""
            var customerId = settingsRepository.getSaltEdgeCustomerId()

            if (customerId == null) customerId = saltEdgeRepo.findCustomer(userIdentifier)
            if (customerId == null) customerId = saltEdgeRepo.createCustomer(userIdentifier)

            if (customerId != null) {
                settingsRepository.saveSaltEdgeCustomerId(customerId)
                val connectUrl = saltEdgeRepo.getConnectUrl(customerId, provider.providerCode)
                if (connectUrl != null) {
                    _events.emit(IntegrationsUiEvent.OpenUrl(connectUrl))
                }
            } else {
                _events.emit(IntegrationsUiEvent.ShowToast(R.string.error_unknown))
            }
            _isEuroLoading.value = false
        }
    }

    fun toggleEuropeanMode(enabled: Boolean) {
        _isEuropeanMode.value = enabled
    }

    fun disconnectProvider(providerId: String) {
        val isMono = providerId == BankProviders.MONOBANK
        viewModelScope.launch {
            if (isMono) _isMonoLoading.value = true else _isEuroLoading.value = true
            try {
                val allAccounts = financeRepository.getAccountsOnce()
                val providerAccountIds =
                    allAccounts.filter { it.provider == providerId }.map { it.id }

                val currentSelectedIds = settingsRepository.getSelectedAccountIds().toMutableList()
                currentSelectedIds.removeAll { providerAccountIds.contains(it) }
                settingsRepository.saveSelectedAccountIds(currentSelectedIds)

                if (providerId == BankProviders.MONOBANK) {
                    settingsRepository.saveMonobankApiToken(null)
                    financeRepository.deleteMonobankAccounts()
                    _isBankConnected.value = false
                } else {
                    financeRepository.deleteAccountsByProvider(providerId)
                }

                _availableAccounts.value = getMappedAccounts()
                _selectedBank.value = null
                _events.emit(IntegrationsUiEvent.ShowToast(R.string.success_updated))
            } finally {
                if (isMono) _isMonoLoading.value = false else _isEuroLoading.value = false
                refreshConnectionStatus()
            }
        }
    }

    fun checkWalletStatus(context: Context) {
        val packageName = context.packageName
        val flat =
            Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        _isWalletEnabled.value = flat?.contains(packageName) == true
    }

    fun openNotificationSettings(context: Context) {
        context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
    }

    fun toggleWalletSync(enabled: Boolean, context: Context) {
        viewModelScope.launch {
            val packageName = context.packageName
            val flat =
                Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            val hasSystemAccess = flat?.contains(packageName) == true

            if (enabled && !hasSystemAccess) {
                _events.emit(IntegrationsUiEvent.ShowToast(R.string.error_permission_required))
                openNotificationSettings(context)
            } else {
                settingsRepository.saveWalletSyncEnabled(enabled)
                _isWalletEnabled.value = hasSystemAccess
            }
        }
    }

    private fun mapCurrencyToIso(code: String?): Int {
        return when (code?.uppercase()) {
            "UAH" -> 980
            "USD" -> 840
            "EUR" -> 978
            "RON" -> 946
            "PLN" -> 985
            "GBP" -> 826
            "HUF" -> 348
            "CZK" -> 203
            else -> 0
        }
    }
}