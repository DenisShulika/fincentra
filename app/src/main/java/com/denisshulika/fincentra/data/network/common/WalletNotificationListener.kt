package com.denisshulika.fincentra.data.network.common

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.denisshulika.fincentra.data.models.domain.Transaction
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WalletNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        if (pkg != "com.google.android.apps.walletnfcrel" && pkg != "com.google.android.gms") return

        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: "Payment"
        val text = extras.getString("android.text") ?: ""

        if (text.isNotBlank()) {
            parseAndSaveTransaction(pkg, title, text)
        }
    }

    private fun parseAndSaveTransaction(pkg: String, title: String, text: String) {
        scope.launch {
            val isEnabled = DependencyProvider.settingsRepository.isWalletSyncEnabled()

            saveLogToFirebase("Package: $pkg | Title: $title | Text: $text | AppEnabled: $isEnabled")

            if (!isEnabled) return@launch

            try {
                val amountRegex = "([\\d\\s,.]+?)\\s?(UAH|грн|EUR|€|RON|USD|\\$)".toRegex()
                val match = amountRegex.find(text) ?: return@launch

                val rawAmount = match.groupValues[1]
                    .replace("\\s".toRegex(), "")
                    .replace(",", ".")
                    .toDoubleOrNull() ?: return@launch

                val currencyStr = match.groupValues[2]
                val currencyCode = when {
                    currencyStr.contains("RON") -> 946
                    currencyStr.contains("EUR") || currencyStr.contains("€") -> 978
                    currencyStr.contains("UAH") || currencyStr.contains("грн") -> 980
                    else -> 840
                }

                val newTx = Transaction(
                    id = "wallet_${System.currentTimeMillis()}",
                    amount = rawAmount,
                    description = title,
                    timestamp = System.currentTimeMillis(),
                    isExpense = true,
                    bankName = "Google Wallet",
                    currencyCode = currencyCode,
                    accountId = "google_wallet_sync",
                    sourceType = "NOTIFICATION"
                )

                DependencyProvider.financeRepository.addTransaction(newTx)
            } catch (e: Exception) {
                saveLogToFirebase("Error parsing: ${e.message}")
            }
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