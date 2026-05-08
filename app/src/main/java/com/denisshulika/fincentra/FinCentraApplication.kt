package com.denisshulika.fincentra

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.denisshulika.fincentra.data.repository.GlobalSyncWorker
import com.denisshulika.fincentra.di.DependencyProvider
import java.util.concurrent.TimeUnit

class FinCentraApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DependencyProvider.init(this)

        createNotificationChannel()
        setupBackgroundSync()
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.fin_centra_app_notif_channel_budget_name)
        val descriptionText = getString(R.string.fin_centra_app_notif_channel_budget_desc)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = android.app.NotificationChannel("BUDGET_ALERTS", name, importance).apply {
            description = descriptionText
        }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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