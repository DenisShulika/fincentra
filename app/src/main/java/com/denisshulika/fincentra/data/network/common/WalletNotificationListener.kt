package com.denisshulika.fincentra.data.network.common

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.denisshulika.fincentra.data.models.domain.Transaction
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WalletNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != "com.google.android.apps.walletnfcrel") return

        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: "Payment"
        val text = extras.getString("android.text") ?: ""

        if (text.isNotBlank()) {
            parseAndSaveTransaction(title, text)
        }
    }

    private fun parseAndSaveTransaction(merchant: String, text: String) {
        scope.launch {
            val isUserEnabled = DependencyProvider.settingsRepository.isWalletSyncEnabled()
            if (!isUserEnabled) return@launch

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
                    description = merchant,
                    timestamp = System.currentTimeMillis(),
                    isExpense = true,
                    bankName = "Google Wallet",
                    currencyCode = currencyCode,
                    accountId = "google_wallet_sync",
                    sourceType = "NOTIFICATION"
                )

                DependencyProvider.financeRepository.addTransaction(newTx)
                Log.d(
                    "WALLET_SYNC",
                    "Successfully parsed Romanian/EU payment: $rawAmount $currencyStr"
                )

            } catch (e: Exception) {
                Log.e("WALLET_SYNC", "Parsing failed: ${e.message}")
            }
        }
    }
}