package com.ofppt.istak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofppt.istak.data.model.Message
import com.ofppt.istak.data.model.SendMessageRequest
import com.ofppt.istak.data.model.User
import com.ofppt.istak.data.remote.ApiService
import com.ofppt.istak.data.websocket.ReverbClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MessageUiState {
    object Loading : MessageUiState()
    data class Success(val messages: List<Message>, val admin: User?) : MessageUiState()
    data class Error(val message: String) : MessageUiState()
}

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val apiService: ApiService,
    private val reverbClient: ReverbClient
) : ViewModel() {

    private val _uiState = MutableStateFlow<MessageUiState>(MessageUiState.Loading)
    val uiState: StateFlow<MessageUiState> = _uiState.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _sending = MutableStateFlow(false)
    val sending: StateFlow<Boolean> = _sending.asStateFlow()

    init {
        fetchUnreadCount()
        // We will start WebSocket after the first message load to ensure we have the User's ID
    }

    private var isSubscribedToChat = false

    private fun setupWebSocket(userId: Int) {
        if (isSubscribedToChat) return
        isSubscribedToChat = true
        
        viewModelScope.launch {
            reverbClient.setupAndConnect()
            val pusher = reverbClient.getPusher()
            
            // Laravel private channels are prefixed with "private-"
            val channelName = "private-chat.$userId" 
            
            val channel = pusher?.subscribePrivate(channelName)
            
            channel?.bind("message.sent") { _ ->
                // Simple approach: When any message is received on this channel, fetch the updated list
                fetchMessages() 
                fetchUnreadCount()
            }
        }
    }

    private suspend fun fetchMessagesSilently() {
        try {
            val response = apiService.getMessages()
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                _uiState.value = MessageUiState.Success(data.messages, data.admin)
            }
        } catch (e: Exception) {
            // Ignore in silent polling
        }
    }

    fun fetchMessages() {
        viewModelScope.launch {
            _uiState.value = MessageUiState.Loading
            try {
                val response = apiService.getMessages()
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    _uiState.value = MessageUiState.Success(data.messages, data.admin)
                    
                    // Extract current user ID from messages to subscribe to private chat channel
                    // The channel name is based on the user's ID.
                    val adminId = data.admin?.id
                    val firstMessage = data.messages.firstOrNull()
                    val myId = if (firstMessage?.sender_id != adminId) firstMessage?.sender_id else firstMessage?.receiver_id
                    
                    if (myId != null) {
                        setupWebSocket(myId)
                    }

                    // Mark as read locally (server does it too)
                    _unreadCount.value = 0
                } else {
                    _uiState.value = MessageUiState.Error("Erreur lors du chargement des messages")
                }
            } catch (e: Exception) {
                _uiState.value = MessageUiState.Error(e.message ?: "Erreur inconnue")
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        
        viewModelScope.launch {
            _sending.value = true
            try {
                val response = apiService.sendMessage(SendMessageRequest(content))
                if (response.isSuccessful) {
                    fetchMessages() // Refresh list immediately after sending
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _sending.value = false
            }
        }
    }

    fun fetchUnreadCount() {
        viewModelScope.launch {
            try {
                val response = apiService.getUnreadCount()
                if (response.isSuccessful && response.body() != null) {
                    _unreadCount.value = response.body()!!.count
                }
            } catch (e: Exception) {
                // Ignore for now
            }
        }
    }
}
