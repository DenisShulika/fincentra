package com.denisshulika.fincentra.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.models.domain.Dream
import com.denisshulika.fincentra.data.models.domain.DreamProgress
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GoalsViewModel : ViewModel() {
    private val repository = DependencyProvider.repository

    // Потік прогресу мрії
    val dreamProgress: StateFlow<DreamProgress?> = combine(
        repository.getDreamFlow(),
        repository.accounts
    ) { dream, accounts ->
        if (dream == null) return@combine null

        val totalBalance = accounts
            .filter { it.currencyCode == dream.currencyCode && it.selected }
            .sumOf { it.balance }

        val available = (totalBalance - dream.safetyBuffer).coerceAtLeast(0.0)
        val progress = if (dream.targetAmount > 0) {
            (available / dream.targetAmount).toFloat().coerceIn(0f, 1f)
        } else 0f

        DreamProgress(dream, available, progress)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateDream(title: String, target: Double, buffer: Double) {
        viewModelScope.launch {
            val newDream = Dream(
                title = title,
                targetAmount = target,
                safetyBuffer = buffer
            )
            repository.saveDream(newDream)
        }
    }
}