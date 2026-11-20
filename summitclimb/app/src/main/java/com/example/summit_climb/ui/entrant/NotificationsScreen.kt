package com.example.summit_climb.ui.entrant

import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import com.example.summit_climb.R
import androidx.compose.ui.text.style.TextAlign
import com.example.summit_climb.ui.Routes
import com.example.summit_climb.ui.ScreensBackground


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
    if (!notificationsEnabled && controller.notifications.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFCFE7F5))
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Smaller notification icon
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                Color(0xFFCFE7F5),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsOff,
                            contentDescription = "Notifications disabled",
                            tint = Color(0xFF245C86),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = "Notifications Disabled",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF233B4D),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "You have opted out of notifications. You will not receive new notifications.",
                        fontSize = 14.sp,
                        color = Color(0xFF526675),
                        textAlign = TextAlign.Center,
                        lineHeight = 21.sp
                    )

                    HorizontalDivider(
                        color = Color(0xFFE5EDF3),
                        thickness = 1.dp
                    )

                    Button(
                        onClick = {
                            navController.navigate(Routes.PROFILE)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = lightBlue
                        ),
                        contentPadding = PaddingValues(vertical = 13.dp)
                    ) {
                        Text(
                            text = "Turn notifications back on",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Keep the header separate so notification cards scroll beneath it
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFCFE7F5))
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
            ) {
                // back button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.offset(x = (-12).dp)
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Text(
                        text = "Back",
                        color = black,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable { navController.popBackStack() }
                    )
                }
                // heading and unread count
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Notifications",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF233B4D)
                    )

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "${controller.unreadCount} unread",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF245C86),
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 2.dp)
                        )

                        Text(
                            text = "Mark all as read",
                            color = Color(0xFF245C86),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                controller.notifications
                                    .filter { !it.read }
                                    .forEach { notification ->
                                        controller.markRead(notification.id)
                                    }
                            }
                        )
                    }
                }
            }

            // The header stays fixed while notification cards scroll
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(
                    start = 10.dp,
                    end = 20.dp,
                    top = 16.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                                .padding(top = 150.dp),
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFCFE7F5)),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read) {
                Color.White
            } else {
                Color(0xFFCFE7F5)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = header,
                    color = Color(0xFF233B4D),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.weight(1f))

                if (!notification.read) {
                    Text(
                        text = "NEW",
                        color = charcoal,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // smaller notification message
            Text(
                text = notification.text,
                color = charcoal,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp)
            )

            // notification timestamp
            Text(
                text = DateUtils.getRelativeTimeSpanString(
                    notification.timestamp,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                ).toString(),
                color = charcoal,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 12.dp)
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
        notificationsEnabled = false
    )
}

