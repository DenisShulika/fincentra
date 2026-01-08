package com.denisshulika.fincentra.data.repository

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull

class GlobalSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val repository = DependencyProvider.repository
    private val monoService = DependencyProvider.monobankProvider

    private suspend fun syncMonobank() {
        val token = repository.getMonobankApiToken()
        val selectedIds = repository.getSelectedAccountIds()

        if (token.isNullOrBlank() || selectedIds.isEmpty()) return

        val actualAccounts = monoService.fetchAccounts(token)
        if (actualAccounts.isNotEmpty()) {
            repository.saveAccounts(actualAccounts, updateSelection = false)
        }

        for (id in selectedIds) {
            val acc = actualAccounts.find { it.id == id }
                ?: repository.getAccountsOnce().find { it.id == id }
                ?: continue

            val lastSyncMillis = repository.getLastSyncTimestamp(id)

            val fromTimeSeconds = if (lastSyncMillis == 0L) 0L else (lastSyncMillis / 1000) + 1

            try {
                monoService.fetchTransactionsForAccount(
                    token = token,
                    accountId = id,
                    accountCurrency = acc.currencyCode,
                    fromTimeSeconds = fromTimeSeconds,
                    onProgress = { },
                    onBatchLoaded = { batch ->
                        repository.addTransactionsBatch(batch)
                        repository.saveLastSyncTimestamp(id, batch.maxOf { it.timestamp })
                    }
                )
            } catch (e: Exception) {
                Log.e("SYNC_WORKER", "Помилка карти $id: ${e.message}")
            }

            if (id != selectedIds.last()) {
                delay(60000)
            }
        }
    }

    override suspend fun doWork(): Result {
        return try {
            syncMonobank()
            checkBudgetsAndNotify()

            repository.saveLastGlobalSyncTime(System.currentTimeMillis())
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun checkBudgetsAndNotify() {
        val cal = java.util.Calendar.getInstance()
        val monthYear = "${cal.get(java.util.Calendar.MONTH) + 1}-${cal.get(java.util.Calendar.YEAR)}"

        val budgets = repository.getBudgetsFlow(monthYear).firstOrNull() ?: return
        val transactions = repository.transactions.value

        for (budget in budgets) {
            val spent = transactions.filter { tx ->
                val txCal = java.util.Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                tx.isExpense &&
                        tx.category.displayName == budget.categoryName &&
                        txCal.get(java.util.Calendar.MONTH) == cal.get(java.util.Calendar.MONTH) &&
                        tx.currencyCode == budget.currencyCode
            }.sumOf { it.amount }

            if (spent > budget.limitAmount) {
                sendNotification(
                    title = "Перевищено ліміт: ${budget.categoryName}",
                    message = "Ви витратили ${spent.toInt()} з запланованих ${budget.limitAmount.toInt()} грн"
                )
            }
        }
    }

    private fun sendNotification(title: String, message: String) {
        val builder = androidx.core.app.NotificationCompat.Builder(applicationContext, "BUDGET_ALERTS")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(androidx.core.app.NotificationManagerCompat.from(applicationContext)) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notify(title.hashCode(), builder.build())
            }
        }
    }
}