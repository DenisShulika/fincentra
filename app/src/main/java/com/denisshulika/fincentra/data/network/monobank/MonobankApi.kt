package com.denisshulika.fincentra.data.network.monobank

import com.denisshulika.fincentra.data.network.monobank.models.MonoClientInfoResponse
import com.denisshulika.fincentra.data.network.monobank.models.MonoTransactionResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface MonobankApi {
    @GET("personal/client-info")
    suspend fun getClientInfo(
        @Header("X-Token") token: String
    ): MonoClientInfoResponse

    @GET("personal/statement/{account}/{from}/{to}")
    suspend fun getStatement(
        @Header("X-Token") token: String,
        @Path("account") account: String,
        @Path("from") from: Long,
        @Path("to") to: Long
    ): List<MonoTransactionResponse>
}