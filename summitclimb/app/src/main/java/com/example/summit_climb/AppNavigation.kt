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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

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
) {
    val navController = rememberNavController()

    Scaffold(
        containerColor = Color.White,
        bottomBar = { AppBottomBar(navController) }
        ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.CAMPSITE_LIST,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding) //stops screens from adding the space twice.
        ) {

            composable("homescreen") { HomeScreen(navController) }
            // Bottom bar tabs

            composable(Routes.CAMPSITE_LIST) {
                CampsiteListScreen(navController, searchController)
            }
            // Replace with real screens when they're ready
            composable(Routes.MY_RESERVATIONS) { ComingSoonScreen("My Reservations") }
            composable(Routes.MY_LOTTERIES) { ComingSoonScreen("My Lotteries") }
            composable(Routes.OWNER_CAMPSITES) { ComingSoonScreen("My Campsites") }
            composable(Routes.PROFILE) { ComingSoonScreen("Profile") }
            composable(Routes.NOTIFICATIONS) { ComingSoonScreen("Notifications") }

            // Entrant screens
            composable(
                route = Routes.CAMPSITE_DETAILS,
                arguments = listOf(navArgument(Routes.CAMPSITE_ID_ARG) { type = NavType.StringType })
            ) { backStackEntry ->
                val campsiteId = backStackEntry.arguments?.getString(Routes.CAMPSITE_ID_ARG) ?: ""
                CampsiteDetailsScreen(
                    navController = navController,
                    campsiteId = campsiteId,
                    currentUserId = currentUserId,
                    searchController = searchController,
                    lotteryController = lotteryController
                )
            }

            // Entrant screens, uncomment when ready to use
            //composable("entrantReserve") { entrantReserve(navController) }
            //composable("entrantConfirmReserve") { entrantConfirmReserve(navController) }
            //composable("guidelines") { guidelines(navController) }
            //composable("entrantNotifs") { entrantNotifs(navController) }

            // Owner screens,uncomment when ready to use
            //composable("ownerCampDetails") { ownerCampDetails(navController) }
            //composable("ownerManageCamp") { ownerManageCamp(navController) }
            //composable("ownerEditCamp") { ownerEditCamp(navController) }
            //composable("ownerRegister") { ownerRegister(navController) }
            //composable("ownerListLottery"){ ownerListLottery(navController) }
            //composable("ownerWaitlist"){ ownerWaitlist(navController) }
            //composable("ownerActiveReserve"){ ownerActiveReserve(navController) }
            //composable("ownerPastReserve"){ ownerPastReserve(navController) }
            //composable("ownerCancelledReserve"){ ownerCancelledReserve(navController) }
            //composable("ownerReserveDetails"){ ownerReserveDetails(navController) }
        }
    }
}

/**
 * Placeholder for a bottom bar tab whose screen isn't built yet.
 * @param title: name of screen that will go here.
 */
@Composable
private fun ComingSoonScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$title — coming soon", style = MaterialTheme.typography.titleMedium)
    }
}