package com.denisshulika.fincentra.data.repository

import com.denisshulika.fincentra.data.util.CurrencyConstants
import com.denisshulika.fincentra.data.util.FirestoreCollections
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.net.URL

class CurrencyRepository(private val db: FirebaseFirestore) {

    private var cachedRates: Map<Int, Double>? = null

    suspend fun getRates(): Map<Int, Double> =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            if (cachedRates != null) return@withContext cachedRates!!

            val docRef = db.collection(FirestoreCollections.SETTINGS).document("currency_rates")
            val snapshot = docRef.get().await()

            val lastUpdate = snapshot.getLong("timestamp") ?: 0L
            val now = System.currentTimeMillis()

            if (now - lastUpdate < 24 * 60 * 60 * 1000 && snapshot.exists()) {
                val rates = snapshot.get("rates") as? Map<String, Any>
                if (rates != null) {
                    val mapped = rates.mapKeys { it.key.toInt() }
                        .mapValues { (it.value as Number).toDouble() }
                    cachedRates = mapped
                    return@withContext mapped
                }
            }

            try {
                val jsonStr = URL("${CurrencyConstants.EXCHANGE_RATE_API_URL}USD").readText()
                val json = JSONObject(jsonStr).getJSONObject("rates")
                val newRates = mutableMapOf<Int, Double>()

                val isoMap =
                    mapOf("UAH" to 980, "USD" to 840, "EUR" to 978, "PLN" to 985, "RON" to 946)

                isoMap.forEach { (name, code) ->
                    if (json.has(name)) newRates[code] = json.getDouble(name)
                }

                docRef.set(
                    mapOf(
                        "rates" to newRates.mapKeys { it.key.toString() },
                        "timestamp" to now
                    )
                ).await()

                cachedRates = newRates
                newRates
            } catch (e: Exception) {
                emptyMap()
            }
        }

    fun convert(amount: Double, from: Int, to: Int, rates: Map<Int, Double>): Double? {
        if (from == to) return amount
        if (rates.isEmpty()) return null

        val rateFrom = rates[from] ?: return null
        val rateTo = rates[to] ?: return null

        return (amount / rateFrom) * rateTo
    }
}