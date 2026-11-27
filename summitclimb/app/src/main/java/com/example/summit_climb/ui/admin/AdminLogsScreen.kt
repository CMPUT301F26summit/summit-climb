package com.example.summit_climb.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.example.summit_climb.model.Campsite
import com.example.summit_climb.model.User
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.summit_climb.model.Notification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.summit_climb.R
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow


private object AdminReviewColors {
    val lightBlue = Color(0xFFE4EDF5)
    val charcoal = Color(0xFF36454F)
    val mutedBlue = Color(0xFF597A94)
    val divider = Color(0xFFDCE8F1)
}

/**
 * Type of Image
 */
enum class AdminImageType {
    CAMPSITE,
    PROFILE
}

/**
 * Image data
 */
data class AdminImageItem(
    val id: String,
    val imageUrl: String,
    val name: String,
    val subtitle: String? = null,
    val type: AdminImageType
)

/**
 * Admin page for reviewing images, notifications, and messages.
 */
@Composable
fun AdminLogsScreen(
    campsites: List<Campsite>,
    users: List<User>,
    notifications: List<Notification>,
    onRemoveImage: (AdminImageItem, (Boolean) -> Unit) -> Unit,
    onNotificationClick: (Notification) -> Unit,
    onBack: () -> Unit,
    conversations: List<List<Notification>> = emptyList(),
    onConversationClick: (List<Notification>) -> Unit = {}
    ) {
    var selectedTab by rememberSaveable() { mutableStateOf("Images") }

    var searchQuery by remember { mutableStateOf("") }

    val tabs = listOf("Images", "Notifications", "Messages")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFCFE7F5))
    ) {
        // Mountain header image
        Image(
            painter = painterResource(R.drawable.mountain_header),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Back button
            TextButton(
                onClick = onBack,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "← Back",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF245C86)
                )
            }

            // Main title
            Text(
                text = "Review",
                fontFamily = FontFamily.SansSerif,
                fontSize = 30.sp,
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF1C3348),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE4EDF5)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (tab in tabs) {
                    Box(
                        modifier = Modifier
                            .weight(if (tab == "Notifications") 1.5f else 1f)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selectedTab == tab) Color.White
                                else Color.Transparent
                            )
                            .clickable {
                                selectedTab = tab
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            fontWeight = if (selectedTab == tab)
                                FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp,
                            color = if (selectedTab == tab)
                                Color(0xFF245C86) else Color(0xFF597A94),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search...",
                        color = Color(0xFF777777)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF36454F)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedTextColor = Color(0xFF36454F),
                    unfocusedTextColor = Color(0xFF36454F),
                    cursorColor = Color(0xFF36454F)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                "Images" -> {
                    AdminImagesContent(
                        campsites = campsites,
                        users = users,
                        searchQuery = searchQuery,
                        onRemoveImage = onRemoveImage
                    )
                }

                "Notifications" -> {
                    AdminNotificationsContent(
                        notifications = notifications,
                        users = users,
                        campsites = campsites,
                        searchQuery = searchQuery,
                        onNotificationClick = onNotificationClick
                    )
                }

                "Messages" -> {
                    AdminMessagesContent(
                        conversations = conversations,
                        users = users,
                        campsites = campsites,
                        searchQuery = searchQuery,
                        onConversationClick = onConversationClick
                    )
                }
            }
        }
    }
}

/**
 * Image Tab for viewing campsite and profile pictures
 */
@Composable
fun AdminImagesContent(
    campsites: List<Campsite>,
    users: List<User>,
    searchQuery: String,
    onRemoveImage: (AdminImageItem, (Boolean) -> Unit) -> Unit
) {
    var selectedFilter by remember {
        mutableStateOf("All")
    }

    var imageToRemove by remember {
        mutableStateOf<AdminImageItem?>(null)
    }

    var showRemoveError by remember {
        mutableStateOf(false)
    }

    val campsiteImages = campsites.mapNotNull { campsite ->
        if (campsite.promoImageUrl.isBlank()) {
            null
        } else {
            val owner = users.find {
                it.userId == campsite.ownerId
            }

            AdminImageItem(
                id = campsite.id,
                imageUrl = campsite.promoImageUrl,
                name = campsite.name,
                subtitle = "Host: ${owner?.name ?: "Unknown"}",
                type = AdminImageType.CAMPSITE
            )
        }
    }

    val profileImages = users.mapNotNull { user ->
        if (user.profileImageUrl.isBlank()) {
            null
        } else {
            AdminImageItem(
                id = user.userId,
                imageUrl = user.profileImageUrl,
                name = user.name,
                type = AdminImageType.PROFILE
            )
        }
    }

    val allImages = campsiteImages + profileImages

    val query = searchQuery.trim()

    val filteredImages = when (selectedFilter) {
        "Campsites" -> campsiteImages
        "Profiles" -> profileImages
        else -> allImages
    }.filter { item ->
        item.name.contains(query, ignoreCase = true) ||
                (item.subtitle?.contains(query, ignoreCase = true) == true)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Campsites", "Profiles").forEach { filter ->

                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = {
                        selectedFilter = filter
                    },
                    label = {
                        Text(filter)
                    }
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = filteredImages,
                key = { "${it.type}_${it.id}" }
            ) { item ->

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        IconButton(
                            onClick = {
                                imageToRemove = item
                            },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove image",
                                tint = Color.Red
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFF36454F)
                    )

                    // campsite images text
                    item.subtitle?.let { subtitle ->
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }



                imageToRemove?.let { item ->
                    val ownerName = item.subtitle?.removePrefix("Host: ")

                    AlertDialog(
                        onDismissRequest = {
                            imageToRemove = null
                        },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(
                                        color = Color(0xFFF8E3E1),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove image",
                                    tint = Color(0xFFB63B30),
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                        },
                        title = {
                            Text(
                                text = "Remove Image?",
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif,
                                color = Color(0xFF222222),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = buildString {
                                        append(item.name)

                                        if (ownerName != null) {
                                            append(" · ")
                                            append(ownerName)
                                        }
                                    },
                                    fontSize = 17.sp,
                                    color = Color(0xFF555555),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Are you sure you want to remove image?",
                                    fontSize = 16.sp,
                                    color = Color(0xFF555555),
                                    textAlign = TextAlign.Center
                                )
                            }
                        },
                        confirmButton = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        imageToRemove = null
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(58.dp),
                                    shape = RoundedCornerShape(50.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        Color(0xFFD2D2D2)
                                    )
                                ) {
                                    Text(
                                        text = "Cancel",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF222222)
                                    )
                                }

                                Button(
                                    onClick = {
                                        onRemoveImage(item) { success ->
                                            imageToRemove = null

                                            if (!success) {
                                                showRemoveError = true
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(58.dp),
                                    shape = RoundedCornerShape(50.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFB63B30),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(
                                        text = "Remove",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(32.dp),
                        containerColor = Color.White,
                        tonalElevation = 0.dp
                    )
                }
            }
        }
    }
}

/**
 * Notification tab for viewing all notifications
 */
@Composable
private fun AdminNotificationsContent(
    notifications: List<Notification>,
    users: List<User>,
    campsites: List<Campsite>,
    searchQuery: String,
    onNotificationClick: (Notification) -> Unit
) {
    val filteredNotifications = notifications
        .sortedByDescending { it.timestamp }
        .filter { notification ->
            val recipient = users.find {
                it.userId == notification.recipientId
            }

            val campsite = campsites.find {
                it.id == notification.campsiteId
            }

            notification.title.contains(searchQuery, ignoreCase = true) ||
                    recipient?.name?.contains(searchQuery, ignoreCase = true) == true ||
                    campsite?.name?.contains(searchQuery, ignoreCase = true) == true
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AdminReviewColors.lightBlue
        )
    ) {
        Column {
            filteredNotifications.forEachIndexed { index, notification ->
                val recipient = users.find {
                    it.userId == notification.recipientId
                }

                val campsite = campsites.find {
                    it.id == notification.campsiteId
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onNotificationClick(notification)
                        }
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = notification.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = AdminReviewColors.charcoal
                        )

                        Text(
                            text = remember(notification.timestamp) {
                                SimpleDateFormat(
                                    "MMM d",
                                    Locale.getDefault()
                                ).format(Date(notification.timestamp))
                            },
                            fontSize = 12.sp,
                            color = AdminReviewColors.mutedBlue
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "To: ${recipient?.name ?: "Unknown"} * ${campsite?.name ?: "Unknown"}",
                            fontSize = 12.sp,
                            color = AdminReviewColors.mutedBlue
                        )

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View notification details",
                            tint = AdminReviewColors.mutedBlue
                        )
                    }
                }

                if (index < filteredNotifications.lastIndex) {
                    HorizontalDivider(
                        color = AdminReviewColors.divider,
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

/**
 * Displays private conversations for admin review.
 */
@Composable
private fun AdminMessagesContent(
    conversations: List<List<Notification>>,
    users: List<User>,
    campsites: List<Campsite>,
    searchQuery: String,
    onConversationClick: (List<Notification>) -> Unit
) {
    val filteredConversations = conversations.filter { conversation ->
        val message = conversation.last()
        val sender = users.find { it.userId == message.senderId }
        val recipient = users.find { it.userId == message.recipientId }
        val campsite = campsites.find { it.id == message.campsiteId }

        searchQuery.isBlank() ||
                sender?.name?.contains(searchQuery, ignoreCase = true) == true ||
                recipient?.name?.contains(searchQuery, ignoreCase = true) == true ||
                campsite?.name?.contains(searchQuery, ignoreCase = true) == true
    }

    if (filteredConversations.isEmpty()) {
        Text(
            text = "No conversations found.",
            color = AdminReviewColors.mutedBlue
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredConversations) { conversation ->
                val message = conversation.last()
                val sender = users.find { it.userId == message.senderId }
                val recipient = users.find { it.userId == message.recipientId }
                val campsite = campsites.find { it.id == message.campsiteId }
                val locale = LocalConfiguration.current.locales[0]
                val date = SimpleDateFormat(
                    "MMM d",
                    locale
                ).format(Date(message.timestamp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onConversationClick(conversation)
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AdminReviewColors.lightBlue
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${sender?.name ?.ifBlank { "Unknown" } ?: "Unknown"} → ${recipient?.name?.ifBlank { "Unknown" } ?: "Unknown"}",
                                fontWeight = FontWeight.Bold,
                                color = AdminReviewColors.charcoal,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = date,
                                fontSize = 12.sp,
                                color = AdminReviewColors.mutedBlue
                            )
                        }

                        Text(
                            text = campsite?.name ?: "Unknown campsite",
                            color = AdminReviewColors.mutedBlue,
                            fontSize = 13.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = message.text,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                                color = AdminReviewColors.charcoal
                            )

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "View conversation",
                                tint = AdminReviewColors.mutedBlue
                            )
                        }
                    }
                }
            }
        }
    }
}
