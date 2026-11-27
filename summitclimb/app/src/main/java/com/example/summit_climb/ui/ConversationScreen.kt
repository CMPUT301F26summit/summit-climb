package com.example.summit_climb.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.summit_climb.model.Notification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.platform.LocalConfiguration
import android.util.Log

/**
 * Displays messages exchanged between an entrant
 * and a campsite owner.
 *
 * Allows the current user to view conversation history
 * and send new messages.
 */
@Composable
fun ConversationScreen(
    currentUserId: String,
    recipientName: String,
    campsiteName: String,
    messages: List<Notification>,
    onSendMessage: (String, () -> Unit) -> Unit,
    onBack: () -> Unit,
    isSending: Boolean = false,
    error: String = ""
) {
    var messageText by remember { mutableStateOf("") }

    val sortedMessages = messages.sortedBy { it.timestamp }
    Log.d("ConversationDebug", "UI received ${messages.size} messages")
    val listState = rememberLazyListState()

    LaunchedEffect(sortedMessages.size) {
        if (sortedMessages.isNotEmpty()) {
            listState.animateScrollToItem(
                sortedMessages.lastIndex
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFCFE7F5))
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            TextButton(onClick = onBack) {
                Text("← Back", color = charcoal)
            }

            Text(
                text = recipientName,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF245C86)
            )

            Text(
                text = campsiteName,
                fontSize = 14.sp,
                color = charcoal
            )
        }

        HorizontalDivider()

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (sortedMessages.isEmpty()) {
                Text(
                    text = "No messages yet. Start a conversation!",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(20.dp),
                    color = charcoal
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    items(sortedMessages) { message ->
                        MessageBubble(
                            message = message,
                            isCurrentUser =
                                message.senderId == currentUserId
                        )
                    }
                }
            }
        }

        if (error.isNotBlank()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(12.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = {
                    Text("Type a message...")
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4
            )

            Button(
                enabled = messageText.isNotBlank() &&
                        !isSending,
                onClick = {
                    onSendMessage(messageText.trim()) {
                        messageText = ""
                    }
                },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = greyBlue
                )
            ) {
                Text(
                    text = if (isSending) "..." else "Send",
                    color = charcoal
                )
            }
        }
    }
}

/**
 * Displays outgoing messages on the right
 * and incoming messages on the left.
 */
@Composable
private fun MessageBubble(
    message: Notification,
    isCurrentUser: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.78f),
            horizontalAlignment = if (isCurrentUser) {
                Alignment.End
            } else {
                Alignment.Start
            }
        ) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrentUser) {
                        Color(0xFF245C86)
                    } else {
                        Color.White
                    }
                )
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(14.dp),
                    color = if (isCurrentUser) {
                        Color.White
                    } else {
                        charcoal
                    },
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = SimpleDateFormat(
                    "MMM d, h:mm a",
                    LocalConfiguration.current.locales[0]
                ).format(Date(message.timestamp)),
                fontSize = 11.sp,
                color = charcoal
            )
        }
    }
}