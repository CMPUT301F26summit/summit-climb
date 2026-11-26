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
import com.example.summit_climb.ui.owner.SentNotificationsScreen
import com.example.summit_climb.controller.ReservationController


//testing (ignore, just to show ui)
import com.example.summit_climb.model.ListingStatus
import com.example.summit_climb.data.SampleData
import androidx.compose.runtime.LaunchedEffect
import com.example.summit_climb.ui.entrant.MyLotteriesText
import com.example.summit_climb.ui.entrant.LotteryUiItem
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.summit_climb.controller.NotificationController
import com.example.summit_climb.controller.OwnerLotteryController
import com.example.summit_climb.controller.OwnerNotificationController
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
import com.example.summit_climb.model.Notification
import com.example.summit_climb.model.NotificationType
import com.example.summit_climb.ui.owner.OwnerReservationDetailsScreen
import com.example.summit_climb.controller.ConversationController

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
    ownerLotteryController: OwnerLotteryController,
    entrantReservationController: ReservationController,
    ownerNotificationController: OwnerNotificationController,
    onOpenAdmin: () -> Unit = {}
){
    val navController = rememberNavController()
    val profileController: ProfileController = viewModel()
    val conversationController = remember { ConversationController() }

    DisposableEffect(
        profileController.profileLoaded,
        profileController.notificationsStatus
    ) {
        if (profileController.profileLoaded && profileController.notificationsStatus) {
            notificationController.startListening()
        } else {
            notificationController.stopListening()
        }

        onDispose {
            notificationController.stopListening()
        }
    }

    Scaffold(
        containerColor = Color(0xFFCFE7F5),
        bottomBar = {
            if (navController.currentBackStackEntryAsState().value?.destination?.route
                != Routes.HOME_SCREEN) {
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

            composable(Routes.HOME_SCREEN) { HomeScreen(navController, onAdminClick = onOpenAdmin) }

            // test
            composable(Routes.MY_RESERVATIONS) {
                var reservations by remember(currentUserId) {
                    mutableStateOf<List<MyReservationUiItem>>(emptyList())
                }

                var message by remember { mutableStateOf("") }

                LaunchedEffect(currentUserId) {
                    entrantReservationController.getReservationsByEntrant(
                        entrantId = currentUserId,
                        onSuccess = { results ->
                            reservations = results.map { reservation ->
                                val campsite = searchController.campsiteDetails(
                                    reservation.campsiteId
                                )

                                val nights = runCatching {
                                    ChronoUnit.DAYS.between(
                                        LocalDate.parse(reservation.startDate),
                                        LocalDate.parse(reservation.endDate)
                                    ).toInt()
                                }.getOrDefault(0)

                                MyReservationUiItem(
                                    reservationId = reservation.id,
                                    campsiteName = campsite?.name ?: "Unknown campsite",
                                    location = campsite?.location ?: "Unknown location",
                                    dates = "${reservation.startDate} – ${reservation.endDate}",
                                    nights = nights,
                                    total = reservation.totalPrice?.let {
                                        String.format(Locale.US, "$%.2f", it)
                                    } ?: "Unavailable",
                                    status = if (
                                        reservation.status == "ACTIVE" &&
                                        LocalDate.parse(reservation.endDate).isBefore(LocalDate.now())
                                    ) {
                                        MyReservationsText.COMPLETED
                                    } else {
                                        reservation.status
                                    },
                                    campsiteId = reservation.campsiteId
                                )
                            }
                        },
                        onFailure = { error ->
                            message = error.message ?: "Failed to load reservations"
                        }
                    )
                }

                MyReservationsContent(
                    reservations = reservations,
                    message = message,
                    onMessageShown = { message = "" },
                    onCancel = { id ->
                        val selected = reservations.find {
                            it.reservationId == id
                        }

                        if (selected != null) {
                            entrantReservationController.cancelReservation(
                                reservationId = id,
                                entrantId = currentUserId,
                                campsiteId = selected.campsiteId,
                                onSuccess = {
                                    reservations = reservations.map {
                                        if (it.reservationId == id) {
                                            it.copy(status = MyReservationsText.CANCELLED)
                                        } else {
                                            it
                                        }
                                    }
                                    message = "Your reservation at ${selected.campsiteName} has been cancelled."
                                },
                                onFailure = { error ->
                                    message = error.message ?: "Failed to cancel reservation"
                                }
                            )
                        }
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
                    notificationsEnabled = profileController.notificationsStatus,
                    onEnableNotifications = {
                        profileController.setNotifications(true)
                    }
                )
            }

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
                var myCampsites by remember { mutableStateOf<List<Campsite>>(emptyList()) }
                val user = searchController
                    .search("", CampsiteFilter())
                    .filter { it.ownerId == currentUserId || it.id.startsWith("sample-") }
                LaunchedEffect(currentUserId) {
                    ownerCampsiteController.listCampsites(
                        currentUserId,
                        onSuccess = { myCampsites = it },
                        onFailure = { error -> Log.e("OwnerCampsites", error.message ?: "failed") }
                    )
                }
                OwnerCampsites(navController = navController, campsites = user + myCampsites)
            }

            // test
            composable(
                route = Routes.OWNER_CAMPSITE_DETAILS,
                arguments = campsiteIdArgs
            ) { backStackEntry ->
                val id = backStackEntry.campsiteId()

                val campsite = searchController.campsiteDetails(id)
                    ?: searchController.search("", CampsiteFilter())
                        .find { it.id == id }

                var reservationDates by remember(id) {
                    mutableStateOf<List<String>>(emptyList())
                }

                LaunchedEffect(id) {
                    entrantReservationController.getBookedDates(
                        campsiteId = id,
                        onSuccess = { ranges ->
                            val dates = mutableSetOf<String>()

                            ranges.forEach { (start, end) ->
                                var date = LocalDate.parse(start)
                                val checkout = LocalDate.parse(end)

                                while (date.isBefore(checkout)) {
                                    dates.add(date.toString())
                                    date = date.plusDays(1)
                                }
                            }

                            reservationDates = dates.toList()
                        },
                        onFailure = { error ->
                            Log.e(
                                "OwnerCalendar",
                                "Failed to load booked dates",
                                error
                            )
                        }
                    )
                }

                if (campsite == null) {
                    Text(
                        text = "Loading...",
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    OwnerCampsiteDetails(
                        navController = navController,
                        campsite = campsite.copy(
                            bookedDates = (
                                    campsite.bookedDates + reservationDates
                                    ).distinct()
                        )
                    )
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
                            Log.d(
                                "PriceDebug",
                                "currentUserId='$currentUserId' stored ownerId='${campsite.ownerId}'"
                            )
                            ownerCampsiteController.editCampsite(
                                ownerId = campsite.ownerId,
                                editCampsite = updated,
                                onSuccess = { Log.d("EditCampsite", "saved") },
                                onFailure = { error ->
                                    Log.e(
                                        "EditCampsite",
                                        error.message ?: "failed save"
                                    )
                                }
                            )
                        },
                        deleteCampsite = { toDelete ->
                            ownerCampsiteController.deleteCampsite(
                                ownerId = currentUserId,
                                campsiteId = toDelete.id,
                                onSuccess = { Log.d("EditCampsite", "deleted") },
                                onFailure = { error ->
                                    Log.e(
                                        "EditCampsite",
                                        error.message ?: "failed delete"
                                    )
                                }
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
                    val lottery = ownerLotteryController.lotteryForCampsite(
                        ownerId = currentUserId,
                        campsiteId = campsite.id
                    )

                    val lotteryStatus = when (lottery?.status) {
                        LotteryStatus.OPEN -> ownerLotteryController.statusLabel(lottery)
                        LotteryStatus.DRAWN -> "Winners Drawn"
                        LotteryStatus.CLOSED -> "Closed"
                        null -> "No Lottery"
                    }

                    val entrantCount = lottery?.entrantCount ?: 0

                    var activeReservations by remember(campsite.id) {
                        mutableIntStateOf(0)
                    }
                    LaunchedEffect(campsite.id) {
                        reservationController.getReservationsByCampsite(
                            campsiteId = campsite.id,
                            onSuccess = { reservations ->
                                activeReservations = reservations.count {
                                    it.status == "ACTIVE" &&
                                            !LocalDate.parse(it.endDate)
                                                .isBefore(LocalDate.now())
                                }
                            },
                            onFailure = { error ->
                                Log.e(
                                    "AppNavigation",
                                    "Failed to load campsite reservations",
                                    error
                                )
                            }
                        )
                    }
                    ManageCampsiteScreen(
                        navController = navController,
                        campsiteId = campsite.id,
                        campsiteName = campsite.name,
                        location = campsite.location,
                        lotteryStatus = lotteryStatus,
                        activeReservations = activeReservations,
                        entrantCount = entrantCount
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
                    notificationController = notificationController,
                    onBack = { navController.popBackStack()
                    }
                )
            }

            composable(
                route = Routes.OWNER_SENT_NOTIFICATIONS,
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
                        Text("Loading...")
                    }
                } else {
                    SentNotificationsScreen(
                        campsiteId = id,
                        campsiteName = campsite.name,
                        currentUserId = currentUserId,
                        notifications = notificationController.notifications,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
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
                        campsiteName = searchController.campsiteDetails(id)?.name ?: "your campsite",
                        controller = reservationController,
                        onReservationClick = { reservationId ->
                            navController.navigate(
                                Routes.OWNER_RESERVATION_DETAILS + "/$id/$reservationId"
                            )
                        },
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }
            composable(
                route = Routes.OWNER_RESERVATION_DETAILS +
                        "/{campsiteId}/{reservationId}",
                arguments = listOf(
                    navArgument("campsiteId") {
                        type = NavType.StringType
                    },
                    navArgument("reservationId") {
                        type = NavType.StringType
                    }
                )
            ) { entry ->

                val campsiteId = entry.arguments
                    ?.getString("campsiteId").orEmpty()

                val reservationId = entry.arguments
                    ?.getString("reservationId").orEmpty()

                var reservation by remember {
                    mutableStateOf<Reservation?>(null)
                }

                var error by remember {
                    mutableStateOf<String?>(null)
                }

                LaunchedEffect(campsiteId, reservationId) {
                    reservationController.getReservationsByCampsite(
                        campsiteId = campsiteId,
                        onSuccess = { results ->
                            reservation = results.find {
                                it.id == reservationId
                            }

                            if (reservation == null) {
                                error = "Reservation not found"
                            }
                        },
                        onFailure = {
                            error = it.message ?: "Failed to load reservation"
                        }
                    )
                }

                val selected = reservation

                if (error != null) {
                    Text(error ?: "")
                } else if (selected == null) {
                    Text("Loading reservation...")
                } else {
                    val campsite = searchController.campsiteDetails(campsiteId)

                    val nights = ChronoUnit.DAYS.between(
                        LocalDate.parse(selected.startDate),
                        LocalDate.parse(selected.endDate)
                    ).toInt()

                    OwnerReservationDetailsScreen(
                        entrantName = selected.entrantName,
                        dates = "${selected.startDate} - ${selected.endDate}",
                        nights = nights,
                        guests = selected.guests,
                        totalPrice = selected.totalPrice?.let {
                            String.format(Locale.US, "$%.2f", it)
                        } ?: "Unavailable",
                        status = selected.status,
                        rulesAccepted = selected.rulesAccepted,
                        onBackClick = {navController.popBackStack()},
                        onConversationClick = { navController.navigate(
                                Routes.conversation(selected.campsiteId, selected.entrantId)
                            )
                        },

                        onMessageClick = { message ->
                            ownerNotificationController.messageEntrant(
                                ownerId = currentUserId,
                                entrantId = selected.entrantId,
                                campsiteId = selected.campsiteId,
                                message = message
                            )
                        },

                        onRejectClick = {
                            reservationController.rejectReservation(
                                reservationId = selected.id,
                                entrantId = selected.entrantId,
                                campsiteId = selected.campsiteId,
                                campsiteName = campsite?.name ?: "your campsite",
                                onSuccess = {
                                    navController.popBackStack()
                                },
                                onFailure = {
                                    error = it.message ?: "Rejection failed"
                                }
                            )
                        }
                    )
                }
            }

            // test
            composable(Routes.RESERVATION) {

                val campsiteId = "sample-lakeside-camp"
                var message by remember { mutableStateOf("") }
                var isSaving by remember { mutableStateOf(false) }

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
                    message = message,
                    onBackClick = { navController.popBackStack() },
                    onConfirm = { checkIn, checkOut ->
                        if (!isSaving) {
                            isSaving = true
                            message = ""

                            entrantReservationController.createReservation(
                                campsiteId = campsiteId,
                                entrantId = currentUserId,
                                entrantName = "Test Entrant",
                                startDate = checkIn.toString(),
                                endDate = checkOut.toString(),
                                guests = 1,
                                totalPrice = searchController.campsiteDetails(campsiteId)?.let {
                                    it.price * ChronoUnit.DAYS.between(checkIn, checkOut)
                                },
                                rulesAccepted = true,
                                onSuccess = {
                                    isSaving = false
                                    navController.popBackStack()
                                },
                                onFailure = { error ->
                                    isSaving = false
                                    message = error.message ?: "Reservation failed"
                                    android.util.Log.e("ReservationDebug", "Creation failed", error)
                                }
                            )
                        }
                    },
                    onDecline = { navController.popBackStack() },
                    onGuidelinesClick = { navController.navigate(Routes.POLICIES) }
                )
            }

            composable(
                route = Routes.CONVERSATION,
                arguments = listOf(
                    navArgument("campsiteId") {
                        type = NavType.StringType
                    },
                    navArgument("recipientId") {
                        type = NavType.StringType
                    }
                )
            ) { entry ->

                val campsiteId = entry.arguments
                    ?.getString("campsiteId").orEmpty()

                val recipientId = entry.arguments
                    ?.getString("recipientId").orEmpty()

                val campsite =
                    searchController.campsiteDetails(campsiteId)

                if (
                    campsite == null ||
                    recipientId.isBlank() ||
                    recipientId == currentUserId ||
                    (
                            currentUserId != campsite.ownerId &&
                            recipientId != campsite.ownerId
                            )
                ) {
                    Text("Conversation unavailable")
                } else {

                    var messages by remember(campsiteId, recipientId) {
                        mutableStateOf<List<Notification>>(emptyList())
                    }

                    var isSending by remember { mutableStateOf(false) }

                    var error by remember { mutableStateOf("") }

                    DisposableEffect(currentUserId, recipientId, campsiteId) {
                        val stopListening =
                            conversationController.getConversationMessages(
                                currentUserId = currentUserId,
                                recipientId = recipientId,
                                campsiteId = campsiteId,
                                onMessages = { messages = it },
                                onFailure = { error = it.message ?: "Unable to load messages" }
                            )
                        onDispose {
                            stopListening()
                        }
                    }

                    ConversationScreen(
                        currentUserId = currentUserId,
                        recipientName = if (
                            recipientId == campsite.ownerId
                        ) {
                            "Campsite Owner"
                        } else {
                            "Entrant"
                        },
                        campsiteName = campsite.name,
                        messages = emptyList(),
                        isSending = isSending,
                        error = error,
                        onBack = {navController.popBackStack()},
                        onSendMessage = { text, clearInput ->
                            isSending = true
                            error = ""

                            conversationController.sendMessage(
                                currentUserId = currentUserId,
                                recipientId = recipientId,
                                campsiteId = campsiteId,
                                message = text,
                                onSuccess = {
                                    isSending = false
                                    clearInput()
                                },
                                onFailure = {
                                    isSending = false
                                    error = it.message ?: "Unable to send message"
                                }
                            )
                        },
                    )
                }
            }
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

