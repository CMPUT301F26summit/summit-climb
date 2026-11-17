package com.example.summit_climb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.summit_climb.controller.CampsiteSearchController
import com.example.summit_climb.controller.EntrantLotteryController
import com.example.summit_climb.controller.NotificationController
import com.example.summit_climb.controller.OwnerCampsiteController
import com.example.summit_climb.controller.OwnerLotteryController
import com.example.summit_climb.data.CampsiteRepository
import com.example.summit_climb.data.FirestoreLotteryRepository
import com.example.summit_climb.data.FirestoreNotificationRepository
import com.example.summit_climb.data.SampleData
import com.example.summit_climb.ui.AppNavigation
import com.example.summit_climb.ui.theme.SummitclimbTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.summit_climb.controller.OwnerReservationController
import com.example.summit_climb.data.FirestoreReservationRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = FirebaseFirestore.getInstance()
        SampleData.uploadIfMissing(db)

        val auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid ?: "test_user"

        val campsiteRepository = CampsiteRepository()
        val searchController = CampsiteSearchController(campsiteRepository)

        val lotteryRepository = FirestoreLotteryRepository(db)
        val lotteryController = EntrantLotteryController(lotteryRepository, currentUserId)
        val ownerLotteryController = OwnerLotteryController(lotteryRepository)

        val notificationRepository = FirestoreNotificationRepository(db)
        val notificationController = NotificationController(notificationRepository) {
            FirebaseAuth.getInstance().currentUser?.uid
        }

        val reservationRepository = FirestoreReservationRepository(db)
        val reservationController = OwnerReservationController(
            repository = reservationRepository,
            notificationRepository = notificationRepository
        )

        setContent {
            SummitclimbTheme {
                AppNavigation(
                    searchController = searchController,
                    lotteryController = lotteryController,
                    currentUserId = currentUserId,
                    ownerCampsiteController = OwnerCampsiteController(campsiteRepository),
                    notificationController = notificationController,
                    reservationController = reservationController,
                    ownerLotteryController = ownerLotteryController
                )
            }
        }
    }
}
