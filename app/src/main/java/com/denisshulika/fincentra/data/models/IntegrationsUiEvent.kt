package com.denisshulika.fincentra.data.models

sealed class IntegrationsUiEvent {
    data class OpenUrl(val url: String) : IntegrationsUiEvent()
    data class ShowToast(val message: String) : IntegrationsUiEvent()
}