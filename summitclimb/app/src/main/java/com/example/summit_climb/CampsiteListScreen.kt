package com.example.summit_climb

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.Locale

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search

/**
 * The entrant's campsite list screen (Home).
 * Tapping the search box shows the filters (location, start, end, guests) and a Search button.
 * Pressing Search runs the search and hides the filters again.
 * The list only changes when the user presses Search, not while they type.
 * Tapping a campsite opens its details
 *
 * @param navController: app's navigation controller, used to open other screens.
 * @param searchController: finds the campsites to show.
 */
@Composable
fun CampsiteListScreen(
    navController: NavController,
    searchController: CampsiteSearchController
){
    // What the user is typing right now (not searched yet)
    var keyword by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(CampsiteFilter()) }

    // What was searched the last time the user pressed Search
    var searchedKeyword by remember { mutableStateOf("") }
    var searchedFilter by remember { mutableStateOf(CampsiteFilter()) }

    // true while the filter fields and Search button are showing
    var showFilters by remember { mutableStateOf(false) }

    val campsites = searchController.search(searchedKeyword, searchedFilter)

    CampsiteListContent(
        keyword = keyword,
        onKeywordChange = { keyword = it },
        filter = filter,
        onFilterChange = { filter = it },
        showFilters = showFilters,
        onSearchBoxTapped = { showFilters = true },
        onSearchClick = {
            searchedKeyword = keyword
            searchedFilter = filter
            showFilters = false
        },
        campsites = campsites,
        onCampsiteClick = { campsite ->
            navController.navigate(Routes.campsiteDetails(campsite.id))
        },
        onScanClick = { navController.navigate("QRScanner") },
        onNotificationsClick = {
            navController.navigate(Routes.NOTIFICATIONS) { launchSingleTop = true }
        }
    )
}


/**
 * The layout of [CampsiteListScreen].
 *
 * @param keyword: text in search box.
 * @param onKeywordChange: called with new text when the user types in search box.
 * @param filter: location, date range and number of guests user has typed.
 * @param onFilterChange: called with updated filter when user changes a filter field.
 * @param showFilters: `true` to show filter fields and Search button.
 * @param onSearchBoxTapped: called when user taps search box.
 * @param onSearchClick: called when user presses Search.
 * @param campsites: campsites to list.
 * @param onCampsiteClick: called with campsite the user tapped.
 * @param onScanClick: called when user taps the QR scan button.
 * @param onNotificationsClick: called when the user taps bell.
 */
@Composable
fun CampsiteListContent(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    filter: CampsiteFilter,
    onFilterChange: (CampsiteFilter) -> Unit,
    showFilters: Boolean,
    onSearchBoxTapped: () -> Unit,
    onSearchClick: () -> Unit,
    campsites: List<Campsite>,
    onCampsiteClick: (Campsite) -> Unit,
    onScanClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    // Runs a search.
    val runSearch = {
        focusManager.clearFocus()
        onSearchClick()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CampsiteListHeader(onNotificationsClick = onNotificationsClick)

        SearchRow(
            keyword = keyword,
            onKeywordChange = onKeywordChange,
            onSearchBoxTapped = onSearchBoxTapped,
            onKeyboardSearch = runSearch,
            onScanClick = onScanClick
        )

        if (showFilters) {
            FilterFields(
                filter = filter,
                onFilterChange = onFilterChange,
                onSearchClick = runSearch
            )
        }

        if (campsites.isEmpty()) {
            Text(CampsiteListText.NO_RESULTS, color = charcoal)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(campsites) { campsite ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(CampsiteListTags.campsiteCard(campsite.id))
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

/**
 * Top row has title  and the notifications bell
 * @param onNotificationsClick: called when user taps bell.
 */
@Composable
fun CampsiteListHeader(onNotificationsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = CampsiteListText.TITLE,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onNotificationsClick,
            modifier = Modifier.testTag(CampsiteListTags.NOTIFICATIONS_BUTTON)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = CampsiteListText.NOTIFICATIONS,
                tint = Color.Black,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/**
 * The search box with QR scan button
 *
 * @param keyword: text in search box.
 * @param onKeywordChange: called with new text when user types.
 * @param onSearchBoxTapped: called when search box gets focus (user tapped it).
 * @param onKeyboardSearch: called when user presses keyboard's search key.
 * @param onScanClick: called when user taps QR scan button.
 */
@Composable
fun SearchRow(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    onSearchBoxTapped: () -> Unit,
    onKeyboardSearch: () -> Unit,
    onScanClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = keyword,
            onValueChange = onKeywordChange,
            placeholder = { Text(CampsiteListText.SEARCH_HINT, color = charcoal) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = charcoal
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onKeyboardSearch() }),
            modifier = Modifier
                .weight(1f)
                .testTag(CampsiteListTags.SEARCH_BOX)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        onSearchBoxTapped()
                    }
                }
        )
        IconButton(
            onClick = onScanClick,
            modifier = Modifier.testTag(CampsiteListTags.SCAN_BUTTON)
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = CampsiteListText.SCAN_QR,
                tint = Color.Black,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/**
 * The filter fields and Search button, shown only while user is searching.
 *
 * @param filter: location, date range and number of guests user has typed.
 * @param onFilterChange: called with updated filter when user changes a field.
 * @param onSearchClick: called when user presses Search.
 */
@Composable
fun FilterFields(
    filter: CampsiteFilter,
    onFilterChange: (CampsiteFilter) -> Unit,
    onSearchClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = filter.location,
            onValueChange = { onFilterChange(filter.copy(location = it)) },
            label = { Text("Location") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(CampsiteListTags.LOCATION_INPUT)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = filter.startDate,
                onValueChange = { onFilterChange(filter.copy(startDate = it)) },
                label = { Text("Start (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag(CampsiteListTags.START_DATE_INPUT)
            )
            OutlinedTextField(
                value = filter.endDate,
                onValueChange = { onFilterChange(filter.copy(endDate = it)) },
                label = { Text("End (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag(CampsiteListTags.END_DATE_INPUT)
            )
        }
        OutlinedTextField(
            value = if (filter.guests == 0) "" else filter.guests.toString(),
            onValueChange = { onFilterChange(filter.copy(guests = it.toIntOrNull() ?: 0)) },
            label = { Text("Number of guests") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(CampsiteListTags.GUESTS_INPUT)
        )
        Button(
            onClick = onSearchClick,
            colors = ButtonDefaults.buttonColors(containerColor = lightBlue, contentColor = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(CampsiteListTags.SEARCH_BUTTON)
        ) {
            Text(CampsiteListText.SEARCH_BUTTON)
        }
    }
}

/**
 * Formats a campsite's rating for display.
 * @return for example "Rating: 4.5 ★" or "No ratings yet" if nobody has rated it.
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
    // Screen title at top.
    const val TITLE = "Find Your Next Campsite"

    // Gray hint inside empty search box.
    const val SEARCH_HINT = "Search by name or keyword"

    // Label of button that runs the search.
    const val SEARCH_BUTTON = "Search"

    // Read by screen readers
    const val NOTIFICATIONS = "Notifications"
    const val SCAN_QR = "Scan QR code"

    // Shown when no campsites match search.
    const val NO_RESULTS = "No campsites match your search."
}

/** Test tags on [CampsiteListScreen]. for UI tests */
object CampsiteListTags {
    const val SEARCH_BOX = "searchBox"
    const val SCAN_BUTTON = "scanButton"
    const val NOTIFICATIONS_BUTTON = "notificationsButton"
    const val LOCATION_INPUT = "locationInput"
    const val START_DATE_INPUT = "startDateInput"
    const val END_DATE_INPUT = "endDateInput"
    const val GUESTS_INPUT = "guestsInput"
    const val SEARCH_BUTTON = "searchButton"

    /**
     * Tag of one campsite's card in list.
     * @return for example `"campsiteCard_sample-cedar-valley"`.
     */
    fun campsiteCard(campsiteId: String): String {
        return "campsiteCard_$campsiteId"
    }
}