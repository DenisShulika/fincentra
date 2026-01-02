package com.denisshulika.fincentra.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.models.CurrencySummary
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val repository = DependencyProvider.repository
    private val _currencySummaries = MutableStateFlow<List<CurrencySummary>>(emptyList())
    val currencySummaries = _currencySummaries.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAccountsFlow().collect { accounts ->
                val summaries = accounts
                    .filter { it.selected }
                    .groupBy { it.currencyCode }
                    .map { (code, list) ->
                        CurrencySummary(code, list.sumOf { it.balance })
                    }
                    .sortedByDescending { it.currencyCode == 980 }
                _currencySummaries.value = summaries
            }
        }
    }
}