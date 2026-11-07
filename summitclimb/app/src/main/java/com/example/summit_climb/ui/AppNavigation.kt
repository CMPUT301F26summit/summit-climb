package com.example.summit_climb.ui

import android.util.Log
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
import com.example.summit_climb.ui.owner.ManageCampsiteScreen
import com.example.summit_climb.ui.owner.ManageQRCodeScreen
import com.example.summit_climb.controller.OwnerCampsiteController
import com.example.summit_climb.ui.owner.OwnerCampsiteDetails
import com.example.summit_climb.ui.owner.OwnerCampsites
import com.example.summit_climb.ui.owner.OwnerEditCampsite
import com.example.summit_climb.ui.owner.OwnerRegisterCampsite
import com.example.summit_climb.ui.entrant.ProfileScreen
import com.example.summit_climb.controller.CampsiteSearchController
import com.example.summit_climb.controller.EntrantLotteryController
import com.example.summit_climb.model.Campsite
import com.example.summit_climb.model.CampsiteFilter
import com.example.summit_climb.ui.entrant.CampsiteDetailsScreen
import com.example.summit_climb.ui.entrant.CampsiteListScreen
import androidx.navigation.compose.currentBackStackEntryAsState

//testing (ignore, just to show ui)
import com.example.summit_climb.model.ListingStatus
import com.example.summit_climb.data.SampleData

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
 * @param currentUserId: [com.example.summit_climb.model.User.userId] of person using the app.
 */
@Composable
fun AppNavigation(
    searchController: CampsiteSearchController,
    lotteryController: EntrantLotteryController,
    currentUserId: String,
    ownerCampsiteController: OwnerCampsiteController
){
    val navController = rememberNavController()


    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            if (navController.currentBackStackEntryAsState().value?.destination?.route
                != "homescreen") {
                AppBottomBar(navController)
            }
        }
    ){ innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME_SCREEN,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ){

            composable("homescreen") { HomeScreen(navController) }


            composable(Routes.MANAGE_QR_CODE) { ManageQRCodeScreen(navController) }

            composable(Routes.MY_RESERVATIONS) { ComingSoonScreen("My Reservations") }
            composable(Routes.MY_LOTTERIES) { ComingSoonScreen("My Lotteries") }
            composable(Routes.PROFILE) { ProfileScreen(navController) }

            // Entrant screens
            composable(Routes.NOTIFICATIONS) { ComingSoonScreen("Notifications") }
            composable(Routes.QR_SCANNER) { ComingSoonScreen("QR Scanner") }
            composable(Routes.POLICIES) { ComingSoonScreen("Policies & guidelines") }
            composable(Routes.MANAGE_QR_CODE) { ManageQRCodeScreen(navController) }

            composable(Routes.CAMPSITE_LIST) {
                CampsiteListScreen(navController, searchController)
            }
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
            composable(Routes.OWNER_CAMPSITES) {
                val mine = searchController
                    .search("", CampsiteFilter())
                    .filter { it.ownerId == currentUserId || it.id.startsWith("sample-") }
                OwnerCampsites(navController = navController, campsites = mine)
            }

            composable(
                route = Routes.OWNER_CAMPSITE_DETAILS,
                arguments = campsiteIdArgs
            ) { backStackEntry ->
                val id = backStackEntry.campsiteId()
                android.util.Log.e("Details", "id = '$id'")

                val campsite = searchController.campsiteDetails(id)
                    ?: searchController.search("", CampsiteFilter()).find { it.id == id }

                if (campsite == null) {
                    Text(text = "Loading...", modifier = Modifier.padding(16.dp))
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

            composable(Routes.OWNER_REGISTER_CAMPSITE) {
                OwnerRegisterCampsite(
                    navController = navController,
                    campsite = Campsite(),
                    registerCampsite = { newCampsite ->
                        ownerCampsiteController.registerCampsite(
                            ownerId = currentUserId,
                            campsite = newCampsite.copy(
                                latitude = 1.0,
                                longitude = 1.0
                            ), // temp placeholder
                            imageUri = null,
                            onSuccess = { Log.e("Register", "succeeded") },
                            onFailure = { error -> Log.e("Register", error.message ?: "failed") }
                        )
                    }
                )
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