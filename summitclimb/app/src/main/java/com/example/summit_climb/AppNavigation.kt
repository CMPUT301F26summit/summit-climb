package com.example.summit_climb

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.summit_climb.controller.NotificationController
import com.example.summit_climb.model.Notification


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
            //composable("ownerManageCamp") { ownerManageCamp(navController) }
            //composable("ownerEditCamp") { ownerEditCamp(navController) }
            //composable("ownerRegister") { ownerRegister(navController) }
            //composable("ownerListLottery"){ ownerListLottery(navController) }
            //composable("ownerWaitlist"){ ownerWaitlist(navController) }
            //composable("ownerActiveReserve"){ ownerActiveReserve(navController) }
            //composable("ownerPastReserve"){ ownerPastReserve(navController) }
            //composable("ownerCancelledReserve"){ ownerCancelledReserve(navController) }
            ////composable("ownerReserveDetails"){ ownerReserveDetails(navController) }
        }
    }
}