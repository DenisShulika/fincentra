package com.denisshulika.fincentra.data.repository

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.denisshulika.fincentra.data.network.monobank.MonobankService
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.delay

class GlobalSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val repository = DependencyProvider.repository
    private val monoService = MonobankService()

    override suspend fun doWork(): Result {
        Log.d("SYNC_WORKER", "Розпочато глобальну фонову синхронізацію")

        return try {
            syncMonobank()

            Log.d("SYNC_WORKER", "Глобальна синхронізація успішно завершена")
            repository.saveLastGlobalSyncTime(System.currentTimeMillis())
            Log.d("SYNC_WORKER", "Час останньої синхронізації оновлено в базі")
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

        Log.d("SYNC_WORKER", "Синхронізація Monobank...")

        val actualAccounts = monoService.fetchAccounts(token)
        repository.saveAccounts(actualAccounts)

        val allTxs = mutableListOf<com.denisshulika.fincentra.data.models.Transaction>()

        for (id in selectedIds) {
            val acc = actualAccounts.find { it.id == id }
            val currency = acc?.currencyCode ?: 980

            try {
                val txs = monoService.fetchTransactionsForAccount(token, id, currency)
                allTxs.addAll(txs)
            } catch (e: Exception) {
                Log.e("SYNC_WORKER", "Помилка карти $id: ${e.message}")
            }

            if (id != selectedIds.last()) {
                delay(65000)
            }
        }

        if (allTxs.isNotEmpty()) {
            repository.addTransactionsBatch(allTxs)
        }
    }
}