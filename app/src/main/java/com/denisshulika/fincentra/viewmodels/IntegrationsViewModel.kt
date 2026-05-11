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
    private val saltEdgeRepo = SaltEdgeRepository()
    private val wiseService = DependencyProvider.wiseService

    private val _isMonoLoading = MutableStateFlow(false)
    val isMonoLoading = _isMonoLoading.asStateFlow()

    private val _isEuroLoading = MutableStateFlow(false)
    val isEuroLoading = _isEuroLoading.asStateFlow()

    private val _isWiseLoading = MutableStateFlow(false)
    val isWiseLoading = _isWiseLoading.asStateFlow()

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

    private val _isMonobankInputVisible = MutableStateFlow(false)
    val isMonobankInputVisible = _isMonobankInputVisible.asStateFlow()

    private val _showAccountSelection = MutableStateFlow(false)
    val showAccountSelection = _showAccountSelection.asStateFlow()

    private val _monoSyncStatus = MutableStateFlow("")
    val monoSyncStatus = _monoSyncStatus.asStateFlow()

    private val _monoSyncProgress = MutableStateFlow(0f)
    val monoSyncProgress = _monoSyncProgress.asStateFlow()

    private val _euroSyncStatus = MutableStateFlow("")
    val euroSyncStatus = _euroSyncStatus.asStateFlow()

    private val _euroSyncProgress = MutableStateFlow(0f)
    val euroSyncProgress = _euroSyncProgress.asStateFlow()

    private val _wiseSyncStatus = MutableStateFlow("")
    val wiseSyncStatus = _wiseSyncStatus.asStateFlow()

    private val _wiseSyncProgress = MutableStateFlow(0f)
    val wiseSyncProgress = _wiseSyncProgress.asStateFlow()

    private val _monobankToken = MutableStateFlow("")
    val monobankToken = _monobankToken.asStateFlow()

    private val _wiseToken = MutableStateFlow("")
    val wiseToken = _wiseToken.asStateFlow()

    private val _selectedBank = MutableStateFlow<BankProviderInfo?>(null)
    val selectedBank = _selectedBank.asStateFlow()

    private val _events = MutableSharedFlow<IntegrationsUiEvent>()
    val events = _events.asSharedFlow()

    private val _isBankConnected = MutableStateFlow(false)
    val isBankConnected = _isBankConnected.asStateFlow()

    private val _isEuropeanMode = MutableStateFlow(false)
    val isEuropeanMode = _isEuropeanMode.asStateFlow()

    private val _europeanBanks = MutableStateFlow(EuropeanDemoBanks)
    val europeanBanks = _europeanBanks.asStateFlow()

    private val _loadingBankId = MutableStateFlow<String?>(null)
    val loadingBankId = _loadingBankId.asStateFlow()

    private val _isWalletEnabled = MutableStateFlow(false)
    val isWalletEnabled = _isWalletEnabled.asStateFlow()

    val isWalletUserEnabled = settingsRepository.isWalletSyncEnabledFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private var currentConnectingProviderId: String? = null

    private val _showDeleteConfirmation = MutableStateFlow(false)
    val showDeleteConfirmation = _showDeleteConfirmation.asStateFlow()

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

    fun dismissDeleteConfirmation() {
        _showDeleteConfirmation.value = false
    }

    fun toggleEuropeanMode(enabled: Boolean) {
        _isEuropeanMode.value = enabled
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

    suspend fun refreshConnectionStatus() {
        val monoToken = settingsRepository.getMonobankApiToken()
        val wiseToken = settingsRepository.getWiseApiToken()

        _isBankConnected.value = !monoToken.isNullOrBlank() || !wiseToken.isNullOrBlank()
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

    private suspend fun getMappedAccounts(): List<BankAccount> {
        val savedAccounts = financeRepository.getAccountsOnce()
        val selectedIds = settingsRepository.getSelectedAccountIds()
        return savedAccounts.map { acc ->
            acc.copy(selected = selectedIds.contains(acc.id))
        }.sortedBy { it.id }
    }

    fun closeBankDetails() {
        _selectedBank.value = null
    }

    fun toggleAccountSelection(id: String) {
        val current = _selectedIdsInUi.value.toMutableSet()
        if (current.contains(id)) current.remove(id) else current.add(id)
        _selectedIdsInUi.value = current
    }

    fun onMonobankTokenChange(v: String) {
        _monobankToken.value = v
    }

    private suspend fun waitForApiCooldown(seconds: Int) {
        for (i in seconds downTo 1) {
            _monoSyncStatus.value = "COOLDOWN:$i"
            _monoSyncProgress.value = (seconds - i).toFloat() / seconds.toFloat()
            delay(1000)
        }
        _monoSyncProgress.value = 1f
    }

    private suspend fun syncMonobankData() {
        if (_monoSyncStatus.value.isNotEmpty()) return

        _monoSyncProgress.value = 0f
        var needsCooldown = false

        try {
            val token = settingsRepository.getMonobankApiToken() ?: return
            _monoSyncStatus.value = "UPDATING_BALANCES"
            val actualAccounts = monobankService.fetchAccounts(token)
            needsCooldown = true

            if (actualAccounts.isNotEmpty()) {
                financeRepository.saveAccounts(actualAccounts, updateSelection = false)
            }

            val selectedIds = settingsRepository.getSelectedAccountIds()
            val accountsToSync = actualAccounts.filter { selectedIds.contains(it.id) }

            if (accountsToSync.isEmpty()) {
                _events.emit(IntegrationsUiEvent.ShowToast(R.string.integrations_view_model_error_no_accounts))
                delay(2000)
            } else {
                for ((index, account) in accountsToSync.withIndex()) {
                    if (index > 0) waitForApiCooldown(60)

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
                                batch.maxOf { it.timestamp })
                        }
                    )
                    _monoSyncProgress.value = (index + 1).toFloat() / accountsToSync.size.toFloat()
                }

                settingsRepository.saveLastGlobalSyncTime(System.currentTimeMillis())
                _monoSyncStatus.value = "DONE"
                delay(2000)
            }
        } catch (e: Exception) {
            _monoSyncStatus.value = "API_ERROR"
            delay(2000)
        } finally {
            if (needsCooldown) waitForApiCooldown(60)
            _monoSyncStatus.value = ""
            _monoSyncProgress.value = 0f
        }
    }

    fun connectMonobankAccount() {
        viewModelScope.launch {
            if (_isMonoLoading.value) return@launch
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
                _events.emit(IntegrationsUiEvent.ShowToast(R.string.integrations_view_model_error_token_invalid))
            } finally {
                _isMonoLoading.value = false
            }
        }
    }

    fun confirmMonobankSelection() {
        viewModelScope.launch {
            _isMonoLoading.value = true
            try {
                val monoAccountIds = financeRepository.getAccountsOnce()
                    .filter { it.provider == BankProviders.MONOBANK }.map { it.id }

                val selectedForMono = _selectedIdsInUi.value.filter { monoAccountIds.contains(it) }

                if (selectedForMono.isEmpty()) {
                    _events.emit(IntegrationsUiEvent.ShowToast(R.string.integrations_view_model_error_no_accounts))
                    return@launch
                }

                val allSelectedIds = settingsRepository.getSelectedAccountIds().toMutableList()
                allSelectedIds.removeAll { monoAccountIds.contains(it) }
                allSelectedIds.addAll(selectedForMono)

                settingsRepository.saveSelectedAccountIds(allSelectedIds)

                delay(300)

                syncMonobankData()
            } finally {
                _isMonoLoading.value = false
            }
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
                    _events.emit(IntegrationsUiEvent.ShowToast(R.string.integrations_view_model_success_updated))
                }
                waitForApiCooldown(60)
            } finally {
                _isMonoLoading.value = false
                _monoSyncStatus.value = ""
            }
        }
    }

    fun disconnectMonobank() {
        viewModelScope.launch {
            _isMonoLoading.value = true
            try {
                val allAccounts = financeRepository.getAccountsOnce()
                val monoAccountIds =
                    allAccounts.filter { it.provider == BankProviders.MONOBANK }.map { it.id }

                val currentSelectedIds = settingsRepository.getSelectedAccountIds().toMutableList()
                currentSelectedIds.removeAll { monoAccountIds.contains(it) }
                settingsRepository.saveSelectedAccountIds(currentSelectedIds)

                settingsRepository.saveMonobankApiToken(null)
                financeRepository.deleteMonobankAccounts()

                _isBankConnected.value = false
                _selectedIdsInUi.value = currentSelectedIds.toSet()
                _events.emit(IntegrationsUiEvent.ShowToast(R.string.integrations_view_model_success_updated))
            } finally {
                _isMonoLoading.value = false
                refreshConnectionStatus()
            }
        }
    }

    fun onWiseTokenChange(v: String) {
        _wiseToken.value = v
    }

    fun connectWiseAccount() {
        viewModelScope.launch {
            _isWiseLoading.value = true
            try {
                val token = _wiseToken.value.trim()
                val accounts = wiseService.fetchAccounts(token)
                if (accounts.isNotEmpty()) {
                    settingsRepository.saveWiseApiToken(token)
                    financeRepository.saveAccounts(accounts)

                    val currentIds = _selectedIdsInUi.value.toMutableSet()
                    accounts.forEach { currentIds.add(it.id) }
                    _selectedIdsInUi.value = currentIds
                    settingsRepository.saveSelectedAccountIds(currentIds.toList())

                    syncWiseData(token, accounts)

                    _events.emit(IntegrationsUiEvent.ShowToast(R.string.integrations_view_model_success_updated))
                    _wiseToken.value = ""
                }
            } catch (e: Exception) {
                _events.emit(IntegrationsUiEvent.ShowToast(R.string.integrations_view_model_error_token_invalid))
            } finally {
                _isWiseLoading.value = false
            }
        }
    }

    fun confirmWiseSelection() {
        viewModelScope.launch {
            _isWiseLoading.value = true
            val currentList = availableAccounts.value

            val selectedWiseIds = currentList
                .filter { it.provider == BankProviders.WISE && it.selected }
                .map { it.id }

            val allSelectedIds = settingsRepository.getSelectedAccountIds().toMutableList()
            val allAccountsInDb = financeRepository.getAccountsOnce()
            val oldWiseIds =
                allAccountsInDb.filter { it.provider == BankProviders.WISE }.map { it.id }

            allSelectedIds.removeAll { oldWiseIds.contains(it) }
            allSelectedIds.addAll(selectedWiseIds)

            settingsRepository.saveSelectedAccountIds(allSelectedIds)
            financeRepository.saveAccounts(currentList, updateSelection = true)

            _isWiseLoading.value = false
        }
    }

    fun toggleWalletSync(enabled: Boolean, context: Context) {
        viewModelScope.launch {
            val packageName = context.packageName
            val flat =
                Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            val hasSystemAccess = flat?.contains(packageName) == true

            if (enabled && !hasSystemAccess) {
                _events.emit(IntegrationsUiEvent.ShowToast(R.string.integrations_view_model_error_permission_required))
                openNotificationSettings(context)
            } else {
                settingsRepository.saveWalletSyncEnabled(enabled)
                _isWalletEnabled.value = hasSystemAccess
            }
        }
    }

    private suspend fun syncWiseData(token: String, accounts: List<BankAccount>) {
        _wiseSyncStatus.value = "SYNCING_WISE"
        accounts.forEachIndexed { index, acc ->
            wiseService.fetchTransactionsForAccount(
                token = token,
                accountId = acc.id,
                accountCurrency = acc.currencyCode,
                fromTimeSeconds = 0,
                onProgress = {},
                onBatchLoaded = { batch ->
                    financeRepository.addTransactionsBatch(batch)
                }
            )
            _wiseSyncProgress.value = (index + 1).toFloat() / accounts.size.toFloat()
        }
        _wiseSyncStatus.value = "DONE"
        delay(1500)
        _wiseSyncStatus.value = ""
    }

    fun disconnectWise() {
        viewModelScope.launch {
            _isWiseLoading.value = true
            try {
                val allAccounts = financeRepository.getAccountsOnce()
                val wiseAccountIds =
                    allAccounts.filter { it.provider == BankProviders.WISE }.map { it.id }

                val currentSelectedIds = settingsRepository.getSelectedAccountIds().toMutableList()
                currentSelectedIds.removeAll { wiseAccountIds.contains(it) }
                settingsRepository.saveSelectedAccountIds(currentSelectedIds)

                settingsRepository.saveWiseApiToken(null)
                financeRepository.deleteAccountsByProvider(BankProviders.WISE)

                _selectedIdsInUi.value = currentSelectedIds.toSet()
                _events.emit(IntegrationsUiEvent.ShowToast(R.string.integrations_view_model_success_updated))
            } finally {
                _isWiseLoading.value = false
                refreshConnectionStatus()
            }
        }
    }

    fun connectEuropeanBank(provider: EuropeanProvider) {
        viewModelScope.launch {
            _isEuroLoading.value = true
            _loadingBankId.value = provider.id
            settingsRepository.saveCurrentConnectingProviderId(provider.id)
            val userIdentifier = DependencyProvider.authRepository.getCurrentUser()?.uid ?: ""
            var customerId =
                settingsRepository.getSaltEdgeCustomerId() ?: saltEdgeRepo.findCustomer(
                    userIdentifier
                ) ?: saltEdgeRepo.createCustomer(userIdentifier)

            if (customerId != null) {
                settingsRepository.saveSaltEdgeCustomerId(customerId)
                saltEdgeRepo.getConnectUrl(customerId, provider.providerCode)?.let {
                    _events.emit(IntegrationsUiEvent.OpenUrl(it))
                }
            }
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
                    nameRes = providerInfo.nameRes,
                    logo = R.drawable.ic_launcher_foreground,
                    brandColor = providerInfo.brandColor,
                    subtitleRes = providerInfo.countryNameRes
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
            _isEuroLoading.value = false
        }
    }

    fun disconnectEuropeanBank(providerId: String) {
        viewModelScope.launch {
            _isEuroLoading.value = true
            try {
                val allAccounts = financeRepository.getAccountsOnce()
                val providerAccountIds =
                    allAccounts.filter { it.provider == providerId }.map { it.id }

                val currentSelectedIds = settingsRepository.getSelectedAccountIds().toMutableList()
                currentSelectedIds.removeAll { providerAccountIds.contains(it) }
                settingsRepository.saveSelectedAccountIds(currentSelectedIds)

                financeRepository.deleteAccountsByProvider(providerId)

                _selectedIdsInUi.value = currentSelectedIds.toSet()
                _events.emit(IntegrationsUiEvent.ShowToast(R.string.integrations_view_model_success_updated))
            } finally {
                _isEuroLoading.value = false
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
}