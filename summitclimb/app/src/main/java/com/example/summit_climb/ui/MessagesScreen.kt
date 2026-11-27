package com.example.summit_climb.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import com.example.summit_climb.model.Notification
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Displays the current user's conversations.
 */
@Composable
fun MessagesScreen(
    currentUserId: String,
    conversations: List<Notification>,
    getUserName: (String) -> String,
    getCampsiteName: (String) -> String,
    onConversationClick: (String, String) -> Unit,
    onBack: () -> Unit,
    error: String = ""
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFCFE7F5))
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back", color = charcoal)
        }

        Text(
            text = "Messages",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF245C86)
        )

        Spacer(Modifier.height(16.dp))

        if (error.isNotBlank()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (conversations.isEmpty() && error.isBlank()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No conversations yet.", color = charcoal)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(conversations) { message ->

                    val otherUserId = if (
                        message.senderId == currentUserId
                    ) {
                        message.recipientId
                    } else {
                        message.senderId
                    }

                    ConversationCard(
                        message = message,
                        userName = getUserName(otherUserId),
                        campsiteName = getCampsiteName(message.campsiteId),
                        onClick = {
                            onConversationClick(
                                message.campsiteId,
                                otherUserId
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Displays the most recent message in a conversation.
 */
@Composable
private fun ConversationCard(
    message: Notification,
    userName: String,
    campsiteName: String,
    onClick: () -> Unit
) {
    val date = SimpleDateFormat(
        "MMM d, h:mm a",
        LocalConfiguration.current.locales[0]
    ).format(Date(message.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = userName,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF245C86)
            )

            Text(
                text = campsiteName,
                fontSize = 13.sp,
                color = charcoal
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = message.text,
                fontSize = 14.sp,
                color = charcoal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = date,
                fontSize = 11.sp,
                color = charcoal
            )
        }
    }
}