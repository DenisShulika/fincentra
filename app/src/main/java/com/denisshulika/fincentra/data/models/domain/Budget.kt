package com.denisshulika.fincentra.data.models.domain

data class Budget(
    val id: String = "",
    val categoryName: String = "",
    val limitAmount: Double = 0.0,
    val currencyCode: Int = 980,
    val monthYear: String = ""
)