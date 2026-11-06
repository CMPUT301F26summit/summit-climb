package com.example.summit_climb

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.Locale

/**
 * The entrant's campsite list screen: search bar, filters, and the matching campsites.
 * The list updates as the user types and whenever Firestore changes.
 * Tapping a campsite opens `CampsiteDetailsScreen/{campsiteId}`.
 * Tapping "Scan QR code" opens `QRScanner` (screen).
 * The bottom navigation bar has Campsites (this screen), My Camps (OwnerCampsitesScreen)
 * and Profile (ProfileScreen).
 *
 * @param navController: app's navigation controller, used to open other screens.
 * @param searchController: finds the campsites to show.
 */
@Composable
fun CampsiteListScreen(
    navController: NavController,
    searchController: CampsiteSearchController
){
    var keyword by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(CampsiteFilter()) }
    val campsites = searchController.search(keyword, filter)

    CampsiteListContent(
        keyword = keyword,
        onKeywordChange = {keyword = it},
        filter = filter,
        onFilterChange = {filter = it},
        campsites = campsites,
        onCampsiteClick = {campsite ->
            navController.navigate("CampsiteDetailsScreen/${campsite.id}")
        },
        onScanClick = { navController.navigate("QRScanner")},
        onMyCampsClick = {
            navController.navigate(CampsiteListRoutes.MY_CAMPS) { launchSingleTop = true }
        },
        onProfileClick = {
            navController.navigate(CampsiteListRoutes.PROFILE) { launchSingleTop = true }
        }
    )
}

/**
 * The layout of [CampsiteListScreen].
 * UI tests can show it with fake campsites.
 *
 * @param keyword: text in the search bar.
 * @param onKeywordChange called with the new text when the user types in the search bar.
 * @param filter: current location, date range and number of guests.
 * @param onFilterChange: called with the updated filter when the user changes a filter field.
 * @param campsites: campsites to list.
 * @param onCampsiteClick: called with the campsite the user tapped.
 * @param onScanClick: called when the user taps "Scan QR code".
 * @param onMyCampsClick: called when the user taps "My Camps" in the bottom bar.
 * @param onProfileClick: called when the user taps "Profile" in the bottom bar.
 */
@Composable
fun CampsiteListContent(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    filter: CampsiteFilter,
    onFilterChange: (CampsiteFilter) -> Unit,
    campsites: List<Campsite>,
    onCampsiteClick: (Campsite) -> Unit,
    onScanClick: () -> Unit,
    onMyCampsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Scaffold(
        bottomBar = {
            CampsiteListBottomBar(
                onMyCampsClick = onMyCampsClick,
                onProfileClick = onProfileClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = keyword,
                onValueChange = onKeywordChange,
                label = { Text("Search by name or keyword") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = filter.location,
                onValueChange = { onFilterChange(filter.copy(location = it)) },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = filter.startDate,
                    onValueChange = { onFilterChange(filter.copy(startDate = it)) },
                    label = { Text("Start (YYYY-MM-DD)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = filter.endDate,
                    onValueChange = { onFilterChange(filter.copy(endDate = it)) },
                    label = { Text("End (YYYY-MM-DD)") },
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = if (filter.guests == 0) "" else filter.guests.toString(),
                onValueChange = { onFilterChange(filter.copy(guests = it.toIntOrNull() ?: 0)) },
                label = { Text("Number of guests") },
                modifier = Modifier.fillMaxWidth()
            )


            Button(onClick = onScanClick, modifier = Modifier.padding(vertical = 8.dp)) {
                Text("Scan QR code")
            }

            if (campsites.isEmpty()) {
                Text(CampsiteListText.NO_RESULTS)
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(campsites) { campsite ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCampsiteClick(campsite) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(campsite.name, style = MaterialTheme.typography.titleMedium)
                            Text(campsite.location)
                            Text("Up to ${campsite.maxOccupancy} guests")
                            Text(formatCampsiteRating(campsite))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Formats a campsite's rating for display.
 * @return for example `"Rating: 4.5 ★"`, or `"No ratings yet"` if nobody has rated it.
 */
fun formatCampsiteRating(campsite: Campsite): String {
    if (campsite.ratings.isEmpty()) {
        return "No ratings yet"
    }
    val roundedRating = String.format(Locale.US, "%.1f", campsite.avgRating)
    return "Rating: $roundedRating ★"
}

/** Text shown on [CampsiteListScreen]. Tests compare against these. */
object CampsiteListText {
    //Shown when no campsites match the search.
    const val NO_RESULTS = "No campsites match your search."
    // Label of the bottom bar button for this screen (the campsite list).
    const val CAMPSITES = "Campsites"

    // Label of the bottom bar button that opens the owner's campsites.
    const val MY_CAMPS = "My Camps"

    // Label of the bottom bar button that opens the profile
    const val PROFILE = "Profile"
}

/**
 * Bottom navigation bar for [CampsiteListScreen] with three buttons: Campsites, My Camps and Profile.
 * Campsites is bold because it is the current screen, so tapping it does nothing.
 * @param onMyCampsClick: called when the user taps "My Camps".
 * @param onProfileClick: called when the user taps "Profile".
 */
@Composable
fun CampsiteListBottomBar(
    onMyCampsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    BottomAppBar {
        BottomBarButton(
            text = CampsiteListText.CAMPSITES,
            isCurrentScreen = true,
            onClick = { /* Already on this screen */ },
            modifier = Modifier.weight(1f)
        )
        BottomBarButton(
            text = CampsiteListText.MY_CAMPS,
            isCurrentScreen = false,
            onClick = onMyCampsClick,
            modifier = Modifier.weight(1f)
        )
        BottomBarButton(
            text = CampsiteListText.PROFILE,
            isCurrentScreen = false,
            onClick = onProfileClick,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * One text button in the bottom bar.
 * @param text: the button's label.
 * @param isCurrentScreen: true if this button is for the screen the user is on; its text is bold.
 * @param onClick: called when the user taps the button.
 * @param modifier: used to give each button an equal share of the bar's width.
 */
@Composable
fun BottomBarButton(
    text: String,
    isCurrentScreen: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fontWeight = if (isCurrentScreen) FontWeight.Bold else FontWeight.Normal

    TextButton(onClick = onClick, modifier = modifier) {
        Text(text = text, fontWeight = fontWeight)
    }
}

/**
 * Navigation routes the bottom bar opens.
 */
object CampsiteListRoutes {
    const val MY_CAMPS = "OwnerCampsitesScreen"
    const val PROFILE = "ProfileScreen"
}