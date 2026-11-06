package com.example.summit_climb

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.summit_climb.controller.NotificationController

@Composable
fun EntrantCampsiteList(
    navController: NavController,
    notificationController: NotificationController
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Campsite list coming soon")

        // notifications icon in the top-right corner; orange when something is unread
        IconButton(
            onClick = {
                navController.navigate("entrantNotifications")
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 20.dp, top = 32.dp)
                .size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = if (notificationController.unreadCount > 0) {
                    Color(0xFFE3963E)
                } else {
                    LocalContentColor.current
                },
                modifier = Modifier.size(36.dp)
            )
        }
    }
}