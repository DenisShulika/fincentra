package com.denisshulika.fincentra.data.models.domain

data class Dream(
    val title: String = "",
    val targetAmount: Double = 0.0,
    val safetyBuffer: Double = 0.0,
    val currencyCode: Int = 980,
    val iconEmoji: String = "🚀"
)