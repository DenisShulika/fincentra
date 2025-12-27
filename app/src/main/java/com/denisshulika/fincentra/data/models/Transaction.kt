package com.denisshulika.fincentra.data.models

import java.util.UUID

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double = 0.0,
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "Різне",
    val bankName: String = "Готівка",
    val isExpense: Boolean = true
)