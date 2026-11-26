package com.example.summit_climb.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.summit_climb.R

/**
 * The bottom navigation bar shown on every screen
 * Five icon-only buttons: Home, My Reservations, My Lotteries, My Campsites, Profile.
 * The button for the current screen is blue; the others are black.
 *
 */

/** Selected icon colour. Not in info_doc yet; add it there if the team keeps it. */
private val selectedBlue = Color(0xFF007AFF)



/**
 * One button in the bottom bar.
 *
 * @property route: screen the button opens
 * @property iconRes: the button's icon.
 * @property label: read aloud by screen readers; UI tests can find the button by it.
 */
enum class BottomBarTab(
    val route: String,
    @DrawableRes val iconRes: Int,
    val label: String
) {
    HOME(Routes.CAMPSITE_LIST, R.drawable.ic_nav_home, "Home"),
    RESERVATIONS(Routes.MY_RESERVATIONS, R.drawable.ic_nav_reservations, "My Reservations"),
    LOTTERIES(Routes.MY_LOTTERIES, R.drawable.ic_nav_lotteries, "My Lotteries"),
    MY_CAMPS(Routes.OWNER_CAMPSITES, R.drawable.ic_nav_my_camps, "My Campsites"),
    PROFILE(Routes.PROFILE, R.drawable.ic_nav_profile, "Profile")
}

/**
 * decides which button to highlight for the screen the user is on.
 *
 * @param route: route of the current screen, or `null` before the first screen opens.
 * @return the tab to highlight, or `null` to highlight nothing.
 */
fun tabForRoute(route: String?): BottomBarTab? {
    if (route == null) {
        return BottomBarTab.HOME
    }

    for (tab in BottomBarTab.entries) {
        if (tab.route == route) {
            return tab
        }
    }

    if (route == Routes.CAMPSITE_DETAILS) {
        return BottomBarTab.HOME
    }
    if (route.startsWith("owner")) {
        return BottomBarTab.MY_CAMPS
    }
    return null
}

/**
 * The bottom bar, connected to the app's navigation.
 * Highlights the current tab and opens a tab's screen when its button is tapped.
 *
 * @param navController: app's navigation controller.
 */
@Composable
fun AppBottomBar(navController: NavController) {
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route
    val currentTab = tabForRoute(currentRoute)

    AppBottomBarContent(
        currentTab = currentTab,
        onTabClick = { tab -> navigateToTab(navController, tab) }
    )
}

/**
 * The layout of [AppBottomBar].
 * UI tests can show it without a real navigation controller.
 *
 * @param currentTab: tab to show in blue, or `null` for none.
 * @param onTabClick: called with the tab the user tapped.
 */
@Composable
fun AppBottomBarContent(
    currentTab: BottomBarTab?,
    onTabClick: (BottomBarTab) -> Unit
) {
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = selectedBlue,
        unselectedIconColor = Color.Black,
        indicatorColor = Color.Transparent   // no grey pill behind the selected icon
    )

    NavigationBar(containerColor = white) {
        for (tab in BottomBarTab.entries) {
            NavigationBarItem(
                selected = tab == currentTab,
                onClick = { onTabClick(tab) },
                icon = {
                    Icon(
                        painter = painterResource(tab.iconRes),
                        contentDescription = tab.label,
                        modifier = Modifier.size(28.dp)
                    )
                },
                colors = itemColors
            )
        }
    }
}

/**
 * Opens a tab's screen the standard way for bottom bars:
 * - goes back to the start screen first, so the back stack doesn't grow with every tap;
 * - doesn't open a second copy if the user taps the tab they're already on;
 * - remembers each tab's scroll position and other state when switching away and back.
 *
 * @param navController: app's navigation controller.
 * @param tab: tab the user tapped.
 */
fun navigateToTab(navController: NavController, tab: BottomBarTab) {
    val startScreenId = navController.graph.findStartDestination().id

    navController.navigate(tab.route) {
        popUpTo(startScreenId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}