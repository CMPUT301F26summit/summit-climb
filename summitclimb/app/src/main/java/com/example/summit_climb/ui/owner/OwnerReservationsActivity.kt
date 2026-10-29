package com.example.summit_climb.ui.owner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

// Temporary UI model until Reservation.kt is available
data class ReservationUiItem(
    val id: String,
    val entrantName: String,
    val dates: String,
    val guests: Int,
    val status: String
)

@Composable
fun OwnerReservationsScreen(
    reservations: List<ReservationUiItem>,
    onReservationClick: (String) -> Unit,
    onRejectClick: (String) -> Unit
) {
    val tabs = listOf("Active", "Past", "Cancelled")

    var selectedTab by remember {
        mutableStateOf("Active")
    }

    var reservationToReject by remember {
        mutableStateOf<ReservationUiItem?>(null)
    }

    // Filter reservations based on the selected tab
    val filteredReservations = reservations.filter { reservation ->
        when (selectedTab) {
            "Active" -> reservation.status == "ACTIVE"
            "Past" -> reservation.status == "COMPLETED"
            "Cancelled" -> reservation.status == "CANCELLED" ||
                    reservation.status == "REJECTED"
            else -> false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Reservations",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Active, Past, Cancelled tabs
        TabRow(
            selectedTabIndex = tabs.indexOf(selectedTab)
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

        // Reservation list
        if (filteredReservations.isEmpty()) {
            Text(
                text = "No ${selectedTab.lowercase()} reservations.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = filteredReservations,
                    key = { it.id }
                ) { reservation ->

                    ReservationCard(
                        reservation = reservation,
                        onClick = {
                            onReservationClick(reservation.id)
                        },
                        onReject = {
                            reservationToReject = reservation
                        }
                    )
                }
            }
        }
    }

    // Confirmation dialog before rejecting a reservation
    reservationToReject?.let { reservation ->

        AlertDialog(
            onDismissRequest = {
                reservationToReject = null
            },
            title = {
                Text("Reject reservation?")
            },
            text = {
                Text(
                    "Are you sure you want to reject " +
                            "${reservation.entrantName}'s reservation " +
                            "for ${reservation.dates}?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRejectClick(reservation.id)
                        reservationToReject = null
                    }
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        reservationToReject = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ReservationCard(
    reservation: ReservationUiItem,
    onClick: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = reservation.entrantName,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = reservation.dates,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "${reservation.guests} guests",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Display reservation status
            Text(
                text = reservation.status.lowercase()
                    .replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelMedium
            )

            // Only active reservations can be rejected
            if (reservation.status == "ACTIVE") {

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onReject
                ) {
                    Text("Reject")
                }
            }
        }
    }
}

// Sample data for previewing the screen
@Preview(showBackground = true)
@Composable
private fun OwnerReservationsPreview() {

    val sampleReservations = listOf(
        ReservationUiItem(
            id = "1",
            entrantName = "Ava Thompson",
            dates = "November 9–11, 2026",
            guests = 4,
            status = "ACTIVE"
        ),
        ReservationUiItem(
            id = "2",
            entrantName = "Liam Chen",
            dates = "November 17–19, 2026",
            guests = 2,
            status = "ACTIVE"
        ),
        ReservationUiItem(
            id = "3",
            entrantName = "Sophia Martin",
            dates = "September 12–14, 2026",
            guests = 3,
            status = "COMPLETED"
        ),
        ReservationUiItem(
            id = "4",
            entrantName = "Emma Wilson",
            dates = "October 3–5, 2026",
            guests = 2,
            status = "CANCELLED"
        ),
        ReservationUiItem(
            id = "5",
            entrantName = "Noah Patel",
            dates = "October 20–22, 2026",
            guests = 4,
            status = "REJECTED"
        )
    )

    MaterialTheme {
        OwnerReservationsScreen(
            reservations = sampleReservations,
            onReservationClick = {},
            onRejectClick = {}
        )
    }
}