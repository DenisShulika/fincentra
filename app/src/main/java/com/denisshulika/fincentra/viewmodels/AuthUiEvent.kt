package com.denisshulika.fincentra.viewmodels

sealed class AuthUiEvent {
    data object NavigateToMain : AuthUiEvent()
    data class ShowError(val message: String) : AuthUiEvent()
}