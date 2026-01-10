package com.denisshulika.fincentra

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.denisshulika.fincentra.data.repository.GlobalSyncWorker
import java.util.concurrent.TimeUnit

class FinCentraApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        setupBackgroundSync()
    }

    private fun createNotificationChannel() {
        val name = "Фінансові ліміти"
        val descriptionText = "Сповіщення про перевищення місячного бюджету"
        val importance = android.app.NotificationManager.IMPORTANCE_DEFAULT
        val channel = android.app.NotificationChannel("BUDGET_ALERTS", name, importance).apply {
            description = descriptionText
        }
        val notificationManager =
            getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun setupBackgroundSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<GlobalSyncWorker>(8, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setBackoffCriteria(androidx.work.BackoffPolicy.EXPONENTIAL, 1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "GlobalSyncWork",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}