package com.denisshulika.fincentra.data.network.monobank.models

data class MonoAccountResponse(
    val id: String,
    val type: String,
    val balance: Long,
    val currencyCode: Int,
    val iban: String? = null,
    val maskedPan: List<String> = emptyList()
)