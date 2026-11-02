package com.example.summit_climb

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.summit_climb.controller.NotificationController


@Composable
fun AppNavigation(container: AppContainer) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "homescreen") {

        composable("homescreen") { HomeScreen(navController, container) }

        // Entrant screens (uncomment when ready to use)

        composable("entrantCampsiteList") { EntrantCampsiteList(navController) }

        // Notifications screen
        composable("entrantNotifications") {
            val userId = container.currUserId

            if (userId != null) {
                val controller = remember(userId) {
                    NotificationController(
                        repository = container.notificationRepository,
                        userId = userId
                    )
                }
                NotificationsScreen(
                    controller = controller,
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