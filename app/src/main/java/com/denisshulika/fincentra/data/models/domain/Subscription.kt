package com.denisshulika.fincentra.data.models.domain

enum class SubFrequency { MONTHLY, YEARLY, WEEKLY }

data class Subscription(
    val id: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val currencyCode: Int = 980,
    val nextPaymentDate: Long = 0L,
    val frequency: String = "MONTHLY"
)