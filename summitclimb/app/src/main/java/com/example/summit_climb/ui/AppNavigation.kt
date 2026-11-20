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
import androidx.compose.runtime.DisposableEffect
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
import com.example.summit_climb.ui.entrant.ReservationContent
import com.example.summit_climb.ui.entrant.ReservationUiInfo


//testing (ignore, just to show ui)
import com.example.summit_climb.model.ListingStatus
import com.example.summit_climb.data.SampleData
import androidx.compose.runtime.LaunchedEffect
import com.example.summit_climb.ui.entrant.MyLotteriesText
import com.example.summit_climb.ui.entrant.LotteryUiItem
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.summit_climb.controller.NotificationController
import com.example.summit_climb.controller.OwnerLotteryController
import com.example.summit_climb.ui.entrant.MyLotteriesContent
import com.example.summit_climb.ui.entrant.MyReservationsText
import com.example.summit_climb.model.WaitlistEntry
import com.example.summit_climb.model.WaitlistResult
import com.example.summit_climb.ui.entrant.NotificationsScreen
import com.example.summit_climb.ui.owner.WaitlistScreen
import com.example.summit_climb.ui.owner.OwnerReservationsFromFirestore
import com.example.summit_climb.controller.OwnerReservationController
import com.example.summit_climb.controller.ProfileController
import com.example.summit_climb.model.LotteryStatus
import com.example.summit_climb.ui.owner.Entrant
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.summit_climb.data.FirestoreReservationRepository
import com.example.summit_climb.model.Reservation
import com.example.summit_climb.ui.entrant.MyReservationUiItem
import com.example.summit_climb.ui.entrant.MyReservationsContent
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import com.example.summit_climb.ui.entrant.PoliciesScreen
import com.example.summit_climb.ui.entrant.PolicyDetailsScreen
import androidx.lifecycle.viewmodel.compose.viewModel

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
    ownerCampsiteController: OwnerCampsiteController,
    notificationController: NotificationController,
    reservationController: OwnerReservationController,
    ownerLotteryController: OwnerLotteryController
){
    val navController = rememberNavController()
    val profileController: ProfileController = viewModel()


    Scaffold(
        containerColor = Color(0xFFCFE7F5),
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
        ) {

            composable("homescreen") { HomeScreen(navController) }

            // test
            composable(Routes.MY_RESERVATIONS) {
                var reservations by remember {
                    mutableStateOf(
                        listOf(
                            MyReservationUiItem("sample-reservation-cedar-valley", "Cedar Valley", "Banff, AB", "Dec 12 – Dec 14, 2026", 2, "\$90", MyReservationsText.ACTIVE),
                            MyReservationUiItem("sample-reservation-lakeside-camp", "Lakeside Camp", "Jasper, AB", "Aug 3 – Aug 6, 2026", 3, "\$135", MyReservationsText.COMPLETED),
                            MyReservationUiItem("sample-reservation-summit-camp", "Summit Camp", "Hinton, AB", "Sep 18 – Sep 20, 2026", 2, "\$80", MyReservationsText.CANCELLED)
                        )
                    )
                }
                var message by remember { mutableStateOf("") }

                MyReservationsContent(
                    reservations = reservations,
                    message = message,
                    onMessageShown = { message = "" },
                    onCancel = { id ->
                        val name = reservations.find { it.reservationId == id }?.campsiteName ?: ""
                        reservations = reservations.map {
                            if (it.reservationId == id) it.copy(status = MyReservationsText.CANCELLED) else it
                        }
                        message = MyReservationsText.cancelled(name)
                    }
                )
            }

            // test
            composable(Routes.MY_LOTTERIES) {
                var lotteries by remember {
                    mutableStateOf(
                        listOf(
                            LotteryUiItem(
                                "sample-lottery-cedar-valley",
                                "Cedar Valley",
                                "Banff, AB",
                                MyLotteriesText.PENDING,
                                "Lottery closes in 14 days"
                            ),
                            LotteryUiItem(
                                "sample-lottery-lakeside-camp",
                                "Lakeside Camp",
                                "Jasper, AB",
                                MyLotteriesText.WON,
                                "You've been selected!"
                            ),
                            LotteryUiItem(
                                "sample-lottery-summit-camp",
                                "Summit Camp",
                                "Hinton, AB",
                                MyLotteriesText.LOST,
                                "You weren't selected this time."
                            )
                        )
                    )
                }
                var message by remember { mutableStateOf("") }

                MyLotteriesContent(
                    lotteries = lotteries,
                    message = message,
                    onMessageShown = { message = "" },
                    onLeave = { id ->
                        val name = lotteries.find { it.lotteryId == id }?.campsiteName ?: ""
                        lotteries = lotteries.filter { it.lotteryId != id }
                        message = MyLotteriesText.left(name)
                    },
                    onConfirmReservation = { navController.navigate(Routes.RESERVATION) }
                )
            }

            // test
            composable(Routes.PROFILE) {
                ProfileScreen(
                    navController = navController,
                    controller = profileController
                )
            }

            composable(
                route = Routes.MANAGE_QR_CODE,
                arguments = campsiteIdArgs
            ) { entry ->
                ManageQRCodeScreen(
                    navController = navController,
                    campsiteID = entry.campsiteId()
                )
            }

            // ----- Entrant screens -------  //
            composable(Routes.NOTIFICATIONS) {
                NotificationsScreen(
                    controller = notificationController,
                    navController = navController,
                    notificationsEnabled = profileController.notificationsStatus
                )
            }

            composable(Routes.QR_SCANNER) { ComingSoonScreen("QR Scanner") }
            composable(Routes.POLICIES) { PoliciesScreen(navController) }
            composable("policy_details/{code}") { entry ->
                PolicyDetailsScreen(
                    policyCode = entry.arguments?.getString("code").orEmpty(),
                    navController = navController
                )
            }

            // test
            composable(Routes.CAMPSITE_LIST) {
                CampsiteListScreen(navController, searchController)
            }

            // test
            composable(
                route = Routes.CAMPSITE_DETAILS,
                arguments = campsiteIdArgs
            ) { backStackEntry ->
                CampsiteDetailsScreen(
                    navController = navController,
                    campsiteId = backStackEntry.campsiteId(),
                    currentUserId = currentUserId,
                    searchController = searchController,
                    lotteryController = lotteryController
                )
            }

            // test
            composable(Routes.OWNER_CAMPSITES) {
                val user = searchController
                    .search("", CampsiteFilter())
                    .filter { it.ownerId == currentUserId || it.id.startsWith("sample-") }
                OwnerCampsites(navController = navController, campsites = user)
            }

            // test
            composable(
                route = Routes.OWNER_CAMPSITE_DETAILS,
                arguments = campsiteIdArgs
            ) { backStackEntry ->
                val id = backStackEntry.campsiteId()

                val campsite = searchController.campsiteDetails(id)
                    ?: searchController.search("", CampsiteFilter()).find { it.id == id }

                if (campsite == null) {
                    Text(
                        text = "Loading...",
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    OwnerCampsiteDetails(navController = navController, campsite = campsite)
                }
            }

            // test
            composable(
                route = Routes.OWNER_EDIT_CAMPSITE,
                arguments = campsiteIdArgs
            ) { backStackEntry ->
                val campsite = searchController.campsiteDetails(backStackEntry.campsiteId())
                if (campsite == null) {
                    Text("Loading...", modifier = Modifier.padding(16.dp))
                } else {
                    OwnerEditCampsite(
                        navController = navController,
                        campsite = campsite,
                        saveCampsite = { updated ->
                            Log.d("PriceDebug", "currentUserId='$currentUserId' stored ownerId='${campsite.ownerId}'")
                            ownerCampsiteController.editCampsite(
                                ownerId = campsite.ownerId,
                                editCampsite = updated,
                                onSuccess = { Log.d("EditCampsite", "saved") },
                                onFailure = { error -> Log.e("EditCampsite", error.message ?: "failed save") }
                            )
                        },
                        deleteCampsite = { toDelete ->
                            ownerCampsiteController.deleteCampsite(
                                ownerId = campsite.ownerId,
                                campsiteId = toDelete.id,
                                onSuccess = { Log.d("EditCampsite", "deleted") },
                                onFailure = { error -> Log.e("EditCampsite", error.message ?: "failed delete") }
                            )
                        }
                    )
                }
            }

            // test
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

            // test
            composable(
                route = Routes.OWNER_MANAGE_CAMPSITE,
                arguments = campsiteIdArgs
            ) { backStackEntry ->
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
            composable(
                route = Routes.OWNER_WAITLIST,
                arguments = campsiteIdArgs
            ) { backStackEntry ->
                WaitlistScreen(
                    campsiteId = backStackEntry.campsiteId(),
                    currentUserId = currentUserId,
                    searchController = searchController,
                    ownerLotteryController = ownerLotteryController,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Routes.OWNER_ACTIVE_RESERVATIONS,
                arguments = campsiteIdArgs

            ) { backStackEntry ->

                val id = backStackEntry.campsiteId()

                val campsite = searchController.campsiteDetails(id)
                    ?: searchController.search("", CampsiteFilter())
                        .find { it.id == id }

                if (campsite == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Loading...",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                } else {
                    OwnerReservationsFromFirestore(
                        campsiteId = id,
                        controller = reservationController,
                        onReservationClick = { reservationId ->
                            navController.navigate(Routes.OWNER_RESERVATION_DETAILS + "/$reservationId")
                        },
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }
            // test
            composable(Routes.RESERVATION) {
                ReservationContent(
                    info = ReservationUiInfo(
                        lotteryId = "sample-lottery-lakeside-camp",
                        campsiteName = "Lakeside Camp",
                        location = "Jasper, AB",
                        bookingStart = LocalDate.of(2026, 12, 1),
                        bookingEnd = LocalDate.of(2026, 12, 20),
                        bookedDates = setOf(
                            LocalDate.of(2026, 12, 5),
                            LocalDate.of(2026, 12, 6),
                            LocalDate.of(2026, 12, 12)
                        ),
                        reserved = false
                    ),
                    message = "",
                    onBackClick = { navController.popBackStack() },
                    onConfirm = { _, _ -> navController.popBackStack() },
                    onDecline = { navController.popBackStack() },
                    onGuidelinesClick = { navController.navigate(Routes.POLICIES) }
                )
            }
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