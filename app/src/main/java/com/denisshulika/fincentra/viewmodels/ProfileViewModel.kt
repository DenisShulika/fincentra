package com.denisshulika.fincentra.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.models.domain.User
import com.denisshulika.fincentra.data.models.state.CurrencySummary
import com.denisshulika.fincentra.data.util.TransactionConstants
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val financeRepository = DependencyProvider.financeRepository
    private val authRepository = DependencyProvider.authRepository
    private val settingsRepository = DependencyProvider.settingsRepository

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _currencySummaries = MutableStateFlow<List<CurrencySummary>>(emptyList())
    val currencySummaries = _currencySummaries.asStateFlow()

    private val _totalTransactionsCount = MutableStateFlow(0)
    val totalTransactionsCount = _totalTransactionsCount.asStateFlow()

    private val _selectedIds = MutableStateFlow<List<String>>(emptyList())

    init {
        loadUserData()

        viewModelScope.launch {
            settingsRepository.getSelectedAccountIdsFlow().collect { ids ->
                _selectedIds.value = ids
            }
        }

        observeStats()

        viewModelScope.launch {
            combine(financeRepository.accounts, _selectedIds) { accounts, selectedIds ->
                accounts
                    .filter { selectedIds.contains(it.id) }
                    .groupBy { it.currencyCode }
                    .map { (code, list) ->
                        CurrencySummary(code, list.sumOf { it.balance })
                    }
                    .sortedByDescending { it.currencyCode == 980 }
            }.collect { summaries ->
                _currencySummaries.value = summaries
            }
        }
    }

    private fun loadUserData() {
        _user.value = authRepository.getCurrentUser()
    }

    private fun observeStats() {
        viewModelScope.launch {
            combine(financeRepository.transactions, _selectedIds) { transactions, selectedIds ->
                transactions.filter { tx ->
                    tx.accountId == TransactionConstants.ACCOUNT_ID_MANUAL ||
                            selectedIds.contains(tx.accountId)
                }.size
            }.collect { count ->
                _totalTransactionsCount.value = count
            }
        }
    }

    fun getSupportIntent(): android.content.Intent {
        return android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:denisshulika31@gmail.com")
            putExtra(android.content.Intent.EXTRA_SUBJECT, "FinCentra Feedback")
        }
    }
}