package com.denisshulika.fincentra.data.network.monobank

import com.denisshulika.fincentra.data.network.monobank.models.MonobankClientInfoResponse
import com.denisshulika.fincentra.data.network.monobank.models.MonobankTransactionResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface MonobankApi {
    @GET("personal/client-info")
    suspend fun getClientInfo(
        @Header("X-Token") token: String
    ): MonobankClientInfoResponse

    @GET("personal/statement/{account}/{from}/{to}")
    suspend fun getStatement(
        @Header("X-Token") token: String,
        @Path("account") account: String,
        @Path("from") from: Long,
        @Path("to") to: Long
    ): List<MonobankTransactionResponse>
}