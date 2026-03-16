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

    // Replace with IP address of your machine on the network if testing on a real device.
    // Use 10.0.2.2 if testing on Android Emulator
    private val HOST = "10.0.2.2" 
    private val PORT = 8080
    private val APP_KEY = "my-reverb-key" // As defined in Laravel .env

    suspend fun setupAndConnect() {
        if (pusher != null && pusher?.connection?.state == ConnectionState.CONNECTED) return

        val token = tokenManager.token.first() ?: return

        // Auth endpoint on Laravel backend
        val authorizer = HttpAuthorizer("http://$HOST:8000/api/broadcasting/auth")
        authorizer.setHeaders(mapOf(
            "Authorization" to "Bearer $token",
            "Accept" to "application/json"
        ))

        val options = PusherOptions().apply {
            setHost(HOST)
            setWsPort(PORT)
            setWssPort(PORT)
            isUseTLS = false // Set to true in production if using https
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
