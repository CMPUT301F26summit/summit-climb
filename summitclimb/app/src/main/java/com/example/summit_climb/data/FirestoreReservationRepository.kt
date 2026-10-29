package com.example.summit_climb.data

import com.google.firebase.firestore.FirebaseFirestore

class FirestoreReservationRepository(
    private val firestore: FirebaseFirestore
) : ReservationRepository {

    override fun getBookedDates(
        campsiteId: String,
        onSuccess: (List<Pair<String, String>>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("reservations")
            .whereEqualTo("campsiteId", campsiteId)
            .whereEqualTo("status", "ACTIVE")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val bookedDates = querySnapshot.documents.mapNotNull { document ->
                    val startDate = document.getString("startDate")
                    val endDate = document.getString("endDate")

                    if (startDate != null && endDate != null) {
                        Pair(startDate, endDate)
                    } else {
                        null
                    }
                }
                onSuccess(bookedDates)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}