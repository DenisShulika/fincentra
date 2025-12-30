package com.denisshulika.fincentra.data.network

import com.denisshulika.fincentra.data.network.models.MonoTransactionResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface MonobankApi {
    @GET("personal/statement/{account}/{from}/{to}")
    suspend fun getStatement(
        @Header("X-Token") token: String,
        @Path("account") account: String = "0",
        @Path("from") from: Long,
        @Path("to") to: Long? = null
    ): List<MonoTransactionResponse>
}