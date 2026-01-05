package com.denisshulika.fincentra.data.models.events

sealed class AuthUiEvent {
    data object NavigateToMain : AuthUiEvent()
    data class ShowError(val message: String) : AuthUiEvent()
}