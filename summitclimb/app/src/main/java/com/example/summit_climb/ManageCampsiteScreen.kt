package com.example.summit_climb

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

// Purpose: the owner's menu for one campsite. Shows its name, location, lottery status,
// and a row for reservations, waitlist, sent notifications and QR code.
// Known problems:
//   - It shows the values it is given; nothing feeds it real data yet.
//   - A row only opens its screen once that screen's route exists; until then it
//     shows "Coming soon".

// Summit Climb colours
private val BackgroundBlue = Color(0xFFEAF5FA)
private val DarkTeal = Color(0xFF245563)
private val Orange = Color(0xFFE3963E)
private val Green = Color(0xFF3B8F5A)
private val White = Color(0xFFFFFFFF)
private val SecondaryText = Color(0xFF71818A)

/** The owner's menu for one campsite. */
@Composable
fun ManageCampsiteScreen(
    navController: NavController,
    campsiteName: String,
    location: String,
    lotteryStatus: String,
    activeReservations: Int,
    entrantCount: Int,
    sentCount: Int
) {
    val context = LocalContext.current

    // opens a screen if its route exists, otherwise tells the user it is coming
    fun open(route: String) {
        if (navController.graph.findNode(route) != null) {
            navController.navigate(route)
        } else {
            Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    // green for an open lottery, orange for an upcoming one, grey otherwise
    val statusColor = when (lotteryStatus) {
        "Open" -> Green
        "Upcoming" -> Orange
        else -> SecondaryText
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlue)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // back button
        Row(verticalAlignment = Alignment.CenterVertically) {
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

        // heading, campsite name and location, and the lottery status pill
        Column {
            Text(
                text = "Manage",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = DarkTeal
            )
            Text(
                text = "$campsiteName · $location",
                color = SecondaryText,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = lotteryStatus,
                color = White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .background(statusColor, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        // the menu
        MenuCard("Reservations", "Active, past & cancelled", "$activeReservations active") {
            open("ownerActiveReserve")
        }
        MenuCard("Waitlist", "Entrants in your lottery", "$entrantCount") {
            open("ownerWaitlist")
        }
        MenuCard("Sent notifications", "Messages sent to entrants", "$sentCount") {
            open("ownerSentNotifications")
        }
        MenuCard("QR code", "Share a link to your campsite", null) {
            open("ownerQrCode")
        }
    }
}

/** One tappable card: a title with a short line under it, an optional count, and an arrow. */
@Composable
private fun MenuCard(
    title: String,
    subtitle: String,
    count: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(18.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = DarkTeal,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = SecondaryText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (count != null) {
                Text(
                    text = count,
                    color = DarkTeal,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
            Text(text = "›", color = Orange, fontSize = 28.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ManageCampsitePreview() {
    ManageCampsiteScreen(
        navController = rememberNavController(),
        campsiteName = "Mt. Green Camp",
        location = "Banff, AB",
        lotteryStatus = "No Lottery",
        activeReservations = 3,
        entrantCount = 12,
        sentCount = 29
    )
}