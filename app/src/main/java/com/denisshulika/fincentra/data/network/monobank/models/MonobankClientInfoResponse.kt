package com.denisshulika.fincentra.data.network.monobank.models

data class MonobankClientInfoResponse(
    val clientId: String,
    val name: String,
    val accounts: List<MonobankAccountResponse>,
    val jars: List<MonobankJarResponse>? = null
)