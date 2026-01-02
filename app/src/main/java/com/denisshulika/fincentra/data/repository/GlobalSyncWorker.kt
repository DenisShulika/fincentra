package com.denisshulika.fincentra.data.repository

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.delay

class GlobalSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val repository = DependencyProvider.repository
    private val monoService = DependencyProvider.monobankProvider

    override suspend fun doWork(): Result {
        Log.d("SYNC_WORKER", "Розпочато глобальну фонову синхронізацію")

        return try {
            syncMonobank()

            Log.d("SYNC_WORKER", "Глобальна синхронізація успішно завершена")
            repository.saveLastGlobalSyncTime(System.currentTimeMillis())
            Result.success()
        } catch (e: Exception) {
            Log.e("SYNC_WORKER", "Помилка під час синхронізації: ${e.message}")
            Result.retry()
        }
    }

    private suspend fun syncMonobank() {
        val token = repository.getMonoToken()
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
}