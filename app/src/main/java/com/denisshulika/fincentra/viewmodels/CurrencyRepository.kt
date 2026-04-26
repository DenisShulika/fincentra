package com.denisshulika.fincentra.viewmodels

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CurrencyRepository(private val db: FirebaseFirestore) {
    private var cachedRates: Map<Int, Double>? = null

    suspend fun getRates(): Map<Int, Double> = withContext(Dispatchers.IO) {
        if (cachedRates != null) return@withContext cachedRates!!

        val docRef = db.collection("settings").document("currency_rates")
        val snapshot = docRef.get().await()
        val now = System.currentTimeMillis()

        if (now - (snapshot.getLong("timestamp") ?: 0L) < 86400000 && snapshot.exists()) {
            val rates = snapshot.get("rates") as? Map<String, Any>
            if (rates != null) {
                cachedRates =
                    rates.mapKeys { it.key.toInt() }.mapValues { (it.value as Number).toDouble() }
                return@withContext cachedRates!!
            }
        }

        try {
            val jsonStr = java.net.URL("https://open.er-api.com/v6/latest/USD").readText()
            val json = org.json.JSONObject(jsonStr).getJSONObject("rates")
            val isoMap = mapOf("UAH" to 980, "USD" to 840, "EUR" to 978, "RON" to 946, "PLN" to 985)
            val newRates = isoMap.mapNotNull { (name, code) ->
                if (json.has(name)) code to json.getDouble(name) else null
            }.toMap()

            docRef.set(mapOf("rates" to newRates.mapKeys { it.key.toString() }, "timestamp" to now))
            cachedRates = newRates
            newRates
        } catch (e: Exception) {
            cachedRates ?: emptyMap()
        }
    }

    fun convert(amount: Double, from: Int, to: Int, rates: Map<Int, Double>): Double {
        if (from == to || rates.isEmpty()) return amount
        val rFrom = rates[from] ?: return amount
        val rTo = rates[to] ?: return amount
        return (amount / rFrom) * rTo
    }
}