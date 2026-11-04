package com.example.summit_climb

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.summit_climb.ui.owner.OwnerReservationsScreen
import com.example.summit_climb.ui.owner.WaitlistScreen


@Composable
fun AppNavigation(container: AppContainer) {
    val navController = rememberNavController()


    // if the user is still signed in from an earlier session, start watching
    // their notifications right away
    LaunchedEffect(Unit) {
        if (container.currUserId != null) {
            container.notificationController.startListening()
        }
    }

    Box {
        NavHost(navController = navController, startDestination = "homescreen") {

            composable("homescreen") { HomeScreen(navController, container) }

            // Entrant screens (uncomment when ready to use)

            composable("entrantCampsiteList") {
                EntrantCampsiteList(navController, container.notificationController) }

            // Notifications screen
            composable("entrantNotifications") {
                if (container.currUserId != null) {
                    NotificationsScreen(
                        controller = container.notificationController,
                        navController = navController
                    )
                } else {
                    Text("Log in to view your notifications.")
                }
            }

            //composable("entrantCampDetails") { entrantCampDetails(navController) }
            //composable("entrantLotteries") { entrantLotteries(navController) }
            //composable("entrantReserve") { entrantReserve(navController) }
            //composable("entrantConfirmReserve") { entrantConfirmReserve(navController) }
            //composable("guidelines") { guidelines(navController) }
            //composable("entrantNotifs") { entrantNotifs(navController) }
            //composable("entrantProfile") { entrantProfile(navController) }

            // Owner screens (uncomment when ready to use)

            //composable("ownerCampsites") { ownerCampsites(navController) }
            //composable("ownerCampDetails") { ownerCampDetails(navController) }
            composable("ownerManageCamp") {
                // placeholders values until the owner controllers exist
                ManageCampsiteScreen(
                    navController = navController,
                    campsiteName = "Mt.Green Camp",
                    location = "Banff, AB",
                    lotteryStatus = "No Lottery",
                    activeReservations = 3,
                    entrantCount = 12,
                    sentCount = 29
                )
            }
            //composable("ownerEditCamp") { ownerEditCamp(navController) }
            //composable("ownerRegister") { ownerRegister(navController) }
            //composable("ownerListLottery"){ ownerListLottery(navController) }
            composable("ownerWaitlist"){ WaitlistScreen() }
            composable("ownerActiveReserve"){ OwnerReservationsScreen() }
            //composable("ownerPastReserve"){ ownerPastReserve(navController) }
            //composable("ownerCancelledReserve"){ ownerCancelledReserve(navController) }
            ////composable("ownerReserveDetails"){ ownerReserveDetails(navController) }
        }
    }
}