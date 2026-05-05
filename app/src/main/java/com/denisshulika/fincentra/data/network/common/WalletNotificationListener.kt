package com.denisshulika.fincentra.data.network.common

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.denisshulika.fincentra.data.models.domain.Transaction
import com.denisshulika.fincentra.data.models.domain.TransactionCategory
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WalletNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val activeJobs = mutableMapOf<String, Job>()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        if (pkg != "com.google.android.apps.walletnfcrel" && pkg != "com.google.android.gms") return

        val notificationKey = sbn.key

        activeJobs[notificationKey]?.cancel()

        activeJobs[notificationKey] = scope.launch {
            delay(4000)

            val currentSbn = activeNotifications.find { it.key == notificationKey } ?: sbn
            val extras = currentSbn.notification.extras
            val title = extras.getString("android.title") ?: ""
            val text = extras.getString("android.text") ?: ""

            saveLogToFirebase("Package: $pkg | Title: $title | Text: $text")

            val currencyMarkers = listOf("грн", "UAH", "EUR", "€", "RON", "USD", "$", "PLN", "zł")
            if (currencyMarkers.any { text.contains(it) } && text.any { it.isDigit() }) {
                parseAndSaveTransaction(title, text)
            }

            activeJobs.remove(notificationKey)
        }
    }

    private suspend fun parseAndSaveTransaction(merchant: String, text: String) {
        val isEnabled = DependencyProvider.settingsRepository.isWalletSyncEnabled()
        if (!isEnabled) return

        try {
            val amountRegex = "([\\d\\s,.]+?)\\s?(грн|UAH|EUR|€|RON|USD|\\$|PLN|zł)".toRegex()
            val match = amountRegex.find(text) ?: return

            val rawAmount = match.groupValues[1]
                .replace("\\s".toRegex(), "")
                .replace(",", ".")
                .toDoubleOrNull() ?: return

            val currencyCode = mapCurrency(match.groupValues[2])

            val uniqueId = "wallet_${merchant.hashCode()}_${(rawAmount * 100).toInt()}"

            val newTx = Transaction(
                id = uniqueId,
                amount = rawAmount,
                description = merchant,
                timestamp = System.currentTimeMillis(),
                isExpense = true,
                bankName = "Google Wallet Sync",
                currencyCode = currencyCode,
                accountId = "google_wallet_sync",
                sourceType = "NOTIFICATION",
                category = TransactionCategory.OTHERS
            )

            DependencyProvider.financeRepository.addTransaction(newTx)
            Log.d("WALLET_SYNC", "Final parsed: $merchant | $rawAmount")

        } catch (e: Exception) {
            Log.e("WALLET_SYNC", "Parsing failed: ${e.message}")
            saveLogToFirebase("Error parsing: ${e.message}")
        }
    }

    private fun mapCurrency(str: String): Int {
        return when {
            str.contains("RON") -> 946
            str.contains("EUR") || str.contains("€") -> 978
            str.contains("PLN") || str.contains("zł") -> 985
            str.contains("UAH") || str.contains("грн") -> 980
            else -> 840
        }
    }

    private fun saveLogToFirebase(message: String) {
        scope.launch {
            val uid = DependencyProvider.auth.currentUser?.uid ?: "unknown"
            DependencyProvider.getInstance()
                .collection("users").document(uid)
                .collection("logs").add(
                    mapOf(
                        "message" to message,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
        }
    }
}