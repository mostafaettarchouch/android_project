package com.ofppt.istak.data.websocket

import android.util.Log
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.pusher.client.util.HttpAuthorizer
import com.ofppt.istak.data.local.TokenManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReverbClient @Inject constructor(
    private val tokenManager: TokenManager
) {

    private var pusher: Pusher? = null

    // Production Settings
    private val HOST = "absence.ofppt.dev" 
    private val PORT = 443
    private val APP_KEY = "your-app-key" // Make sure this matches your Laravel .env for production

    suspend fun setupAndConnect() {
        if (pusher != null && pusher?.connection?.state == ConnectionState.CONNECTED) return

        val token = tokenManager.token.first() ?: return

        // Auth endpoint on production
        val authorizer = HttpAuthorizer("https://$HOST/api/broadcasting/auth")
        authorizer.setHeaders(mapOf(
            "Authorization" to "Bearer $token",
            "Accept" to "application/json"
        ))

        val options = PusherOptions().apply {
            setHost(HOST)
            setWsPort(PORT)
            setWssPort(PORT)
            isUseTLS = true // Production uses https/wss
            setAuthorizer(authorizer)
        }

        pusher = Pusher(APP_KEY, options)

        pusher?.connect(object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                Log.d("ReverbClient", "State changed from ${change.previousState} to ${change.currentState}")
            }

            override fun onError(message: String?, code: String?, e: Exception?) {
                Log.e("ReverbClient", "Connection error: $message", e)
            }
        }, ConnectionState.ALL)
    }

    fun getPusher(): Pusher? {
        return pusher
    }
    
    fun disconnect() {
        pusher?.disconnect()
    }
}
