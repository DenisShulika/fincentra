package com.denisshulika.fincentra.data.models.state

data class TransactionQuery(
    val bank: String = "Всі",
    val type: String = "Всі",
    val categories: Set<String> = emptySet(),
    val dateRange: LongRange? = null
)