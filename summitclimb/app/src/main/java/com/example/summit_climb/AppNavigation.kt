package com.example.summit_climb

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.summit_climb.model.LotteryStatus
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import com.example.summit_climb.controller.OwnerReservationController
import com.example.summit_climb.ui.owner.OwnerReservationsFromFirestore



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
                EntrantCampsiteList(navController, container.notificationController)
            }

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

                // Get the currently signed-in owner's ID
                val ownerId = container.currUserId

                // Find the owner's campsite
                val campsite = if (ownerId != null) {
                    container.campsiteRepository.campsite.firstOrNull {
                        it.ownerId == ownerId
                    }
                } else {
                    null
                }

                // Find the lottery associated with the campsite
                val lottery = campsite?.let { camp ->

                    camp.activeLotteryId?.let { lotteryId ->
                        container.lotteryRepository.get(lotteryId)
                    } ?: container.lotteryRepository.forCampsite(camp.id).firstOrNull()

                }

                val activeReservations = remember(campsite?.id) {
                    mutableStateOf(0)
                }

                LaunchedEffect(campsite?.id) {
                    if (campsite != null) {
                        container.reservationRepository.getReservationsByCampsite(
                            campsiteId = campsite.id,
                            onSuccess = { reservations ->
                                activeReservations.value = reservations.count {
                                    it.status == "ACTIVE"
                                }
                            },
                            onFailure = { exception ->
                                android.util.Log.e(
                                    "ManageCampsite",
                                    "Could not load reservations",
                                    exception
                                )
                            }
                        )
                    }
                }

                // Display the campsite information
                if (campsite != null) {

                    val status = when (lottery?.status) {
                        LotteryStatus.OPEN -> "Open"
                        LotteryStatus.DRAWN -> "Drawn"
                        LotteryStatus.CLOSED -> "Closed"
                        null -> "No Lottery"
                    }

                    ManageCampsiteScreen(
                        navController = navController,
                        campsiteId = campsite.id,
                        campsiteName = campsite.name,
                        location = campsite.location,
                        lotteryStatus = status,
                        activeReservations = activeReservations.value,
                        entrantCount = lottery?.entrantCount ?: 0,
                        sentCount = container.notificationRepository
                            .sentBy(campsite.ownerId)
                            .count { it.campsiteId == campsite.id }
                    )

                } else {
                    Text("No campsite found for this account.")
                }
            }
            //composable("ownerEditCamp") { ownerEditCamp(navController) }
            //composable("ownerRegister") { ownerRegister(navController) }
            //composable("ownerListLottery"){ ownerListLottery(navController) }
            composable("ownerWaitlist/{campsiteId}") { backStackEntry ->
                val campsiteId = backStackEntry.arguments
                    ?.getString("campsiteId")

                val campsite = campsiteId?.let {
                    container.campsiteRepository.getCampsite(it)
                }

                val lottery = campsiteId?.let {
                    container.lotteryRepository.forCampsite(it).firstOrNull()
                }
                if (campsite != null && lottery != null) {

                    val status = when (lottery.status) {
                        LotteryStatus.OPEN -> "Lottery Open"
                        LotteryStatus.DRAWN -> "Winners Drawn"
                        LotteryStatus.CLOSED -> "Lottery Closed"
                    }

                    val drawDate = if (lottery.registrationEnd > 0L) {
                        java.text.SimpleDateFormat(
                            "MMM dd",
                            java.util.Locale.getDefault()
                        ).format(java.util.Date(lottery.registrationEnd))
                    } else {
                        "Not scheduled"
                    }

                    WaitlistScreen(
                        campsiteName = campsite.name,
                        entrants = emptyList(),    // to be connected later
                        lotteryStatus = status,
                        entrantCount = lottery.entrantCount,
                        winnersDrawnDate = drawDate,
                        maxEntrants = lottery.waitlistCap,
                        onBack = { navController.popBackStack() },
                        onNotifyEntrants = {
                            // to be connected later
                        }
                    )
                } else {
                    Text("Loading campsite lottery information....")
                }
            }
            composable("ownerActiveReserve") {
                val campsite = container.currUserId?.let { ownerId ->
                    container.campsiteRepository.campsite.firstOrNull {
                        it.ownerId == ownerId
                    }
                }

                if (campsite != null) {
                    val controller = remember {
                        OwnerReservationController(container.reservationRe)
                    }

                    OwnerReservationsFromFirestore(
                        campsiteId = campsite.id,
                        controller = controller,
                        onReservationClick = { reservationId ->
                            // Details navigation will be connected separately.
                        }
                    )
                } else {
                    Text("No campsite found for this account.")
                }
            }

            //composable("ownerPastReserve"){ ownerPastReserve(navController) }
            //composable("ownerCancelledReserve"){ ownerCancelledReserve(navController) }
            ////composable("ownerReserveDetails"){ ownerReserveDetails(navController) }
        }
    }
}