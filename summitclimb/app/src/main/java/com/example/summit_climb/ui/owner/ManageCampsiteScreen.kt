package com.example.summit_climb.ui.owner

import android.service.autofill.OnClickAction
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.summit_climb.ui.Routes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import com.example.summit_climb.R

import com.example.summit_climb.ui.charcoal
import com.example.summit_climb.ui.greyBlue
import com.example.summit_climb.ui.white
import com.example.summit_climb.ui.black
import com.example.summit_climb.ui.*
import com.example.summit_climb.ui.entrant.statusColor


// Displays the owner's management menu for a selected campsite.
// Shows the campsite's name, location, lottery status, reservations,
// waitlist, sent notifications, and QR code options.
//
// Known Issues:
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
 */
@Composable
fun ManageCampsiteScreen(
    navController: NavController,
    campsiteId: String,
    campsiteName: String,
    location: String,
    lotteryStatus: String,
    activeReservations: Int,
    entrantCount: Int
) {
    val context = androidx.compose.ui.platform.LocalContext.current

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

    fun openSentNotifications() {
        if (navController.graph.findNode(Routes.OWNER_SENT_NOTIFICATIONS) != null) {
            navController.navigate(Routes.ownerSentNotifications(campsiteId))
        } else {
            Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    fun openReservations() {
        if (navController.graph.findNode(Routes.OWNER_ACTIVE_RESERVATIONS) != null) {
            navController.navigate(Routes.ownerActiveReservations(campsiteId))
        } else {
            Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Mountain header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
                        .fillMaxSize()
                        .background(Color(0x660F2938))
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 14.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // back button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.offset(x=(-12).dp)
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

                    Spacer(modifier = Modifier.height(4.dp))

                    // Same title style as Notifications.
                    Text(
                        text = "Manage",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF233B4D)
                    )

                    // Campsite name, location, and lottery
                    // status displayed beside one another.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = campsiteName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B8FA8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Text(
                            text = " • ",
                            fontSize = 15.sp,
                            color = Color(0xFF36454F)
                        )

                        Text(
                            text = location,
                            modifier = Modifier.weight(1f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6B8FA8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        StatusBadge(
                            text = lotteryStatus,
                            color = statusColor
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 18.dp
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 1.dp
                )
            ) {
                Column {

                    // Reservations
                    MenuOption(
                        title = "Reservations",
                        subtitle = "Active, past & cancelled",
                        icon = Icons.Filled.CalendarMonth,
                        count = "$activeReservations active"
                    ) {
                        openReservations()
                    }

                    HorizontalDivider(
                        color = Color(0xFFE1EAF0)
                    )

                    // Waitlist
                    MenuOption(
                        title = "Waitlist",
                        subtitle = "Entrants in your lottery",
                        icon = Icons.Filled.Groups,
                        count = "$entrantCount entrants",
                        countPill = true
                    ) {
                        openWaitlist()
                    }

                    HorizontalDivider(
                        color = Color(0xFFE1EAF0)
                    )

                    // Sent notifications
                    MenuOption(
                        title = "Sent notifications",
                        subtitle = "Messages sent to entrants",
                        icon = Icons.Filled.Notifications
                    ) {
                        openSentNotifications()
                    }

                    HorizontalDivider(
                        color = Color(0xFFE1EAF0)
                    )

                    // QR code
                    MenuOption(
                        title = "QR code",
                        subtitle = "Share a link to your campsite",
                        icon = Icons.Filled.QrCode2
                    ) {
                        navController.navigate(
                            Routes.manageQRCode(campsiteId)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    count: String ? = null,
    countPill: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 16.dp,
                vertical = 18.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Icon inside a light-blue pill.
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    Color(0xFFCFE7F5),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF245C86),
                modifier = Modifier.size(23.dp)
            )
        }

        // Option title and subtitle.
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        ) {
            Text(
                text = title,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF233B4D),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF36454F),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Optional count.
        if (count != null) {
            if (countPill) {
                Text(
                    text = count,
                    color = Color(0xFF245C86),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .background(
                            Color(0xFFCFE7F5),
                            RoundedCornerShape(50)
                        )
                        .padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        )
                )
            } else {
                Text(
                    text = count,
                    color = Color(0xFF233B4D),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Open",
            tint = Color(0xFF536775),
            modifier = Modifier
                .size(18.dp)
                .background(Color.Transparent)
        )
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
        lotteryStatus = "Winners Drawn",
        activeReservations = 3,
        entrantCount = 12
    )
}