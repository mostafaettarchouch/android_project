package com.ofppt.istak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ofppt.istak.data.model.Message
import com.ofppt.istak.data.model.User
import com.ofppt.istak.viewmodel.MessageUiState
import com.ofppt.istak.viewmodel.MessageViewModel

@Composable
fun ChatDialog(
    viewModel: MessageViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val sending by viewModel.sending.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.fetchMessages()
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(uiState) {
        if (uiState is MessageUiState.Success) {
            val messages = (uiState as MessageUiState.Success).messages
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Full width
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F7FA))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Discussion avec l'Administration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState is MessageUiState.Success) {
                            val admin = (uiState as MessageUiState.Success).admin
                            if (admin != null) {
                                Text(
                                    text = admin.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider()

                // Messages List
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFF5F7FA))
                        .padding(horizontal = 16.dp)
                ) {
                    when (uiState) {
                        is MessageUiState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                        is MessageUiState.Error -> {
                            Text(
                                text = (uiState as MessageUiState.Error).message,
                                color = Color.Red,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        is MessageUiState.Success -> {
                            val messages = (uiState as MessageUiState.Success).messages
                            val currentUserId = 0 // We don't have easy access to ID here, but we can infer from sender_id vs admin logic
                            // Actually, better logic: if sender_id == admin?.id then it's received.
                            val adminId = (uiState as MessageUiState.Success).admin?.id ?: -1

                            if (messages.isEmpty()) {
                                Text(
                                    text = "Aucun message. Commencez la discussion !",
                                    color = Color.Gray,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                LazyColumn(
                                    state = listState,
                                    contentPadding = PaddingValues(vertical = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(messages) { message ->
                                        val isAdmin = message.sender_id == adminId
                                        MessageBubble(message = message, isFromMe = !isAdmin)
                                    }
                                }
                            }
                        }
                    }
                }

                Divider()

                // Input Area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Votre message...") },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(messageText)
                                messageText = ""
                            }
                        },
                        containerColor = Color(0xFF2563EB),
                        contentColor = Color.White,
                        modifier = Modifier.size(48.dp)
                    ) {
                        if (sending) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isFromMe: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isFromMe) Color(0xFF2563EB) else Color.White,
            contentColor = if (isFromMe) Color.White else Color.Black,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromMe) 16.dp else 4.dp,
                bottomEnd = if (isFromMe) 4.dp else 16.dp
            ),
            shadowElevation = 1.dp
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = formatMessageDate(message.created_at),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
        )
    }
}

fun formatMessageDate(dateStr: String): String {
    return try {
        // Simple formatting, can be improved
        dateStr.substring(11, 16) // HH:mm
    } catch (e: Exception) {
        ""
    }
}
