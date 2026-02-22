package com.denisshulika.fincentra.data.models.events

sealed class IntegrationsUiEvent {
    data class OpenUrl(val url: String) : IntegrationsUiEvent()
    data class ShowToast(val messageRes: Int) : IntegrationsUiEvent()
}