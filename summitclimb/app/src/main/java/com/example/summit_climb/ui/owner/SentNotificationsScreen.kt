package com.example.summit_climb.ui.owner

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.summit_climb.R
import com.example.summit_climb.model.Notification
import com.example.summit_climb.model.NotificationType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val charcoal = Color(0xFF36454F)
private val greyBlue = Color(0xFFC9DEF3)
private val titleColor = Color(0xFF233B4D)
private val pillBlue = Color(0xFFCFE7F5)
private val pillText = Color(0xFF245C86)

/**
 * Displays notifications sent by the campsite owner.
 *
 * Notifications belonging to the same batch are grouped
 * into one card, with the recipient count displayed.
 *
 * @param campsiteId campsite whose notifications are displayed
 * @param campsiteName name of the selected campsite
 * @param currentUserId ID of the current owner
 * @param notifications notifications retrieved from the repository
 * @param onBack action performed when Back is clicked
 */

@Composable
fun SentNotificationsScreen(
    campsiteId: String,
    campsiteName: String,
    currentUserId: String,
    notifications: List<Notification>,
    onBack: () -> Unit
) {
    // Only display notifications sent by this owner
    // for the selected campsite.
    val sentNotifications = notifications
        .filter {
            it.senderId == currentUserId && it.campsiteId == campsiteId
        }

        // group notifications sent together
        .groupBy {
            it.batchId.ifBlank { it.id }
        }

        // convert each group into one display item
        .map { (_, batch) ->
            val firstNotification = batch.first()

            SentNotificationBatch(
                title = firstNotification.title,
                message = firstNotification.text,
                timestamp = batch.maxOf { it.timestamp },
                recipientCount = batch
                    .map { it.recipientId }
                    .distinct()
                    .size
            )
        }
        .sortedByDescending { it.timestamp }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFCFE7F5))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {

        // Mountain header.
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(R.drawable.mountain_header),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            )

            // Header content.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .padding(
                        horizontal = 20.dp,
                        vertical = 14.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Back button.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.offset(x = (-12).dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }

                    Text(
                        text = "Back",
                        color = Color.Black,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Screen title.
                Text(
                    text = "Sent Notifications",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Campsite name.
                Text(
                    text = campsiteName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B8FA8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Notification cards.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {

            if (sentNotifications.isEmpty()) {
                Text(
                    text = "No notifications have been sent yet.",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    sentNotifications.forEach { notification ->
                        SentNotificationCard(notification = notification)
                    }
                }
            }
        }
    }
}

/**
 * Represents one grouped notification batch.
 */
private data class SentNotificationBatch(
    val title: String,
    val message: String,
    val timestamp: Long,
    val recipientCount: Int
)

/**
 * Displays one individual sent notification card.
 */
@Composable
private fun SentNotificationCard(
    notification: SentNotificationBatch
) {
    val formattedDate = SimpleDateFormat(
        "MMM dd",
        Locale.US
    ).format(Date(notification.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Notification title and date.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notification.title,
                    modifier = Modifier.weight(1f),
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = formattedDate,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = charcoal
                )
            }

            // Notification message.
            Text(
                text = notification.message,
                fontFamily = FontFamily.SansSerif,
                fontSize = 13.sp,
                color = charcoal,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            // Recipient count pill.
            Row(
                modifier = Modifier
                    .background(
                        color = pillBlue,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = null,
                    tint = pillText,
                    modifier = Modifier.size(15.dp)
                )

                Text(
                    text = "Sent to ${notification.recipientCount} entrants",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = pillText
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SentNotificationsScreenPreview() {
    SentNotificationsScreen(
        campsiteId = "campsite123",
        campsiteName = "Mountain View Campground",
        currentUserId = "owner123",
        notifications = listOf(
            Notification(
                id = "notification1",
                senderId = "owner123",
                recipientId = "entrant1",
                type = NotificationType.MESSAGE,
                title = "Lottery Results",
                text = "The lottery results are now available. Please check your reservation status.",
                campsiteId = "campsite123",
                batchId = "batch1",
                timestamp = System.currentTimeMillis()
            ),
            Notification(
                id = "notification2",
                senderId = "owner123",
                recipientId = "entrant2",
                type = NotificationType.MESSAGE,
                title = "Lottery Results",
                text = "The lottery results are now available. Please check your reservation status.",
                campsiteId = "campsite123",
                batchId = "batch1",
                timestamp = System.currentTimeMillis()
            )
        ),
        onBack = {}
    )
}