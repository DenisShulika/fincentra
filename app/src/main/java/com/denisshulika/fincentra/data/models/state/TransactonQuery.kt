package com.denisshulika.fincentra.data.models.state

data class TransactionQuery(
    val bank: String = "ALL",
    val type: String = "ALL",
    val categories: Set<String> = emptySet(),
    val dateRange: LongRange? = null
)