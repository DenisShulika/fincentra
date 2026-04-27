package com.denisshulika.fincentra.data.util

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object DeepLinkHandler {
    private val _authSuccessEvent =
        MutableSharedFlow<Unit>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val authSuccessEvent = _authSuccessEvent.asSharedFlow()

    private val _navigationEvent = MutableSharedFlow<String>(replay = 1)
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun onAuthSuccess() {
        _authSuccessEvent.tryEmit(Unit)
    }

    suspend fun emitNavigation(route: String) {
        _navigationEvent.emit(route)
    }

    fun resetAuthEvent() {
        _authSuccessEvent.tryEmit(Unit)
    }
}