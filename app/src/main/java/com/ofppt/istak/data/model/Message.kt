package com.ofppt.istak.data.model

data class Message(
    val id: Int,
    val sender_id: Int,
    val receiver_id: Int,
    val content: String,
    val is_read: Boolean,
    val created_at: String,
    val updated_at: String
)

data class MessageResponse(
    val messages: List<Message>,
    val admin: User?
)

data class SendMessageRequest(
    val content: String
)

data class UnreadCountResponse(
    val count: Int
)
