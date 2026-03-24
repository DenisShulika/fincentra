package com.denisshulika.fincentra.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.util.LanguageManager
import com.denisshulika.fincentra.di.DependencyProvider
import com.denisshulika.fincentra.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    private val authRepository = DependencyProvider.authRepository
    private val financeRepository = DependencyProvider.financeRepository

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _appTheme = MutableStateFlow(
        AppTheme.valueOf(prefs.getString("app_theme", AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name)
    )
    val appTheme = _appTheme.asStateFlow()

    val isDarkMode: StateFlow<Boolean> = _appTheme.map { theme ->
        when (theme) {
            AppTheme.DARK -> true
            AppTheme.LIGHT, AppTheme.NEUTRAL -> false
            AppTheme.SYSTEM -> false
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, _appTheme.value == AppTheme.DARK)

    fun setLanguage(langCode: String) {
        LanguageManager.setLanguage(langCode)
    }

    fun setTheme(theme: AppTheme) {
        _appTheme.value = theme
        prefs.edit().putString("app_theme", theme.name).apply()
    }

    fun changePassword(newPass: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.updatePassword(newPass)
            _isLoading.value = false
            result.onSuccess { onComplete("SUCCESS_PASSWORD") }
                .onFailure { onComplete("ERROR_REAUTH") }
        }
    }

    fun deleteAccount(onDeleted: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            financeRepository.clearAllData()
            val result = authRepository.deleteUserAccount()
            _isLoading.value = false
            result.onSuccess { onDeleted() }
                .onFailure { onError("ERROR_REAUTH") }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            financeRepository.clearAllData()
            authRepository.signOut()
            onSuccess()
        }
    }
}