package com.example.summit_climb.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

/**
 * Admin page for reviewing images, notifications, and messages.
 */
@Composable
fun AdminLogsScreen() {
    var selectedTab by remember { mutableStateOf("Images") }

    val tabs = listOf("Images", "Notifications", "Messages")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Admin Review",
            style = MaterialTheme.typography.headlineMedium
        )

        TabRow(
            selectedTabIndex = tabs.indexOf(selectedTab),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            containerColor = Color.White,
            contentColor = Color(0xFF245C86),
            divider = {}
        ) {
            tabs.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when(selectedTab) {
            "Images" -> Text("Image logs will appear here")
            "Notifications" -> Text("Notifications logs will appear here.")
            "Messages" -> Text("Message logs will appear here")
        }
    }
}