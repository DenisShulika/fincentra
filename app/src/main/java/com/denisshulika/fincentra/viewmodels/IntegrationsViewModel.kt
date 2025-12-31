package com.denisshulika.fincentra.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.network.MonobankService
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IntegrationsViewModel : ViewModel() {
    private val repository = DependencyProvider.repository
    private val monoService = MonobankService()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isBankConnected = MutableStateFlow(false)
    val isBankConnected: StateFlow<Boolean> = _isBankConnected.asStateFlow()

    private val _monoToken = MutableStateFlow("")
    val monoToken: StateFlow<String> = _monoToken.asStateFlow()

    private val _isMonoInputVisible = MutableStateFlow(false)
    val isMonoInputVisible: StateFlow<Boolean> = _isMonoInputVisible.asStateFlow()

    private val _openUrlEvent = MutableSharedFlow<String>()
    val openUrlEvent = _openUrlEvent.asSharedFlow()

    init {
        checkConnectionStatus()
    }

    private fun checkConnectionStatus() {
        viewModelScope.launch {
            val token = repository.getMonoToken()
            _isBankConnected.value = !token.isNullOrBlank()
        }
    }

    fun saveMonoToken() {
        viewModelScope.launch {
            if (_monoToken.value.isNotBlank()) {
                repository.saveMonoToken(_monoToken.value)
                _isBankConnected.value = true
                _isMonoInputVisible.value = false
                _monoToken.value = ""
            }
        }
    }

    fun syncMonobank() {
        viewModelScope.launch {
            _isLoading.value = true
            val token = repository.getMonoToken()
            if (!token.isNullOrBlank()) {
                val newTransactions = monoService.fetchAllTransactions(token)
                newTransactions.forEach { repository.addTransaction(it) }
            }
            _isLoading.value = false
        }
    }

    fun disconnectBank() {
        viewModelScope.launch {
            repository.saveMonoToken("")
            _isBankConnected.value = false
        }
    }

    fun openMonobankAuth() {
        viewModelScope.launch {
            _events.emit("https://api.monobank.ua/")
        }
    }

    fun onTokenChange(newToken: String) { _monoToken.value = newToken }
    fun toggleMonoInput(visible: Boolean) { _isMonoInputVisible.value = visible }
}