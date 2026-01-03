package com.denisshulika.fincentra.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun onEmailChange(newValue: String) { _email.value = newValue }
    fun onPasswordChange(newValue: String) { _password.value = newValue }

    fun signIn() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.signInWithEmail(_email.value, _password.value)
            _isLoading.value = false

            result.onSuccess { _events.emit(AuthUiEvent.NavigateToMain) }
                .onFailure { _events.emit(AuthUiEvent.ShowError(it.message ?: "Помилка входу")) }
        }
    }

    fun signUp() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.signUpWithEmail(_email.value, _password.value)
            _isLoading.value = false

            result.onSuccess { _events.emit(AuthUiEvent.NavigateToMain) }
                .onFailure { _events.emit(AuthUiEvent.ShowError(it.message ?: "Помилка реєстрації")) }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = authRepository.signInWithGoogle(credential)
            _isLoading.value = false

            result.onSuccess { _events.emit(AuthUiEvent.NavigateToMain) }
                .onFailure { _events.emit(AuthUiEvent.ShowError("Google Auth Error")) }
        }
    }
}