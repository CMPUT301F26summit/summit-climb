package com.example.summit_climb.ui.owner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun OwnerReservationDetailsScreen(
    entrantName: String,
    dates: String,
    nights: Int,
    guests: Int,
    totalPrice: String,
    status: String,
    rulesAccepted: Boolean,
    onMessageClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    var showRejectDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Reservation",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = entrantName,
                    style = MaterialTheme.typography.titleLarge
                )

                Text("Status: $status")

                Spacer(modifier = Modifier.height(16.dp))

                Text("Dates: $dates")
                Text("Length: $nights nights")
                Text("Guests: $guests")
                Text("Total: $totalPrice")

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    if (rulesAccepted) {
                        "Campsite rules: Accepted"
                    } else {
                        "Campsite rules: Not accepted"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onMessageClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Message Entrant")
        }

        if (status == "ACTIVE") {
            OutlinedButton(
                onClick = { showRejectDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reject Reservation")
            }
        }
    }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject reservation?") },
            text = {
                Text("Are you sure you want to reject this reservation?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRejectClick()
                        showRejectDialog = false
                    }
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRejectDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}