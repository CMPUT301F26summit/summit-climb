package com.example.summit_climb.data

import com.google.firebase.firestore.FirebaseFirestore
import com.example.summit_climb.model.Reservation
import java.text.SimpleDateFormat
import java.util.*
import kotlin.IllegalStateException
import kotlin.Exception
import kotlin.collections.mapOf

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
                        val reservation = document.toObject(Reservation::class.java)
                            ?: throw IllegalStateException("Missing reservation data")
                        reservation.copy(id = document.id)
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
                throw IllegalStateException("Reservation is not active")
            }

            val campsiteId = snapshot.getString("campsiteId")
                ?: throw IllegalStateException("Missing campsiteId")
            val startDate = snapshot.getString("startDate")
                ?: throw IllegalStateException("Missing startDate")
            val endDate = snapshot.getString("endDate")
                ?: throw IllegalStateException("Missing endDate")

            val format = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                isLenient = false
            }

            val start = requireNotNull(format.parse(startDate))
            val end = requireNotNull(format.parse(endDate))

            require(start.before(end)) {
                "Invalid reservation dates"
            }

            val calendar = Calendar.getInstance().apply {
                time = start
            }

            val nightRefs = mutableListOf<com.google.firebase.firestore.DocumentReference>()

            while (calendar.time.before(end)) {
                val date = format.format(calendar.time)

                nightRefs.add(
                    firestore.collection("reservationBookedNights")
                        .document("${campsiteId}_$date")
                )

                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            // Read the booked-night documents before making changes.
            val locks = nightRefs.map { ref ->
                ref to transaction.get(ref)
            }

            // Release only nights belonging to this reservation.
            locks.forEach { (ref, lock) ->
                if (lock.exists()) {
                    if (lock.getString("reservationId") != reservationId) {
                        throw IllegalStateException(
                            "Booked night belongs to another reservation"
                        )
                    }

                    transaction.delete(ref)
                }
            }


            transaction.update(reservationRef, "status", "REJECTED")
            null
        }
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    override fun createReservation(
        campsiteId: String,
        entrantId: String,
        entrantName: String,
        startDate: String,
        endDate: String,
        guests: Int,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                isLenient = false
            }

            require(campsiteId.isNotBlank())
            require(entrantId.isNotBlank())
            require(guests > 0)

            val start = requireNotNull(format.parse(startDate))
            val end = requireNotNull(format.parse(endDate))

            require(format.format(start) == startDate)
            require(format.format(end) == endDate)
            require(start.before(end)) {
                "End date must be after start date"
            }

            val dates = mutableListOf<String>()
            val calendar = Calendar.getInstance().apply {
                time = start
            }

            while (calendar.time.before(end)) {

                dates.add(format.format(calendar.time))
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            val reservationRef = firestore.collection("reservations").document()

            val reservation = Reservation(
                id = reservationRef.id,
                campsiteId = campsiteId,
                entrantId = entrantId,
                entrantName = entrantName,
                startDate = startDate,
                endDate = endDate,
                guests = guests,
                status = "ACTIVE"
            )

            val nightRefs = dates.map { date ->
                firestore.collection("reservationBookedNights")
                    .document("${campsiteId}_$date")
            }

            firestore.runTransaction { transaction ->

                // Check availability before writing anything.
                nightRefs.forEach { ref ->
                    if (transaction.get(ref).exists()) {
                        throw IllegalStateException(
                            "Some selected dates are already booked"
                        )
                    }
                }

                // Reserve each night.
                nightRefs.forEach { ref ->
                    transaction.set(
                        ref,
                        mapOf("reservationId" to reservationRef.id)
                    )
                }

                // Save the reservation in the same transaction.
                transaction.set(reservationRef, reservation)

                null
            }
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }

        } catch (exception: Exception) {
            onFailure(exception)
        }
    }

    override fun cancelReservation(
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
                throw IllegalStateException("Reservation is not active")
            }

            val campsiteId = snapshot.getString("campsiteId")
                ?: throw IllegalStateException("Missing campsiteId")
            val startDate = snapshot.getString("startDate")
                ?: throw IllegalStateException("Missing startDate")
            val endDate = snapshot.getString("endDate")
                ?: throw IllegalStateException("Missing endDate")

            val format = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                isLenient = false
            }

            val start = requireNotNull(format.parse(startDate))
            val end = requireNotNull(format.parse(endDate))

            require(start.before(end)) {
                "Invalid reservation dates"
            }

            val calendar = Calendar.getInstance().apply {
                time = start
            }

            val nightRefs = mutableListOf<com.google.firebase.firestore.DocumentReference>()

            while (calendar.time.before(end)) {
                val date = format.format(calendar.time)

                nightRefs.add(
                    firestore.collection("reservationBookedNights")
                        .document("${campsiteId}_$date")
                )

                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            val locks = nightRefs.map { ref ->
                ref to transaction.get(ref)
            }

            locks.forEach { (ref, lock) ->
                if (lock.exists()) {
                    if (lock.getString("reservationId") != reservationId) {
                        throw IllegalStateException(
                            "Booked night belongs to another reservation"
                        )
                    }

                    transaction.delete(ref)
                }
            }

            transaction.update(reservationRef, "status", "CANCELLED")
            null
        }
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }
}