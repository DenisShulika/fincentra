package com.denisshulika.fincentra.viewmodels

sealed class IntegrationsUiEvent {
    data class OpenUrl(val url: String) : IntegrationsUiEvent()
    data class ShowToast(val message: String) : IntegrationsUiEvent()
}