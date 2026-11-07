package com.example.summit_climb.ui.owner

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.summit_climb.ui.Routes

import com.example.summit_climb.ui.charcoal
import com.example.summit_climb.ui.greyBlue
import com.example.summit_climb.ui.white
import com.example.summit_climb.ui.black
import com.example.summit_climb.ui.*


// Displays the owner's management menu for a selected campsite.
// Shows the campsite's name, location, lottery status, reservations,
// waitlist, sent notifications, and QR code options.
//
// Known Issues:
//      - Displays values provided through function parameters rather than loading them directly.
//      - Some menu options may show "Coming soon" if their navigation routes are unavailable.



/**
 * Displays the management menu for a selected campsite.
 *
 * Shows campsite information and provides navigation to reservations,
 * waitlist, sent notifications, and QR code screens.
 *
 * @param navController handles navigation between screens
 * @param campsiteId unique identifier of the campsite
 * @param campsiteName name of the campsite
 * @param location location of the campsite
 * @param lotteryStatus current lottery status
 * @param activeReservations number of active reservations
 * @param entrantCount number of people on the waitlist
 * @param sentCount number of notifications sent
 */
@Composable
fun ManageCampsiteScreen(
    navController: NavController,
    campsiteId: String,
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

    // Colour depends on the lottery status
    val statusColor = when (lotteryStatus) {
        "Open", "Lottery Open" -> lightGreen
        "Drawn", "Winners Drawn" -> lightOrange
        "Closed", "Lottery Closed" -> lightRed
        else -> charcoal
    }

    fun openWaitlist() {
        if (navController.graph.findNode("ownerWaitlist/{campsiteId}") != null) {
            navController.navigate("ownerWaitlist/$campsiteId")
        } else {
            Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(white)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // back button
        val backButton = {
            navController.popBackStack()
            Unit
        }

        Row(
            modifier = Modifier
                .clickable(onClick = backButton)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back Button",
                modifier = Modifier.size(20.dp),
                tint = charcoal
            )

            Text(
                text = "Back",
                color = charcoal,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
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
                color = black
            )
            Text(
                text = "$campsiteName · $location",
                color = charcoal,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = lotteryStatus,
                color = white,
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
            openWaitlist()
        }
        MenuCard("Sent notifications", "Messages sent to entrants", "$sentCount") {
            open("ownerSentNotifications")
        }
        MenuCard("QR code", "Share a link to your campsite", null) {
            open(Routes.MANAGE_QR_CODE)
        }
    }
}

/**
 *
 * Displays a clickable menu card with a title, subtitle, optional count,
 * and navigation arrow.
 *
 * @param title title displayed on the card
 * @param subtitle short description displayed below the title
 * @param count optional count displayed on the card
 * @param onClick action performed when the card is clicked
*/
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
        colors = CardDefaults.cardColors(containerColor = greyBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(18.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = black,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = charcoal,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (count != null) {
                Text(
                    text = count,
                    color = black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
            Text(text = "›", color = charcoal, fontSize = 28.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ManageCampsitePreview() {
    ManageCampsiteScreen(
        navController = rememberNavController(),
        campsiteId = "preview-campsite-id",
        campsiteName = "Mt. Green Camp",
        location = "Banff, AB",
        lotteryStatus = "No Lottery",
        activeReservations = 3,
        entrantCount = 12,
        sentCount = 29
    )
}