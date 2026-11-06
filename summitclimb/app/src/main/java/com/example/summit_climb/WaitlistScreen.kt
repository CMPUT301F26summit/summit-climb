package com.example.summit_climb

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

 // Displays the waitlist for a campsite.
 //
 // Shows the campsite's lottery status, entrant count, winners drawn date,
 // and list of entrants. Allows campsite owners to view all entrants and
 // notify them
 //
 // Known Issues:
 // - Entrant information is currently provided through the function parameters.

private val black = Color.Black
private val charcoal = Color(0xFF36454F)

private val lightGreen = Color(0xFF96B38E)
private val lightRed = Color(0xFFFF7F7F)
private val lightOrange = Color(0xFFFFA759)

private val lightBlue = Color(0xFFA6C8EB)
private val greyBlue = Color(0xFFC9DEF3)

private val white = Color.White

/**
 * Represents a person who has joined a campsite waitlist.
 *
 * @property id unique identifier of the entrant
 * @property name name of the entrant
 * @property joinedDate date the entrant joined the waitlist
 */
data class Entrant(
    val id: String,
    val name: String,
    val joinedDate: String
)

/**
 * Displays the waitlist screen for a selected campsite.
 *
 *  @param campsiteName name of the campsite
 *  @param entrants list of people who joined the waitlist
 *  @param lotteryStatus current status of the lottery
 *  @param entrantCount total number of entrants
 *  @param winnersDrawnDate date the winners were drawn
 *  @param maxEntrants maximum number of entrants, or null if there is no limit
 *  @param onBack action performed when the back button is clicked
 *  @param onNotifyEntrants action performed when the notify button is clicked
 */
@Composable
fun WaitlistScreen(
    campsiteName: String,
    entrants: List<Entrant>,
    lotteryStatus: String,
    entrantCount: Int,
    winnersDrawnDate: String,
    maxEntrants: Int?,
    onBack: () -> Unit,
    onNotifyEntrants: () -> Unit
) {
    var showAllEntrants by remember { mutableStateOf(false) }

    val visibleEntrants =
        if (showAllEntrants) entrants else entrants.take(5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(white)
            .padding(20.dp)
    ) {

        // Back button
        TextButton(
            onClick = onBack,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "← Back",
                color = charcoal,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Screen title
        Text(
            text = "Waitlist",
            style = MaterialTheme.typography.headlineMedium,
            color = black,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campsite name and lottery status
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = campsiteName,
                style = MaterialTheme.typography.titleMedium,
                color = black,
                fontWeight = FontWeight.SemiBold
            )
            val statusColor = when (lotteryStatus) {
                "Open", "Lottery Open" -> lightGreen
                "Drawn", "Winners Drawn" -> lightOrange
                "Closed", "Lottery Closed" -> lightRed
                else -> charcoal
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = statusColor
            ) {
                Text(
                    text = lotteryStatus,
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = white
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Entrant statistics
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = greyBlue
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Entrants joined
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (maxEntrants != null) {
                            "$entrantCount/$maxEntrants"
                        } else {
                            entrantCount.toString()
                        },
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = black

                    )
                    Text(
                        text = "Entrants joined",
                        style = MaterialTheme.typography.bodyMedium,
                        color = charcoal

                    )
                }

                // divider between stats
                VerticalDivider(
                    modifier = Modifier.height(55.dp),
                    color = charcoal.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Winners drawn
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = winnersDrawnDate,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = black

                    )
                    Text(
                        text = "Winners drawn",
                        style = MaterialTheme.typography.bodyMedium,
                        color = charcoal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Entrants",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = black
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Entrant list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(visibleEntrants, key = { it.id }) { entrant ->
                EntrantRow(entrant = entrant)
            }

            if (!showAllEntrants && entrants.size > 5) {
                item {
                    TextButton(
                        onClick = { showAllEntrants = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "+ ${entrants.size - 5} more entrants",
                            color = charcoal
                        )
                    }
                }
            }

            if (showAllEntrants && entrants.size > 5) {
                item {
                    TextButton(
                        onClick = { showAllEntrants = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Show less",
                            color = charcoal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Notify button
        Button(
            onClick = onNotifyEntrants,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = lightBlue,
                contentColor = black
            )
        ) {
            Text(
                text = "Notify Entrants",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Displays one entrant's information in the waitlist.
 *
 * Shows the entrant's initials, name, and joined date.
 *
 * @param entrant entrant whose information is displayed
 */
@Composable
fun EntrantRow(entrant: Entrant) {

    val initials = entrant.name
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Initials circle
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    color = lightBlue,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                fontWeight = FontWeight.Bold,
                color = black
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = entrant.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = black
            )

            Text(
                text = "Joined ${entrant.joinedDate}",
                style = MaterialTheme.typography.bodySmall,
                color = charcoal            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WaitlistScreenPreview() {
    MaterialTheme {
        WaitlistScreen(
            campsiteName = "Mt. Green Camp",
            winnersDrawnDate = "Oct 30",
            lotteryStatus = "Lottery Open",
            entrantCount = 12,
            maxEntrants = 50,
            entrants = listOf(
                Entrant("1", "Jordan Reid", "Oct 20"),
                Entrant("2", "Priya Shah", "Oct 20"),
                Entrant("3", "Marcus Brown", "Oct 21"),
                Entrant("4", "Hana Nakamura", "Oct 22"),
                Entrant("5", "Ethan Lavoie", "Oct 23"),
                Entrant("6", "Entrant Six", "Oct 24"),
                Entrant("7", "Entrant Seven", "Oct 24"),
                Entrant("8", "Entrant Eight", "Oct 25"),
                Entrant("9", "Entrant Nine", "Oct 26"),
                Entrant("10", "Entrant Ten", "Oct 27"),
                Entrant("11", "Entrant Eleven", "Oct 28"),
                Entrant("12", "Entrant Twelve", "Oct 29")
            ),
            onBack = {},
            onNotifyEntrants = {}
        )
    }
}
