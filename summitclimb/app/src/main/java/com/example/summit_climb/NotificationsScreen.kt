
package com.example.summit_climb

import android.text.format.DateUtils
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.summit_climb.controller.NotificationController
import com.example.summit_climb.model.Notification
import com.example.summit_climb.model.NotificationType
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.ExtraBold
import androidx.compose.ui.unit.sp
import com.example.summit_climb.R
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding

// Summit Climb colours
private val BackgroundBlue = Color(0xFFEAF5FA)
private val DarkTeal = Color(0xFF245563)
private val UnreadBlue = Color(0xFFD8EAF3)
private val White = Color(0xFFFFFFFF)
private val SecondaryText = Color(0xFF71818A)
private val Orange = Color(0xFFE99A3D)

/** Shows the logged-in user's notifications. */
@Composable
fun NotificationsScreen(
    controller: NotificationController,
    navController: NavController
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // summit climb background image
        Image(
            painter = painterResource(id = R.drawable.summit_climb_no_text),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

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
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = DarkTeal
                        )
                    }
                    Text(
                        text = "Back",
                        color = DarkTeal,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable { navController.popBackStack() }
                    )
                }
            }

            // heading, unread count, and the temporary test button
            item {
                Column {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = DarkTeal
                    )
                    Text(
                        text = "${controller.unreadCount} unread",
                        color = SecondaryText,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                    )
                }
            }

            // the notifications
            if (controller.notifications.isEmpty()) {
                item {
                    Text(
                        text = "You don't have any notifications yet.",
                        color = DarkTeal,
                        modifier = Modifier.padding(top = 12.dp)
                    )
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read) {
                White
            } else {
                UnreadBlue
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
                    color = Color(0xFFE3963E),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.weight(1f))

                if (!notification.read) {
                    Text(
                        text = "NEW",
                        color = Orange,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // smaller notification message
            Text(
                text = notification.text,
                color = DarkTeal,
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
                color = SecondaryText,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}