package com.denisshulika.fincentra.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.models.domain.User
import com.denisshulika.fincentra.data.models.state.CurrencySummary
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val financeRepository = DependencyProvider.financeRepository
    private val authRepository = DependencyProvider.authRepository

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _currencySummaries = MutableStateFlow<List<CurrencySummary>>(emptyList())
    val currencySummaries = _currencySummaries.asStateFlow()

    private val _totalTransactionsCount = MutableStateFlow(0)
    val totalTransactionsCount = _totalTransactionsCount.asStateFlow()

    private val _provider = MutableStateFlow("")
    val provider = _provider.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadUserData()
        observeStats()

        viewModelScope.launch {
            financeRepository.accounts.collect { accounts ->
                val summaries = accounts
                    .filter { it.selected }
                    .groupBy { it.currencyCode }
                    .map { (code, list) ->
                        CurrencySummary(code, list.sumOf { it.balance })
                    }
                    .sortedByDescending { it.currencyCode == 980 }
                _currencySummaries.value = summaries
            }
        }
    }

    private fun loadUserData() {
        val currentUser = authRepository.getCurrentUser()
        _user.value = currentUser
        _provider.value = authRepository.getSignInProvider()
    }

    private fun observeStats() {
        viewModelScope.launch {
            financeRepository.transactions.collect { list ->
                _totalTransactionsCount.value = list.size
            }
        }
    }

    fun changePassword(newPass: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.updatePassword(newPass)
            _isLoading.value = false

            result.onSuccess { onComplete("Пароль змінено") }
                .onFailure { onComplete("Помилка: ${it.message}. Можливо, потрібно перелогінитись.") }
        }
    }

    fun deleteAccount(onDeleted: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            financeRepository.clearAllData()

            val result = authRepository.deleteUserAccount()
            _isLoading.value = false

            result.onSuccess { onDeleted() }
                .onFailure { onError("Для видалення потрібно заново увійти в акаунт (вимоги безпеки)") }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            DependencyProvider.financeRepository.clearAllData()
            onSuccess()
        }
    }

    fun getSupportIntent(): android.content.Intent {
        return android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:denisshulika31@gmail.com")
            putExtra(android.content.Intent.EXTRA_SUBJECT, "FinCentra Feedback")
        }
    }
}