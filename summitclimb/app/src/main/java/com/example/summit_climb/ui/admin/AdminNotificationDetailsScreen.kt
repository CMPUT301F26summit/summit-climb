
package com.example.summit_climb.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.summit_climb.model.Notification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private object AdminNotificationDetailsColors {
    val background = Color(0xFFCFE7F5)
    val cardBackground = Color.White
    val charcoal = Color(0xFF36454F)
    val darkBlue = Color(0xFF245C86)
    val mutedBlue = Color(0xFF597A94)
    val lightGreen = Color(0xFF96B38E)
    val divider = Color(0xFFDCE8F1)
}

/**
 * View a specific notification's details. Only for Admin view.
 * Cannot be able to delete or modify a notification, an admin can only view.
 *
 *
 */
@Composable
fun AdminNotificationDetailsScreen(
    notification: Notification,
    recipientName: String,
    campsiteName: String,
    onBack: () -> Unit,
    onViewUser: (String) -> Unit,
    onViewCampsite: (String) -> Unit
) {
    val sentDate = remember(notification.timestamp) {
        SimpleDateFormat(
            "MMM d, h:mm a",
            Locale.getDefault()
        ).format(Date(notification.timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminNotificationDetailsColors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Admin View",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = AdminNotificationDetailsColors.charcoal
        )

        Spacer(modifier = Modifier.height(20.dp))


        Text(
            text = "Notification",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium,
            color = AdminNotificationDetailsColors.charcoal
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Back button
        TextButton(
            onClick = onBack
        ) {
            Text(
                text = "← Back",
                color = AdminNotificationDetailsColors.darkBlue
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Notification information card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = AdminNotificationDetailsColors.cardBackground
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Notification title and status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        modifier = Modifier.weight(1f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AdminNotificationDetailsColors.charcoal
                    )

                    Text(
                        text = "Delivered",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AdminNotificationDetailsColors.lightGreen
                    )
                }

                // Recipient
                NotificationDetailField(
                    label = "To",
                    value = recipientName
                )

                // Campsite
                NotificationDetailField(
                    label = "Campsite",
                    value = campsiteName
                )

                // Sent date and time
                NotificationDetailField(
                    label = "Sent",
                    value = sentDate
                )

                // Full notification text
                NotificationDetailField(
                    label = "Full text",
                    value = notification.text
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // View user button
        Button(
            onClick = {
                onViewUser(notification.recipientId)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = notification.recipientId.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AdminNotificationDetailsColors.darkBlue
            )
        ) {
            Text("View User")
        }

        Spacer(modifier = Modifier.height(10.dp))

        // View campsite button
        Button(
            onClick = {
                onViewCampsite(notification.campsiteId)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = notification.campsiteId.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AdminNotificationDetailsColors.mutedBlue
            )
        ) {
            Text("View Campsite")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Read-only. Logs can't be edited or deleted.",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            color = AdminNotificationDetailsColors.mutedBlue
        )
    }
}

@Composable
private fun NotificationDetailField(
    label: String,
    value: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AdminNotificationDetailsColors.mutedBlue
        )

        Text(
            text = value.ifBlank { "Not available" },
            fontSize = 15.sp,
            color = AdminNotificationDetailsColors.charcoal
        )
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminNotificationDetailsScreenPreview() {
    val sampleNotification = Notification(
        id = "notification-1",
        senderId = "system",
        recipientId = "user-1",
        title = "Lottery won",
        text = "Congratulations! You have been selected as a winner for Cedar Valley campsite.",
        campsiteId = "campsite-1",
        timestamp = System.currentTimeMillis(),
        read = false
    )

    MaterialTheme {
        AdminNotificationDetailsScreen(
            notification = sampleNotification,
            recipientName = "Jane Doe",
            campsiteName = "Cedar Valley",
            onBack = {},
            onViewUser = {},
            onViewCampsite = {}
        )
    }
}

