package com.denisshulika.fincentra.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.models.events.AuthUiEvent
import com.denisshulika.fincentra.di.DependencyProvider
import com.google.firebase.auth.GoogleAuthProvider
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

    private val _nameError = MutableStateFlow<String?>(null)
    val nameError = _nameError.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError = _passwordError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
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
            _emailError.value = "Введіть Email"; return
        }
        if (_password.value.isBlank()) {
            _passwordError.value = "Введіть пароль"; return
        }

        viewModelScope.launch {
            _isLoading.value = true
            DependencyProvider.repository.clearAllData()
            val result = authRepository.signInWithEmail(_email.value, _password.value)
            _isLoading.value = false

            result.onSuccess {
                DependencyProvider.repository.observeUserTransactions()
                _events.emit(AuthUiEvent.NavigateToMain)
            }.onFailure {
                _events.emit(AuthUiEvent.ShowError("Неправильна пошта або пароль"))
            }
        }
    }

    fun signUp() {
        var isValid = true
        if (_name.value.isBlank()) {
            _nameError.value = "Як вас звати?"; isValid = false
        }
        if (!validateEmail(_email.value)) {
            _emailError.value = "Невірний формат пошти"; isValid = false
        }
        if (_password.value.length < 6) {
            _passwordError.value = "Мінімум 6 символів"; isValid = false
        }
        if (_password.value != _confirmPassword.value) {
            _confirmPasswordError.value = "Паролі не збігаються"; isValid = false
        }

        if (!isValid) return

        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.signUpWithEmail(_email.value, _password.value, _name.value)
            _isLoading.value = false

            result.onSuccess {
                DependencyProvider.repository.observeUserTransactions()
                _events.emit(AuthUiEvent.NavigateToMain)
            }.onFailure {
                _events.emit(AuthUiEvent.ShowError(it.message ?: "Помилка реєстрації"))
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isGoogleLoading.value = true
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = authRepository.signInWithGoogle(credential)

            result.onSuccess {
                DependencyProvider.repository.observeUserTransactions()
                _events.emit(AuthUiEvent.NavigateToMain)
            }.onFailure {
                _events.emit(AuthUiEvent.ShowError("Помилка входу через Google"))
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
                _events.emit(AuthUiEvent.ShowError("Пароль успішно змінено"))
            }.onFailure {
                _events.emit(AuthUiEvent.ShowError("Помилка: ${it.message}"))
            }
        }
    }

    fun deleteAccount(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            DependencyProvider.repository.clearAllData()

            val result = authRepository.deleteUserAccount()
            _isLoading.value = false

            result.onSuccess {
                onDeleted()
            }.onFailure {
                _events.emit(AuthUiEvent.ShowError("Для видалення потрібно перезайти в додаток"))
            }
        }
    }
}