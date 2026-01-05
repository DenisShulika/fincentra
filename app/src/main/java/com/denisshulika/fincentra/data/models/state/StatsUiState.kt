package com.denisshulika.fincentra.data.models.state

data class StatsUiState(
    val currencyData: List<CurrencyStats> = emptyList(),
    val dateRange: LongRange? = null
)