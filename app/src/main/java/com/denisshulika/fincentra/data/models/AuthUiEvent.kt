package com.denisshulika.fincentra.data.models

sealed class AuthUiEvent {
    data object NavigateToMain : AuthUiEvent()
    data class ShowError(val message: String) : AuthUiEvent()
}