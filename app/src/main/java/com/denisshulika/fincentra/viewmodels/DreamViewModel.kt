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

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val dreamProgress: StateFlow<DreamProgress?> = combine(
        financeRepository.dream,
        financeRepository.accounts,
        financeRepository.transactions,
        settingsRepository.getSelectedAccountIdsFlow()
    ) { dream, accounts, transactions, selectedIds ->
        if (dream == null) return@combine null

        val baseAccounts = if (selectedIds.isNotEmpty()) {
            accounts.filter { selectedIds.contains(it.id) }
        } else {
            accounts
        }

        val bankBalance = baseAccounts
            .filter { it.currencyCode == dream.currencyCode }
            .sumOf { it.balance }

        val cashBalance = transactions
            .filter { it.accountId == TransactionConstants.ACCOUNT_ID_MANUAL && it.currencyCode == dream.currencyCode }
            .sumOf { if (it.isExpense) -it.amount else it.amount }

        val totalBalance = bankBalance + cashBalance

        val available = (totalBalance - dream.safetyBuffer).coerceAtLeast(0.0)

        val progress = if (dream.targetAmount > 0) {
            (available / dream.targetAmount).toFloat().coerceIn(0f, 1f)
        } else 0f

        DreamProgress(dream, available, progress)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateDream(title: String, target: Double, buffer: Double, currencyCode: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val newDream = Dream(
                title = title,
                targetAmount = target,
                safetyBuffer = buffer,
                currencyCode = currencyCode
            )
            settingsRepository.saveDream(newDream)
            _isLoading.value = false
        }
    }
}