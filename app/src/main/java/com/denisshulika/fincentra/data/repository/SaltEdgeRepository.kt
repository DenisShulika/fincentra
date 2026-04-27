package com.denisshulika.fincentra.data.repository

import android.util.Log
import com.denisshulika.fincentra.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class SaltEdgeRepository {
    private val APP_ID = BuildConfig.SALT_EDGE_APP_ID
    private val SECRET = BuildConfig.SALT_EDGE_SECRET
    private val BASE_URL = "https://www.saltedge.com/api/v6/"

    private val client = OkHttpClient()
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun createCustomer(userIdentifier: String): String? = withContext(Dispatchers.IO) {
        if (userIdentifier.isBlank()) {
            Log.e("SALT_EDGE", "Identifier is blank!")
            return@withContext null
        }

        val json = JSONObject().apply {
            put("data", JSONObject().apply { put("identifier", userIdentifier) })
        }

        val request = Request.Builder()
            .url("${BASE_URL}customers")
            .addHeader("App-id", APP_ID)
            .addHeader("Secret", SECRET)
            .addHeader("Accept", "application/json")
            .post(json.toString().toRequestBody(mediaType))
            .build()

        try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("SALT_EDGE", "API Error: ${response.code} $body")
                return@withContext null
            }

            val responseJson = JSONObject(body)
            if (responseJson.has("data")) {
                val id = responseJson.getJSONObject("data").getString("customer_id")
                Log.d("SALT_EDGE", "Customer Created: $id")
                id
            } else null
        } catch (e: Exception) {
            Log.e("SALT_EDGE", "Crash prevented: ${e.message}")
            null
        }
    }

    suspend fun getConnectUrl(customerId: String, providerCode: String): String? =
        withContext(Dispatchers.IO) {
            val json = JSONObject().apply {
                put("data", JSONObject().apply {
                    put("customer_id", customerId)
                    put("consent", JSONObject().apply {
                        put("scopes", org.json.JSONArray(listOf("accounts", "transactions")))
                    })
                    put("attempt", JSONObject().apply {
                        put("return_to", "fincentra://callback")
                    })
                    put("provider", JSONObject().apply {
                        put("code", providerCode)
                    })
                })
            }

            val request = Request.Builder()
                .url("${BASE_URL}connections/connect")
                .addHeader("App-id", APP_ID)
                .addHeader("Secret", SECRET)
                .addHeader("Accept", "application/json")
                .post(json.toString().toRequestBody(mediaType))
                .build()

            try {
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    Log.e("SALT_EDGE", "Connect Session Error: ${response.code} $body")
                    return@withContext null
                }

                val responseJson = JSONObject(body)
                if (responseJson.has("data")) {
                    responseJson.getJSONObject("data").getString("connect_url")
                } else null
            } catch (e: Exception) {
                Log.e("SALT_EDGE", "Error in getConnectUrl: ${e.message}")
                null
            }
        }

    suspend fun fetchAccounts(customerId: String): List<JSONObject> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${BASE_URL}accounts?customer_id=$customerId")
            .addHeader("App-id", APP_ID)
            .addHeader("Secret", SECRET)
            .addHeader("Accept", "application/json")
            .get()
            .build()

        try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            val json = JSONObject(body)
            val dataArray = json.getJSONArray("data")
            val list = mutableListOf<JSONObject>()
            for (i in 0 until dataArray.length()) {
                list.add(dataArray.getJSONObject(i))
            }
            list
        } catch (e: Exception) {
            Log.e("SALT_EDGE", "Fetch Accounts Error: ${e.message}")
            emptyList()
        }
    }

    suspend fun fetchTransactions(customerId: String, accountId: String): List<JSONObject> =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("${BASE_URL}transactions?customer_id=$customerId&account_id=$accountId")
                .addHeader("App-id", APP_ID)
                .addHeader("Secret", SECRET)
                .addHeader("Accept", "application/json")
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""
                val json = JSONObject(body)
                val dataArray = json.getJSONArray("data")
                val list = mutableListOf<JSONObject>()
                for (i in 0 until dataArray.length()) {
                    list.add(dataArray.getJSONObject(i))
                }
                list
            } catch (e: Exception) {
                Log.e("SALT_EDGE", "Fetch Transactions Error: ${e.message}")
                emptyList()
            }
        }

    suspend fun findCustomer(userIdentifier: String): String? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${BASE_URL}customers")
            .addHeader("App-id", APP_ID)
            .addHeader("Secret", SECRET)
            .addHeader("Accept", "application/json")
            .get()
            .build()

        try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            val json = JSONObject(body)
            val dataArray = json.getJSONArray("data")

            for (i in 0 until dataArray.length()) {
                val customer = dataArray.getJSONObject(i)
                if (customer.getString("identifier") == userIdentifier) {
                    return@withContext customer.getString("customer_id")
                }
            }
            null
        } catch (e: Exception) {
            Log.e("SALT_EDGE", "Find Customer Error: ${e.message}")
            null
        }
    }
}