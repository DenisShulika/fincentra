package com.denisshulika.fincentra.data.network.models

data class MonoTransactionResponse(
    val id: String,
    val time: Long,
    val description: String,
    val mcc: Int,
    val amount: Long,
    val balance: Long,
    val currencyCode: Int
)