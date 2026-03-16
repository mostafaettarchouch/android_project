package com.ofppt.istak

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class IstakApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        scheduleNotificationWorker()
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "Messages"
            val descriptionText = "Notifications pour les nouveaux messages"
            val importance = android.app.NotificationManager.IMPORTANCE_DEFAULT
            val channel = android.app.NotificationChannel("messages_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: android.app.NotificationManager =
                getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleNotificationWorker() {
        val workRequest = androidx.work.PeriodicWorkRequestBuilder<com.ofppt.istak.worker.NotificationWorker>(
            15, java.util.concurrent.TimeUnit.MINUTES
        ).setConstraints(
            androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()
        ).build()

        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "message_polling",
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
