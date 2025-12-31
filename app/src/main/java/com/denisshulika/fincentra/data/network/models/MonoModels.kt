package com.denisshulika.fincentra.data.network.models

data class MonoAccountResponse(
    val id: String,
    val type: String,
    val balance: Long,
    val currencyCode: Int,
    val iban: String? = null
)

data class MonoJarResponse(
    val id: String,
    val title: String,
    val balance: Long,
    val currencyCode: Int
)

data class MonoClientInfoResponse(
    val clientId: String,
    val name: String,
    val accounts: List<MonoAccountResponse>,
    val jars: List<MonoJarResponse>? = null
)

data class MonoTransactionResponse(
    val id: String,
    val time: Long,
    val description: String,
    val mcc: Int,
    val amount: Long,
    val balance: Long,
    val comment: String? = null,
    val counterName: String? = null
)