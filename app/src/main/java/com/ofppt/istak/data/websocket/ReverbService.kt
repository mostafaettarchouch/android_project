package com.ofppt.istak.data.websocket

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ofppt.istak.MainActivity
import com.ofppt.istak.data.local.TokenManager
import com.ofppt.istak.worker.NotificationWorker
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReverbService : Service() {

    @Inject
    lateinit var reverbClient: ReverbClient

    @Inject
    lateinit var tokenManager: TokenManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        connectWebSocket()
    }

    private fun startForegroundService() {
        val channelId = "reverb_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Service de Notification",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Istak est en ligne")
            .setContentText("Recherche de nouveaux messages...")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .build()

        startForeground(1, notification)
    }

    private fun connectWebSocket() {
        serviceScope.launch {
            val token = tokenManager.token.first()
            val userId = tokenManager.userId.first()
            
            if (token == null || userId == null) {
                Log.d("ReverbService", "Token or UserId missing, stopping service")
                stopSelf()
                return@launch
            }

            reverbClient.setupAndConnect()
            val pusher = reverbClient.getPusher() ?: return@launch

            // Subscribe to News
            pusher.subscribe("news")?.bind("news.published") {
                NotificationWorker.showNotification(
                    this@ReverbService,
                    "Nouvelle Actualité",
                    "Une nouvelle actualité a été publiée.",
                    "news"
                )
            }

            // Subscribe to User Private Channel
            val channelName = "private-chat.$userId"
            val channel = pusher.subscribePrivate(channelName)
            
            channel?.bind("message.sent", object : PrivateChannelEventListener {
                override fun onEvent(event: PusherEvent?) {
                    NotificationWorker.showNotification(
                        this@ReverbService,
                        "Nouveau Message",
                        "Vous avez reçu un nouveau message de l'administration.",
                        "messages"
                    )
                }

                override fun onAuthenticationFailure(message: String?, e: Exception?) {
                    Log.e("ReverbService", "Auth failure for channel $channelName: $message", e)
                }

                override fun onSubscriptionSucceeded(channelName: String?) {
                    Log.d("ReverbService", "Subscribed to $channelName")
                }
            })
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        reverbClient.disconnect()
        serviceScope.cancel()
    }
}
