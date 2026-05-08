package com.denisshulika.fincentra.data.repository

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.models.domain.TransactionCategory
import com.denisshulika.fincentra.data.network.common.CurrencyMapper
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class GlobalSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val financeRepository = DependencyProvider.financeRepository
    private val settingsRepository = DependencyProvider.settingsRepository
    private val budgetRepository = DependencyProvider.budgetRepository
    private val monoService = DependencyProvider.monobankProvider

    private suspend fun syncMonobank() {
        val token = settingsRepository.getMonobankApiToken()
        val selectedIds = settingsRepository.getSelectedAccountIds()

        if (token.isNullOrBlank() || selectedIds.isEmpty()) return

        val actualAccounts = monoService.fetchAccounts(token)
        if (actualAccounts.isNotEmpty()) {
            financeRepository.saveAccounts(actualAccounts, updateSelection = false)
        }

        for (id in selectedIds) {
            val acc = actualAccounts.find { it.id == id }
                ?: financeRepository.getAccountsOnce().find { it.id == id }
                ?: continue

            val lastSyncMillis = settingsRepository.getLastSyncTimestamp(id)
            val fromTimeSeconds =
                if (lastSyncMillis == 0L) (System.currentTimeMillis() / 1000) - 2682000L else (lastSyncMillis / 1000) + 1

            try {
                monoService.fetchTransactionsForAccount(
                    token = token,
                    accountId = id,
                    accountCurrency = acc.currencyCode,
                    fromTimeSeconds = fromTimeSeconds,
                    onProgress = { },
                    onBatchLoaded = { batch ->
                        financeRepository.addTransactionsBatch(batch)
                        settingsRepository.saveLastSyncTimestamp(id, batch.maxOf { it.timestamp })
                    }
                )
            } catch (e: Exception) {
                Log.e("SYNC_WORKER", "Card sync error $id: ${e.message}")
            }

            if (id != selectedIds.last()) {
                delay(60000)
            }
        }
    }

    override suspend fun doWork(): Result {
        return try {
            DependencyProvider.currencyRepository.getRates()
            syncMonobank()
            checkBudgetsAndNotify()

            settingsRepository.saveLastGlobalSyncTime(System.currentTimeMillis())

            Result.success()
        } catch (e: Exception) {
            Log.e("SYNC_WORKER", "Work failed: ${e.message}")
            Result.retry()
        }
    }

    private suspend fun checkBudgetsAndNotify() {
        val cal = Calendar.getInstance()
        val monthYear = "${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.YEAR)}"

        val budgets = budgetRepository.getBudgetsFlow(monthYear).firstOrNull() ?: return
        val transactions = financeRepository.transactions.value

        for (budget in budgets) {
            val spent = transactions.filter { tx ->
                val txCal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                tx.isExpense &&
                        tx.category.name == budget.categoryName &&
                        txCal.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
                        tx.currencyCode == budget.currencyCode
            }.sumOf { it.amount }

            if (spent > budget.limitAmount) {
                val category = TransactionCategory.entries.find { it.name == budget.categoryName }
                val categoryDisplayName =
                    category?.let { applicationContext.getString(it.displayNameRes) }
                        ?: budget.categoryName
                val currencySymbol = CurrencyMapper.getSymbol(budget.currencyCode)

                sendNotification(
                    title = applicationContext.getString(
                        R.string.global_sync_worker_notif_title,
                        categoryDisplayName
                    ),
                    message = applicationContext.getString(
                        R.string.global_sync_worker_notif_message,
                        spent.toInt(),
                        budget.limitAmount.toInt(),
                        currencySymbol
                    )
                )
            }
        }
    }

    private fun sendNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(applicationContext, "BUDGET_ALERTS")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(title.hashCode(), builder.build())
            }
        } else {
            notificationManager.notify(title.hashCode(), builder.build())
        }
    }
}