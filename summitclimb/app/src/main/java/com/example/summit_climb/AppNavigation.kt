package com.example.summit_climb

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

val campsiteIdArgs = listOf(
    navArgument(Routes.CAMPSITE_ID_ARG) { type = NavType.StringType }
)

fun NavBackStackEntry.campsiteId(): String {
    return arguments?.getString(Routes.CAMPSITE_ID_ARG).orEmpty()
}

/**
 * All app's screens and how to get between them.
 * Every screen shows [AppBottomBar] at the bottom
 * The app opens on the campsite list.
 *
 * @param searchController: passed to campsite list and details screens.
 * @param lotteryController: passed to campsite details screen.
 * @param currentUserId: [User.userId] of person using the app.
 */
@Composable
fun AppNavigation(
    searchController: CampsiteSearchController,
    lotteryController: EntrantLotteryController,
    currentUserId: String
){
    val navController = rememberNavController()

    Scaffold(
        containerColor = Color.White,
        bottomBar = { AppBottomBar(navController) }
    ){ innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.CAMPSITE_LIST,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding) //stops screens from adding the space twice.
        ){

            composable("homescreen") { HomeScreen(navController) }

            // Bottom bar tabs
            composable(Routes.CAMPSITE_LIST) {
                CampsiteListScreen(navController, searchController)
            }
            // Replace with real screens when they're ready
            composable(Routes.MY_RESERVATIONS) { ComingSoonScreen("My Reservations") }
            composable(Routes.MY_LOTTERIES) { ComingSoonScreen("My Lotteries") }
            composable(Routes.OWNER_CAMPSITES) { ComingSoonScreen("My Campsites") }
            composable(Routes.PROFILE) { ProfileScreen(navController) }

            // Entrant screens
            composable(Routes.NOTIFICATIONS) { ComingSoonScreen("Notifications") }
            composable(Routes.QR_SCANNER) { ComingSoonScreen("QR Scanner") }
            composable(Routes.POLICIES) { ComingSoonScreen("Policies & guidelines") }

            composable(
                route = Routes.CAMPSITE_DETAILS,
                arguments = campsiteIdArgs
            ){ backStackEntry ->
                CampsiteDetailsScreen(
                    navController = navController,
                    campsiteId = backStackEntry.campsiteId(),
                    currentUserId = currentUserId,
                    searchController = searchController,
                    lotteryController = lotteryController
                )
            }

            // Owner screens
            composable(
                route = Routes.OWNER_CAMPSITE_DETAILS,
                arguments = campsiteIdArgs
            ){ backStackEntry ->
                val campsite = searchController.campsiteDetails(backStackEntry.campsiteId())
                if (campsite == null) {
                    Text("Loading...", modifier = Modifier.padding(16.dp))
                } else {
                    OwnerCampsiteDetails(navController = navController, campsite = campsite)
                }
            }

            composable(
                route = Routes.OWNER_EDIT_CAMPSITE,
                arguments = campsiteIdArgs
            ){ backStackEntry ->
                val campsite = searchController.campsiteDetails(backStackEntry.campsiteId())
                if (campsite == null) {
                    Text("Loading...", modifier = Modifier.padding(16.dp))
                } else {
                    OwnerEditCampsite(navController = navController, campsite = campsite)
                }
            }

            // New campsite: no ID, so it starts from a blank Campsite()
            composable(Routes.OWNER_REGISTER_CAMPSITE) {
                OwnerRegisterCampsite(navController = navController, campsite = Campsite())
            }

            composable(
                route = Routes.OWNER_MANAGE_CAMPSITE,
                arguments = campsiteIdArgs
            ){ backStackEntry ->
                val campsite = searchController.campsiteDetails(backStackEntry.campsiteId())
                if (campsite == null) {
                    Text("Loading...", modifier = Modifier.padding(16.dp))
                } else {
                    ManageCampsiteScreen(
                        navController = navController,
                        campsiteId = campsite.id,
                        campsiteName = campsite.name,
                        location = campsite.location,
                        // TODO: replace these placeholders with real lottery/reservation/notification data
                        lotteryStatus = "No Lottery",
                        activeReservations = 0,
                        entrantCount = 0,
                        sentCount = 0
                    )
                }
            }

            composable(
                route = Routes.OWNER_LIST_LOTTERY,
                arguments = campsiteIdArgs
            ) { ComingSoonScreen("List in Lottery") }

            composable(Routes.MANAGE_QR_CODE) { ManageQRCodeScreen(navController) }

            // Owner screens, uncomment when ready to use. Each route constant already exists in Routes.
            //composable(Routes.OWNER_WAITLIST, arguments = campsiteIdArgs) { ... }
            //composable(Routes.OWNER_ACTIVE_RESERVATIONS) { ... }
            //composable(Routes.OWNER_SENT_NOTIFICATIONS) { ... }
        }
    }
}

/**
 * Placeholder for a screen that isn't built yet.
 * @param title: name of screen that will go here.
 */
@Composable
private fun ComingSoonScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$title — coming soon", style = MaterialTheme.typography.titleMedium)
    }
}