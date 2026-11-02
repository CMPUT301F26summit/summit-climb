package com.example.summit_climb.data

import com.google.firebase.firestore.FirebaseFirestore
import com.example.summit_climb.model.Reservation

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
                try {
                    val bookedDates = querySnapshot.documents.map { document ->
                        val startDate = document.getString("startDate")
                            ?: throw IllegalStateException("Missing start date")
                        val endDate = document.getString("endDate")
                            ?: throw IllegalStateException("Missing end date")
                        Pair(startDate, endDate)
                    }
                    onSuccess(bookedDates)
                } catch (e: Exception) {
                    onFailure(e)
                }
            }

            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    override fun getReservationsByCampsite(
        campsiteId: String,
        onSuccess: (List<Reservation>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("reservations")
            .whereEqualTo("campsiteId", campsiteId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                try {
                    val reservations = querySnapshot.documents.map { document ->
                        document.toObject(Reservation::class.java)
                            ?: throw IllegalStateException("Missing reservation data")
                    }
                    onSuccess(reservations)

                } catch (e: Exception) { onFailure(e) }
            }
            .addOnFailureListener { exception -> onFailure(exception) }
        }

    override fun rejectReservation(
        reservationId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val reservationRef = firestore.collection("reservations")
            .document(reservationId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(reservationRef)

            if (!snapshot.exists()) {
                throw IllegalStateException("Reservation does not exist")
            }

            if (snapshot.getString("status") != "ACTIVE") {
                throw IllegalStateException("Reservation is not active") }
            }
        }
}