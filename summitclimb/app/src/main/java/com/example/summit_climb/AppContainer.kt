package com.example.summit_climb
import com.example.summit_climb.controller.NotificationController
import com.example.summit_climb.data.FirestoreLotteryRepository
import com.example.summit_climb.data.NotificationRepository
import com.example.summit_climb.data.FirestoreNotificationRepository
import com.example.summit_climb.data.LotteryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AppContainer {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currUserId: String?
        get() = auth.currentUser?.uid

    // Uncomment each line once the repo gets written

    // val userRepository: UserRepository = FirestoreUserRepository(firestore)
    val campsiteRepository: CampsiteRepository by lazy {
        CampsiteRepository()
    }
    val lotteryRepository: LotteryRepository by lazy {
        FirestoreLotteryRepository(firestore)
    }
    // val reservationRepository: ReservationRepository = FirestoreReservationRepository(firestore)
    val notificationRepository: NotificationRepository by lazy {
        FirestoreNotificationRepository(firestore)
    }
    val notificationController: NotificationController by lazy {
        NotificationController(notificationRepository) { currUserId }
    }


}