package com.denisshulika.fincentra

import android.app.Application
import androidx.work.*
import com.denisshulika.fincentra.data.repository.GlobalSyncWorker
import java.util.concurrent.TimeUnit

class FinCentraApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupBackgroundSync()
    }

    private fun setupBackgroundSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<GlobalSyncWorker>(8, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "GlobalSyncWork",git
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}