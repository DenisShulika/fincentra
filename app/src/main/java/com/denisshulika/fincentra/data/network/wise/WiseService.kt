package com.denisshulika.fincentra.data.network.wise

import android.content.Context
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.models.domain.BankAccount
import com.denisshulika.fincentra.data.models.domain.Transaction
import com.denisshulika.fincentra.data.network.common.BankProvider
import com.denisshulika.fincentra.data.util.BankProviders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.time.ZonedDateTime
import kotlin.math.abs

class WiseService(private val context: Context) : BankProvider {
    private val client = OkHttpClient()
    private val BASE_URL = "https://api.transferwise.com"

    override suspend fun fetchAccounts(token: String): List<BankAccount> =
        withContext(Dispatchers.IO) {
            try {
                val profileReq = Request.Builder()
                    .url("$BASE_URL/v1/profiles")
                    .addHeader("Authorization", "Bearer $token")
                    .get().build()

                val profileRes = client.newCall(profileReq).execute()
                val profiles = JSONArray(profileRes.body?.string() ?: "[]")
                if (profiles.length() == 0) return@withContext emptyList()
                val profileId = profiles.getJSONObject(0).getLong("id")

                val accReq = Request.Builder()
                    .url("$BASE_URL/v1/borderless-accounts?profileId=$profileId")
                    .addHeader("Authorization", "Bearer $token")
                    .get().build()

                val accRes = client.newCall(accReq).execute()
                val accData = JSONArray(accRes.body?.string() ?: "[]")
                val result = mutableListOf<BankAccount>()

                if (accData.length() > 0) {
                    val balances = accData.getJSONObject(0).getJSONArray("balances")
                    for (i in 0 until balances.length()) {
                        val b = balances.getJSONObject(i)
                        val currency = b.getString("currency")
                        result.add(
                            BankAccount(
                                id = "wise_${currency.lowercase()}",
                                provider = BankProviders.WISE,
                                name = context.getString(
                                    R.string.wise_service_account_name_format,
                                    currency
                                ),
                                balance = b.getJSONObject("amount").getDouble("value"),
                                currencyCode = mapCurrencyToIso(currency),
                                selected = true,
                                sourceType = "DIRECT",
                                type = "borderless"
                            )
                        )
                    }
                }
                result
            } catch (e: Exception) {
                emptyList()
            }
        }

    override suspend fun fetchTransactionsForAccount(
        token: String,
        accountId: String,
        accountCurrency: Int,
        fromTimeSeconds: Long,
        onProgress: suspend (String) -> Unit,
        onBatchLoaded: suspend (List<Transaction>) -> Unit
    ): List<Transaction> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BASE_URL/v1/activities?limit=50")
                .addHeader("Authorization", "Bearer $token")
                .get().build()

            val response = client.newCall(request).execute()
            val json = JSONObject(response.body?.string() ?: "{}")
            val activities = json.getJSONArray("activities")
            val transactions = mutableListOf<Transaction>()

            for (i in 0 until activities.length()) {
                val act = activities.getJSONObject(i)
                val primaryAmount = act.getJSONObject("primaryAmount")

                val timestamp = try {
                    ZonedDateTime.parse(act.getString("createdAfter")).toInstant().toEpochMilli()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }

                transactions.add(
                    Transaction(
                        id = act.getString("id"),
                        accountId = accountId,
                        amount = abs(primaryAmount.getDouble("value")),
                        currencyCode = accountCurrency,
                        description = act.getString("title"),
                        timestamp = timestamp,
                        bankName = BankProviders.WISE,
                        isExpense = act.getString("type") == "DEBIT",
                        sourceType = "DIRECT"
                    )
                )
            }
            onBatchLoaded(transactions)
            transactions
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun mapCurrencyToIso(code: String): Int = when (code.uppercase()) {
        "UAH" -> 980; "USD" -> 840; "EUR" -> 978; "GBP" -> 826; "PLN" -> 985; "RON" -> 946; else -> 840
    }
}