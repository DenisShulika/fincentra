package com.denisshulika.fincentra.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.models.events.AuthUiEvent
import com.denisshulika.fincentra.di.DependencyProvider
import com.denisshulika.fincentra.di.DependencyProvider.financeRepository
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = DependencyProvider.authRepository

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _events = MutableSharedFlow<AuthUiEvent>()
    val events = _events.asSharedFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword = _confirmPassword.asStateFlow()

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _nameError = MutableStateFlow<Int?>(null)
    val nameError = _nameError.asStateFlow()

    private val _emailError = MutableStateFlow<Int?>(null)
    val emailError = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<Int?>(null)
    val passwordError = _passwordError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow<Int?>(null)
    val confirmPasswordError = _confirmPasswordError.asStateFlow()

    private val _isGoogleLoading = MutableStateFlow(false)
    val isGoogleLoading = _isGoogleLoading.asStateFlow()

    fun onNameChange(v: String) {
        _name.value = v; _nameError.value = null
    }

    private fun validateEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$".toRegex()
        return email.matches(emailRegex)
    }

    fun signIn() {
        if (_email.value.isBlank()) {
            _emailError.value = R.string.auth_view_model_error_email_required; return
        }
        if (_password.value.isBlank()) {
            _passwordError.value = R.string.auth_view_model_error_password_required; return
        }

        viewModelScope.launch {
            _isLoading.value = true

            financeRepository.clearAllData()

            val result = authRepository.signInWithEmail(_email.value, _password.value)

            result.onSuccess {
                financeRepository.refreshUser()

                delay(800)

                _events.emit(AuthUiEvent.NavigateToMain)
            }.onFailure {
                _events.emit(AuthUiEvent.ShowError(R.string.auth_view_model_error_invalid_credentials))
            }
            _isLoading.value = false
        }
    }

    fun signUp() {
        var isValid = true
        if (_name.value.isBlank()) {
            _nameError.value = R.string.auth_view_model_error_name_required
            isValid = false
        }
        if (!validateEmail(_email.value)) {
            _emailError.value = R.string.auth_view_model_error_invalid_email; isValid = false
        }
        if (_password.value.length < 6) {
            _passwordError.value = R.string.auth_view_model_error_short_password; isValid = false
        }
        if (_password.value != _confirmPassword.value) {
            _confirmPasswordError.value =
                R.string.auth_view_model_error_passwords_dont_match; isValid = false
        }

        if (!isValid) return

        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.signUpWithEmail(_email.value, _password.value, _name.value)
            _isLoading.value = false

            result.onSuccess {
                _events.emit(AuthUiEvent.NavigateToMain)
            }.onFailure {
                _events.emit(AuthUiEvent.ShowError(R.string.auth_view_model_error_unknown))
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isGoogleLoading.value = true
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = authRepository.signInWithGoogle(credential)

            result.onSuccess {
                _events.emit(AuthUiEvent.NavigateToMain)
            }.onFailure {
                _events.emit(AuthUiEvent.ShowError(R.string.auth_view_model_error_google_failed))
            }
            _isGoogleLoading.value = false
        }
    }

    fun setGoogleLoading(loading: Boolean) {
        _isGoogleLoading.value = loading
    }

    fun onConfirmPasswordChange(newValue: String) {
        _confirmPassword.value = newValue
    }

    fun onEmailChange(newValue: String) {
        _email.value = newValue
    }

    fun onPasswordChange(newValue: String) {
        _password.value = newValue
    }

    fun getProvider() = authRepository.getSignInProvider()

    fun changePassword(newPass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.updatePassword(newPass)
            _isLoading.value = false

            result.onSuccess {
                _events.emit(AuthUiEvent.ShowError(R.string.auth_view_model_success_password_changed))
            }.onFailure {
                _events.emit(AuthUiEvent.ShowError(R.string.auth_view_model_error_unknown))
            }
        }
    }

    fun deleteAccount(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            financeRepository.clearAllData()

            val result = authRepository.deleteUserAccount()
            _isLoading.value = false

            result.onSuccess {
                onDeleted()
            }.onFailure {
                _events.emit(AuthUiEvent.ShowError(R.string.auth_view_model_error_relogin_required))
            }
        }
    }
}