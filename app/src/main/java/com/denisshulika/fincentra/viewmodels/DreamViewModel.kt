package com.denisshulika.fincentra.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.models.domain.Dream
import com.denisshulika.fincentra.data.models.domain.DreamProgress
import com.denisshulika.fincentra.data.util.TransactionConstants
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DreamViewModel : ViewModel() {
    private val financeRepository = DependencyProvider.financeRepository
    private val settingsRepository = DependencyProvider.settingsRepository

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _target = MutableStateFlow("")
    val target = _target.asStateFlow()

    private val _buffer = MutableStateFlow("")
    val buffer = _buffer.asStateFlow()

    private val _emoji = MutableStateFlow("🚀")
    val emoji = _emoji.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _selectedCurrency = MutableStateFlow(980)
    val selectedCurrency = _selectedCurrency.asStateFlow()

    val dreamProgress: StateFlow<DreamProgress?> = combine(
        financeRepository.dream,
        financeRepository.accounts,
        financeRepository.transactions,
        settingsRepository.getSelectedAccountIdsFlow(),
    ) { dream, accounts, transactions, selectedIds ->
        if (dream == null) return@combine null

        val rates = try {
            DependencyProvider.currencyRepository.getRates()
        } catch (e: Exception) {
            emptyMap<Int, Double>()
        }

        val effectiveIds = if (selectedIds.isEmpty()) accounts.map { it.id } else selectedIds

        val totalBankInDreamCurrency = accounts
            .filter { effectiveIds.contains(it.id) }
            .sumOf { acc ->
                val converted = DependencyProvider.currencyRepository.convert(
                    acc.balance,
                    acc.currencyCode,
                    dream.currencyCode,
                    rates
                )
                converted ?: if (acc.currencyCode == dream.currencyCode) acc.balance else 0.0
            }

        val totalCashInDreamCurrency = transactions
            .filter { it.accountId == TransactionConstants.ACCOUNT_ID_MANUAL }
            .groupBy { it.currencyCode }
            .map { (code, manualTxs) ->
                val sum = manualTxs.sumOf { if (it.isExpense) -it.amount else it.amount }
                val converted = DependencyProvider.currencyRepository.convert(
                    sum,
                    code,
                    dream.currencyCode,
                    rates
                )
                converted ?: if (code == dream.currencyCode) sum else 0.0
            }.sum()

        val totalAvailable = totalBankInDreamCurrency + totalCashInDreamCurrency

        val availableForDream = (totalAvailable - dream.safetyBuffer).coerceAtLeast(0.0)

        val progress = if (dream.targetAmount > 0)
            (availableForDream / dream.targetAmount).toFloat().coerceIn(0f, 1f)
        else 0f

        DreamProgress(dream, availableForDream, progress)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun onTitleChange(v: String) {
        _title.value = v
    }

    fun onTargetChange(v: String) {
        _target.value = v
    }

    fun onBufferChange(v: String) {
        _buffer.value = v
    }

    fun onEmojiChange(v: String) {
        if (v.length <= 8) {
            _emoji.value = v
        }
    }

    fun onCurrencyChange(code: Int) {
        _selectedCurrency.value = code
    }

    fun prepareForEdit(dream: Dream) {
        _title.value = dream.title
        _target.value = dream.targetAmount.toInt().toString()
        _buffer.value = dream.safetyBuffer.toInt().toString()
        _emoji.value = dream.iconEmoji.ifBlank { "🚀" }
        _selectedCurrency.value = dream.currencyCode
    }

    fun resetForm() {
        _title.value = ""
        _target.value = ""
        _buffer.value = ""
        _emoji.value = "🚀"
        _selectedCurrency.value = 980
    }

    fun deleteDream() {
        viewModelScope.launch {
            _isLoading.value = true
            settingsRepository.deleteDream()
            resetForm()
            _isLoading.value = false
        }
    }

    fun updateDream() {
        val titleVal = _title.value
        val targetVal = _target.value.toDoubleOrNull() ?: 0.0
        val bufferVal = _buffer.value.toDoubleOrNull() ?: 0.0
        val emojiVal = _emoji.value
        val currencyVal = _selectedCurrency.value

        viewModelScope.launch {
            _isLoading.value = true
            val newDream = Dream(
                title = titleVal,
                targetAmount = targetVal,
                safetyBuffer = bufferVal,
                currencyCode = currencyVal,
                iconEmoji = emojiVal
            )
            settingsRepository.saveDream(newDream)
            _isLoading.value = false
        }
    }
}