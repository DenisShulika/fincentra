package com.denisshulika.fincentra.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.util.FinancialPredictions
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
    private val settingsRepository = DependencyProvider.settingsRepository

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _dailyPrediction = MutableStateFlow("")
    val dailyPrediction = _dailyPrediction.asStateFlow()

    private val _isCoinFlipped = MutableStateFlow(false)
    val isCoinFlipped = _isCoinFlipped.asStateFlow()

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

    val displayCurrency = settingsRepository.getDisplayCurrencyFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 980
        )

    fun setDisplayCurrency(code: Int) {
        viewModelScope.launch {
            settingsRepository.saveDisplayCurrency(code)
        }
    }

    fun checkDailyPrediction() {
        val lastFlipDate = prefs.getString("last_flip_date", "")
        val today = java.time.LocalDate.now().toString()

        if (lastFlipDate == today) {
            _dailyPrediction.value = prefs.getString("last_prediction", "") ?: ""
            _isCoinFlipped.value = true
        } else {
            _isCoinFlipped.value = false
            _dailyPrediction.value = FinancialPredictions.random()
        }
    }

    fun flipCoin() {
        val today = java.time.LocalDate.now().toString()
        prefs.edit()
            .putString("last_flip_date", today)
            .putString("last_prediction", _dailyPrediction.value)
            .apply()
        _isCoinFlipped.value = true
    }
}