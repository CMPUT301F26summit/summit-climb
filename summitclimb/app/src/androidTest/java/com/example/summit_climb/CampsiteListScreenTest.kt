package com.example.summit_climb

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for [CampsiteListScreen]
 * The screen is shown with the fake campsites in [TestCampsites] instead of Firestore,
 * using same state handling as [CampsiteListScreen]
 */
@RunWith(AndroidJUnit4::class)
class CampsiteListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun opening_screen_shows_only_search_box_and_scan_button() {
        composeTestRule.setContent { CampsiteListForTest() }

        // verify title, bell, search box and scan button
        composeTestRule
            .onNodeWithText(CampsiteListText.TITLE)
            .assertExists()
        composeTestRule
            .onNodeWithTag(CampsiteListTags.NOTIFICATIONS_BUTTON)
            .assertExists()
        composeTestRule
            .onNodeWithTag(CampsiteListTags.SEARCH_BOX)
            .assertExists()
        composeTestRule
            .onNodeWithTag(CampsiteListTags.SCAN_BUTTON)
            .assertExists()

        // verify filters are hidden
        composeTestRule
            .onNodeWithTag(CampsiteListTags.LOCATION_INPUT)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(CampsiteListTags.SEARCH_BUTTON)
            .assertDoesNotExist()
    }

    @Test
    fun list_shows_summary_info_for_every_campsite() {
        composeTestRule.setContent { CampsiteListForTest() }

        // verify all three campsites appear
        composeTestRule
            .onNodeWithText("Cedar Valley")
            .assertExists()
        composeTestRule
            .onNodeWithText("Lakeside Camp")
            .assertExists()
        composeTestRule
            .onNodeWithText("Summit Camp")
            .assertExists()

        // verify Cedar Valley's location, max occupancy and rating
        composeTestRule
            .onNodeWithText("Banff, AB")
            .assertExists()
        composeTestRule
            .onNodeWithText("Up to 6 guests")
            .assertExists()
        composeTestRule
            .onNodeWithText("Rating: 4.5 ★")
            .assertExists()
    }

    @Test
    fun tapping_search_box_shows_filters_and_search_button() {
        composeTestRule.setContent { CampsiteListForTest() }

        // tap search box
        composeTestRule
            .onNodeWithTag(CampsiteListTags.SEARCH_BOX)
            .performClick()

        // verify filters and search button appear
        composeTestRule
            .onNodeWithTag(CampsiteListTags.LOCATION_INPUT)
            .assertExists()
        composeTestRule
            .onNodeWithTag(CampsiteListTags.START_DATE_INPUT)
            .assertExists()
        composeTestRule
            .onNodeWithTag(CampsiteListTags.END_DATE_INPUT)
            .assertExists()
        composeTestRule
            .onNodeWithTag(CampsiteListTags.GUESTS_INPUT)
            .assertExists()
        composeTestRule
            .onNodeWithTag(CampsiteListTags.SEARCH_BUTTON)
            .assertExists()
    }

    @Test
    fun search_by_keyword_shows_match_and_hides_filters() {
        composeTestRule.setContent { CampsiteListForTest() }

        // tap search box and type a tag
        composeTestRule
            .onNodeWithTag(CampsiteListTags.SEARCH_BOX)
            .performClick()
        composeTestRule
            .onNodeWithTag(CampsiteListTags.SEARCH_BOX)
            .performTextInput("lake")

        // press search
        composeTestRule
            .onNodeWithTag(CampsiteListTags.SEARCH_BUTTON)
            .performClick()

        // verify only Lakeside Camp is listed
        composeTestRule
            .onNodeWithText("Lakeside Camp")
            .assertExists()
        composeTestRule
            .onNodeWithText("Cedar Valley")
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithText("Summit Camp")
            .assertDoesNotExist()

        // verify filters are hidden again, but search box and scan button stay
        composeTestRule
            .onNodeWithTag(CampsiteListTags.LOCATION_INPUT)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(CampsiteListTags.SEARCH_BUTTON)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(CampsiteListTags.SEARCH_BOX)
            .assertExists()
        composeTestRule
            .onNodeWithTag(CampsiteListTags.SCAN_BUTTON)
            .assertExists()
    }

    @Test
    fun typing_without_pressing_search_does_not_change_list() {
        composeTestRule.setContent { CampsiteListForTest() }

        // type a keyword but don't press search
        composeTestRule
            .onNodeWithTag(CampsiteListTags.SEARCH_BOX)
            .performTextInput("lake")

        // verify the list wasn't filtered: Cedar Valley doesn't match "lake" but is still shown
        // (only the first card is checked; the open filters can push later cards off screen)
        composeTestRule
            .onNodeWithText("Cedar Valley")
            .assertExists()
    }

    @Test
    fun keyboard_search_key_runs_search() {
        composeTestRule.setContent { CampsiteListForTest() }

        // type a keyword and press the keyboard's search key
        composeTestRule
            .onNodeWithTag(CampsiteListTags.SEARCH_BOX)
            .performTextInput("tennis")
        composeTestRule
            .onNodeWithTag(CampsiteListTags.SEARCH_BOX)
            .performImeAction()

        // verify only Summit Camp is listed
        composeTestRule
            .onNodeWithText("Summit Camp")
            .assertExists()
        composeTestRule
            .onNodeWithText("Cedar Valley")
            .assertDoesNotExist()
    }

    @Test
    fun filter_by_location_and_guests() {
        composeTestRule.setContent { CampsiteListForTest() }

        // open filters
        composeTestRule
            .onNodeWithTag(CampsiteListTags.SEARCH_BOX)
            .performClick()

        // enter location and number of guests
        composeTestRule
            .onNodeWithTag(CampsiteListTags.LOCATION_INPUT)
            .performTextInput("jasper")
        composeTestRule
            .onNodeWithTag(CampsiteListTags.GUESTS_INPUT)
            .performTextInput("8")

        // press search
        composeTestRule
            .onNodeWithTag(CampsiteListTags.SEARCH_BUTTON)
            .performClick()

        // verify only the Jasper campsite is listed
        composeTestRule
            .onNodeWithText("Lakeside Camp")
            .assertExists()
        composeTestRule
            .onNodeWithText("Cedar Valley")
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithText("Summit Camp")
            .assertDoesNotExist()
    }

    @Test
    fun filter_by_dates_hides_campsite_with_booked_date() {
        composeTestRule.setContent { CampsiteListForTest() }

        // open filters
        composeTestRule
            .onNodeWithTag(CampsiteListTags.SEARCH_BOX)
            .performClick()

        // enter Dec 2–3; Lakeside Camp is booked on Dec 2, Summit Camp isn't open on Dec 3
        composeTestRule
            .onNodeWithTag(CampsiteListTags.START_DATE_INPUT)
            .performTextInput("2026-12-02")
        composeTestRule
            .onNodeWithTag(CampsiteListTags.END_DATE_INPUT)
            .performTextInput("2026-12-03")

        // press search
        composeTestRule
            .onNodeWithTag(CampsiteListTags.SEARCH_BUTTON)
            .performClick()

        // verify only Cedar Valley is free for both nights
        composeTestRule
            .onNodeWithText("Cedar Valley")
            .assertExists()
        composeTestRule
            .onNodeWithText("Lakeside Camp")
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithText("Summit Camp")
            .assertDoesNotExist()
    }

    @Test
    fun search_with_no_matches_shows_no_results_message() {
        composeTestRule.setContent { CampsiteListForTest() }

        // open filters and ask for more guests than any campsite fits
        composeTestRule
            .onNodeWithTag(CampsiteListTags.SEARCH_BOX)
            .performClick()
        composeTestRule
            .onNodeWithTag(CampsiteListTags.GUESTS_INPUT)
            .performTextInput("20")
        composeTestRule
            .onNodeWithTag(CampsiteListTags.SEARCH_BUTTON)
            .performClick()

        // verify the no results message
        composeTestRule
            .onNodeWithText(CampsiteListText.NO_RESULTS)
            .assertExists()
    }

    @Test
    fun scan_and_notification_buttons_report_clicks() {
        var scanClicks = 0
        var notificationClicks = 0
        composeTestRule.setContent {
            CampsiteListForTest(
                onScanClick = { scanClicks++ },
                onNotificationsClick = { notificationClicks++ }
            )
        }

        // tap scan button
        composeTestRule
            .onNodeWithTag(CampsiteListTags.SCAN_BUTTON)
            .performClick()

        // tap notifications bell
        composeTestRule
            .onNodeWithTag(CampsiteListTags.NOTIFICATIONS_BUTTON)
            .performClick()

        // verify each was tapped once
        assertEquals(1, scanClicks)
        assertEquals(1, notificationClicks)
    }

    @Test
    fun navigate_to_campsite_details_and_return_to_list() {
        composeTestRule.setContent { ListAndDetailsForTest() }

        // open Cedar Valley
        composeTestRule
            .onNodeWithTag(CampsiteListTags.campsiteCard(TestCampsites.cedarValley.id))
            .performClick()

        // verify details screen
        composeTestRule
            .onNodeWithText("About this Campsite")
            .assertExists()
        composeTestRule
            .onNodeWithText(TestCampsites.cedarValley.description)
            .assertExists()

        // go back
        composeTestRule
            .onNodeWithTag(CampsiteDetailsTags.BACK_BUTTON)
            .performClick()

        // verify return to  campsite list
        composeTestRule
            .onNodeWithText(CampsiteListText.TITLE)
            .assertExists()
    }
}

/**
 * [CampsiteListContent] with the same state handling as [CampsiteListScreen],
 * but searching a fixed list instead of Firestore.
 *
 * @param campsites: campsites to search.
 * @param onCampsiteClick: called with campsite user tapped.
 * @param onScanClick: called when user taps the scan button.
 * @param onNotificationsClick: called when user taps the bell.
 */
@Composable
private fun CampsiteListForTest(
    campsites: List<Campsite> = TestCampsites.all,
    onCampsiteClick: (Campsite) -> Unit = {},
    onScanClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {}
) {
    var keyword by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(CampsiteFilter()) }
    var searchedKeyword by remember { mutableStateOf("") }
    var searchedFilter by remember { mutableStateOf(CampsiteFilter()) }
    var showFilters by remember { mutableStateOf(false) }

    val shownCampsites = campsites.filter { campsite ->
        campsite.keywordMatch(searchedKeyword) && campsite.filterMatch(searchedFilter)
    }

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
        campsites = shownCampsites,
        onCampsiteClick = onCampsiteClick,
        onScanClick = onScanClick,
        onNotificationsClick = onNotificationsClick
    )
}

/**
 * The campsite list and details screens connected by navigation, like in the app.
 */
@Composable
private fun ListAndDetailsForTest() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.CAMPSITE_LIST) {
        composable(Routes.CAMPSITE_LIST) {
            CampsiteListForTest(
                onCampsiteClick = { campsite ->
                    navController.navigate(Routes.campsiteDetails(campsite.id))
                }
            )
        }
        composable(
            route = Routes.CAMPSITE_DETAILS,
            arguments = listOf(navArgument(Routes.CAMPSITE_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val campsiteId = backStackEntry.arguments?.getString(Routes.CAMPSITE_ID_ARG)
            val campsite = TestCampsites.all.first { campsite -> campsite.id == campsiteId }
            CampsiteDetailsContent(
                campsite = campsite,
                lottery = null,
                hasJoined = false,
                message = "",
                onBackClick = { navController.popBackStack() },
                onJoinClick = {},
                onLeaveClick = {},
                onRateClick = {}
            )
        }
    }
}