package com.example.summit_climb.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.summit_climb.model.Notification
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Displays a private conversation for admin review.
 * Messages cannot be edited or deleted from this screen.
 */
@Composable
fun AdminConversationDetailsScreen(
    messages: List<Notification>,
    ownerName: String,
    camperName: String,
    campsiteName: String,
    getUserName: (String) -> String,
    onBack: () -> Unit,
    onViewOwner: () -> Unit,
    onViewCamper: () -> Unit
) {
    val sortedMessages = messages.sortedBy { it.timestamp }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFCFE7F5))
            .padding(16.dp)
    ) {
        Text(
            text = "Admin View",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = Color(0xFF36454F),
            fontSize = 18.sp
        )

        TextButton(onClick = onBack) {
            Text(
                text = "← Back",
                color = Color(0xFF245C86)
            )
        }

        Text(
            text = "$ownerName → $camperName",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF245C86)
        )

        Text(
            text = campsiteName,
            fontSize = 16.sp,
            color = Color(0xFF597A94)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Message History",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF36454F)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(sortedMessages) { message ->
                val senderName = getUserName(message.senderId)
                val locale = LocalConfiguration.current.locales[0]
                val date = SimpleDateFormat(
                    "MMM d",
                    locale
                ).format(Date(message.timestamp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = senderName,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF245C86)
                        )

                        Text(
                            text = date,
                            fontSize = 12.sp,
                            color = Color(0xFF597A94)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = message.text,
                            fontSize = 15.sp,
                            color = Color(0xFF36454F)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onViewOwner,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF245C86)
            )
        ) {
            Text("View Owner")
        }

        Button(
            onClick = onViewCamper,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF597A94)
            )
        ) {
            Text("View Camper")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Read-only: Logs can't be edited or deleted.",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            color = Color(0xFF597A94)
        )
    }
}