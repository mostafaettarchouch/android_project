package com.ofppt.istak.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
// import androidx.hilt.work.HiltWorker // Removed unused import
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ofppt.istak.MainActivity
import com.ofppt.istak.R
import com.ofppt.istak.data.local.TokenManager
import com.ofppt.istak.data.remote.ApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first


class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface NotificationWorkerEntryPoint {
        fun apiService(): ApiService
        fun tokenManager(): TokenManager
    }

    override suspend fun doWork(): Result {
        val appContext = applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            NotificationWorkerEntryPoint::class.java
        )
        val apiService = entryPoint.apiService()
        val tokenManager = entryPoint.tokenManager()

        // Check if user is logged in
        val token = tokenManager.token.first()
        if (token == null) {
            return Result.success()
        }

        return try {
            // 1. Check Messages
            val response = apiService.getMessages()
            if (response.isSuccessful && response.body() != null) {
                val messages = response.body()!!.messages
                val unreadMessages = messages.filter { !it.is_read }
                
                if (unreadMessages.isNotEmpty()) {
                    // Sort by ID descending to get the latest message
                    val latestMessage = unreadMessages.sortedByDescending { it.id }.first()
                    val count = unreadMessages.size
                    
                    val title = if (count == 1) "Nouveau Message" else "$count Nouveaux Messages"
                    val content = latestMessage.content
                    
                    showNotification(appContext, title, content, "messages")
                }
            }

            // 2. Check News
            val newsResponse = apiService.getNews()
            if (newsResponse.isSuccessful && newsResponse.body() != null) {
                val articles = newsResponse.body()!!.data
                if (articles.isNotEmpty()) {
                    val latestArticle = articles.maxByOrNull { it.id }
                    if (latestArticle != null) {
                        val lastSeenId = tokenManager.lastNewsId.first() ?: 0

                        if (latestArticle.id > lastSeenId) {
                            showNotification(appContext, latestArticle.title, latestArticle.content, "news")
                            tokenManager.saveLastNewsId(latestArticle.id)
                        }
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun showNotification(context: Context, title: String, content: String, navigateTo: String) {
            val channelId = "messages_channel"

            // Create Intent to open App
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigate_to", navigateTo)
            }

            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                context, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Parse HTML content
            val styledContent = androidx.core.text.HtmlCompat.fromHtml(content, androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY)

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(title)
                .setContentText(styledContent)
                .setStyle(NotificationCompat.BigTextStyle().bigText(styledContent))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            try {
                NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), builder.build())
            } catch (e: SecurityException) {
                // Permission not granted
            }
        }
    }
}
