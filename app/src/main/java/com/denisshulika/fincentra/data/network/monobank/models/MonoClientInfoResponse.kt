package com.denisshulika.fincentra.data.network.monobank.models

data class MonoClientInfoResponse(
    val clientId: String,
    val name: String,
    val accounts: List<MonoAccountResponse>,
    val jars: List<MonoJarResponse>? = null
)