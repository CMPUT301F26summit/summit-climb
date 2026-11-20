package com.example.summit_climb.ui.entrant

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.summit_climb.controller.NotificationController
import com.example.summit_climb.model.Notification
import com.example.summit_climb.model.NotificationType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import com.example.summit_climb.ui.charcoal
import com.example.summit_climb.ui.greyBlue
import com.example.summit_climb.ui.lightBlue
import com.example.summit_climb.ui.black
import androidx.compose.foundation.background
import com.example.summit_climb.ui.white
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.summit_climb.data.NotificationRepository
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import com.example.summit_climb.ui.Routes
import com.example.summit_climb.ui.ScreensBackground
import com.example.summit_climb.ui.StatusBadge
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.ChatBubbleOutline
import com.example.summit_climb.ui.StatusBadge


/**
 * Displays the current user's notifications.
 *
 * Shows unread notifications, notification messages, and timestamps.
 * Allows users to mark notifications as read and navigate to available
 * related screens
 *
 * Known Issues:
 *  - Some notifications types do not have navigation destination yet.
 *  - Notifications are not displayed in the phone's notification bar.
 */

@Composable
fun NotificationsScreen(
    controller: NotificationController,
    navController: NavController,
    notificationsEnabled: Boolean
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        ScreensBackground()

        // the whole screen is one list, so everything scrolls together
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // back button
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = black
                        )
                    }
                    Text(
                        text = "Back",
                        color = black,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable { navController.popBackStack() }
                    )
                }
            }

            // heading and unread count
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = white
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Notifications",
                                fontSize = 35.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = black
                            )

                            Text(
                                text = "Stay Updated on Your Activity",
                                fontSize = 12.sp,
                                color = charcoal.copy(alpha = 0.65f)
                            )
                        }

                        StatusBadge(
                            text = "${controller.unreadCount} unread",
                            color = lightBlue
                        )
                    }
                }
            }

            item{
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Mark all as read",
                        color = charcoal,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            controller.notifications
                                .filter { !it.read }
                                .forEach { controller.markRead(it.id) }
                        }
                    )
                }
            }

            if (!notificationsEnabled) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Notifications Disabled",
                                color = charcoal,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "You have opted out of notifications. You will not receive new notifications.",
                                color = charcoal,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = {
                                    navController.navigate(Routes.PROFILE)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = lightBlue
                                )
                            ) {
                                Text(
                                    text = "Turn notifications back on",
                                    color = black,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // the notifications
            if (controller.notifications.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No notifications yet",
                            color = black
                        )
                    }
                }
            } else {
                items(
                    items = controller.notifications,
                    key = { it.id }
                ) { notification ->
                    NotificationCard(
                        notification = notification,
                        onClick = {
                            if (!notification.read) controller.markRead(notification.id)
                            controller.destinationFor(notification)?.let { route ->
                                // only go if that screen exists yet; otherwise the app would crash
                                if (navController.graph.findNode(route) != null) {
                                    navController.navigate(route)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit
) {
    val header = when (notification.type) {
        NotificationType.LOTTERY_WIN -> "You won the lottery!"
        NotificationType.LOTTERY_LOSS -> "Lottery Not Selected"
        NotificationType.RESERVATION_UPDATE -> "Reservation Notice"
        NotificationType.MESSAGE -> "New Message"

    }
    // Notifications received within the last 24 hours are labelled NEW.
    val isNew = System.currentTimeMillis() - notification.timestamp <
            24 * 60 * 60 * 1000

    val status = when {
        notification.read -> "READ"
        isNew -> "NEW"
        else -> "UNREAD"
    }

    val icon = when (notification.type) {
        NotificationType.LOTTERY_WIN -> Icons.Default.EmojiEvents
        NotificationType.LOTTERY_LOSS -> Icons.Default.ConfirmationNumber
        NotificationType.RESERVATION_UPDATE -> Icons.Default.EventAvailable
        NotificationType.MESSAGE -> Icons.Default.ChatBubbleOutline
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read) greyBlue else lightBlue
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = charcoal
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp)
                ) {
                    Text(
                        text = header,
                        color = black,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = DateUtils.getRelativeTimeSpanString(
                            notification.timestamp,
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS
                        ).toString(),
                        color = charcoal,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (!notification.read) {
                    StatusBadge(
                        text = status,
                        color = charcoal
                    )
                }
            }

            Text(
                text = notification.text,
                color = charcoal,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NotificationsScreenPreview() {

    // Sample notifications for the preview
    val sampleNotifications = listOf(
        Notification(
            id = "1",
            senderId = "system",
            recipientId = "preview-user",
            type = NotificationType.LOTTERY_WIN,
            title = "Lottery Result",
            text = "Congratulations! You have been selected for your campsite reservation at Pine Ridge Campground.",
            timestamp = System.currentTimeMillis() - 5 * 60 * 1000,
            read = false
        ),

        Notification(
            id = "2",
            senderId = "system",
            recipientId = "preview-user",
            type = NotificationType.RESERVATION_UPDATE,
            title = "Reservation Update",
            text = "Your reservation has been confirmed. Check your reservations for more details.",
            timestamp = System.currentTimeMillis() - 60 * 60 * 1000,
            read = false
        ),

        Notification(
            id = "3",
            senderId = "system",
            recipientId = "preview-user",
            type = NotificationType.LOTTERY_LOSS,
            title = "Lottery Result",
            text = "Unfortunately, you were not selected for this campsite lottery. Better luck next time!",
            timestamp = System.currentTimeMillis() - 24 * 60 * 60 * 1000,
            read = true
        ),

        Notification(
            id = "4",
            senderId = "owner123",
            recipientId = "preview-user",
            type = NotificationType.MESSAGE,
            title = "New Message",
            text = "Hello! Please remember to review the campsite guidelines before your upcoming visit.",
            timestamp = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000,
            read = true
        )
    )

    // Temporary repository so the preview does not need Firestore
    val previewRepository = object : NotificationRepository {

        override val notifications = sampleNotifications

        override fun send(notification: Notification) {}

        override fun markRead(notificationId: String) {}

        override fun forUser(userId: String): List<Notification> {
            return sampleNotifications
        }

        override fun sentBy(senderId: String): List<Notification> {
            return sampleNotifications
        }

        override fun listenToUser(
            userId: String,
            onAdded: (Notification) -> Unit,
            onModified: (Notification) -> Unit,
            onRemoved: (Notification) -> Unit
        ): () -> Unit {
            return {}
        }
    }

    val previewController = remember {
        NotificationController(previewRepository) {
            "preview-user"
        }.apply {
            notifications.addAll(sampleNotifications)
        }
    }

    NotificationsScreen(
        controller = previewController,
        navController = rememberNavController(),
        notificationsEnabled = true
    )
}

