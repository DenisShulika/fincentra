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

    val dreamProgress: StateFlow<DreamProgress?> = combine(
        financeRepository.dream,
        financeRepository.accounts,
        financeRepository.transactions,
        settingsRepository.getSelectedAccountIdsFlow()
    ) { dream, accounts, transactions, selectedIds ->
        if (dream == null) return@combine null

        val trackedBankBalance = accounts
            .filter { selectedIds.contains(it.id) && it.currencyCode == dream.currencyCode }
            .sumOf { it.balance }

        val cashBalance = transactions
            .filter { it.accountId == TransactionConstants.ACCOUNT_ID_MANUAL && it.currencyCode == dream.currencyCode }
            .sumOf { if (it.isExpense) -it.amount else it.amount }

        val total = trackedBankBalance + cashBalance
        val available = (total - dream.safetyBuffer).coerceAtLeast(0.0)
        val progress = if (dream.targetAmount > 0) (available / dream.targetAmount).toFloat()
            .coerceIn(0f, 1f) else 0f

        DreamProgress(dream, available, progress)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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
        if (v.length <= 1) _emoji.value = v
    }

    fun prepareForEdit(dream: Dream) {
        _title.value = dream.title
        _target.value = dream.targetAmount.toInt().toString()
        _buffer.value = dream.safetyBuffer.toInt().toString()
        _emoji.value = dream.iconEmoji.ifBlank { "🚀" }
    }

    fun resetForm() {
        _title.value = ""
        _target.value = ""
        _buffer.value = ""
        _emoji.value = "🚀"
    }

    fun deleteDream() {
        viewModelScope.launch {
            _isLoading.value = true
            settingsRepository.deleteDream()
            resetForm()
            _isLoading.value = false
        }
    }

    fun updateDream(currencyCode: Int) {
        val titleVal = _title.value
        val targetVal = _target.value.toDoubleOrNull() ?: 0.0
        val bufferVal = _buffer.value.toDoubleOrNull() ?: 0.0
        val emojiVal = _emoji.value

        viewModelScope.launch {
            _isLoading.value = true
            val newDream = Dream(
                title = titleVal,
                targetAmount = targetVal,
                safetyBuffer = bufferVal,
                currencyCode = currencyCode,
                iconEmoji = emojiVal
            )
            settingsRepository.saveDream(newDream)
            _isLoading.value = false
        }
    }
}