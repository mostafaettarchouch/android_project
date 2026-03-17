package com.ofppt.istak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ofppt.istak.data.model.Message
import com.ofppt.istak.data.model.User
import com.ofppt.istak.ui.theme.NeumorphicColors
import com.ofppt.istak.ui.theme.neumorphic
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
                .padding(16.dp)
                .neumorphic(shape = RoundedCornerShape(28.dp), elevation = 10.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphic(shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), elevation = 2.dp)
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Discussion Administration",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (uiState is MessageUiState.Success) {
                                val admin = (uiState as MessageUiState.Success).admin
                                if (admin != null) {
                                    Text(
                                        text = "Avec: ${admin.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .neumorphic(shape = CircleShape, elevation = 2.dp)
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // Messages List
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    when (uiState) {
                        is MessageUiState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
                        }
                        is MessageUiState.Error -> {
                            Text(
                                text = (uiState as MessageUiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        is MessageUiState.Success -> {
                            val messages = (uiState as MessageUiState.Success).messages
                            val adminId = (uiState as MessageUiState.Success).admin?.id ?: -1

                            if (messages.isEmpty()) {
                                Text(
                                    text = "Aucun message. Commencez la discussion !",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.align(Alignment.Center),
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                LazyColumn(
                                    state = listState,
                                    contentPadding = PaddingValues(vertical = 20.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
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

                // Input Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphic(shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp), elevation = 4.dp)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .neumorphic(shape = RoundedCornerShape(24.dp), elevation = 2.dp, isPressed = true)
                        ) {
                            TextField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Votre message...") },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                maxLines = 3
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .neumorphic(shape = CircleShape, elevation = 4.dp)
                                .clickable {
                                    if (messageText.isNotBlank()) {
                                        viewModel.sendMessage(messageText)
                                        messageText = ""
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (sending) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                            }
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
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .neumorphic(
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isFromMe) 20.dp else 4.dp,
                        bottomEnd = if (isFromMe) 4.dp else 20.dp
                    ),
                    elevation = 2.dp,
                    darkShadowColor = if (isFromMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else NeumorphicColors.darkShadow()
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isFromMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isFromMe) FontWeight.Bold else FontWeight.Normal
            )
        }
        Text(
            text = formatMessageDate(message.created_at),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp)
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
