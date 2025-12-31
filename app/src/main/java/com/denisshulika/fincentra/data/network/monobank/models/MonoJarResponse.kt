package com.denisshulika.fincentra.data.network.monobank.models

data class MonoJarResponse(
    val id: String,
    val title: String,
    val balance: Long,
    val currencyCode: Int
)