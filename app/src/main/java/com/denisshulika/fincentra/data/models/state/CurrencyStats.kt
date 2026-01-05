package com.denisshulika.fincentra.data.models.state

data class CurrencyStats(
    val currencyCode: Int,
    val startPeriodBalance: Double,
    val endPeriodBalance: Double,
    val totalIncome: Double,
    val totalExpense: Double,
    val categories: List<CategoryStat>
)