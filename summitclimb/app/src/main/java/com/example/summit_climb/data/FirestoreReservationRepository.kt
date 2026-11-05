package com.example.summit_climb.data

import com.google.firebase.firestore.FirebaseFirestore
import com.example.summit_climb.model.Reservation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.google.firebase.firestore.DocumentReference

/**
 * Handles reservation operations in Firebase Firestore.
 *
 * Stores reservations in the "reservations" collection and tracks booked
 * nights in "reservationBookedNights" to prevent overlapping bookings.
 *
 * Uses Firestore transactions when creating, cancelling, or rejecting
 * reservations so that reservation statuses and booked dates stay consistent.
 */

class FirestoreReservationRepository(
    private val firestore: FirebaseFirestore
) : ReservationRepository {

    /**
     * Retrieves the booked date ranges for active reservations at a campsite.
     *
     * @param campsiteId ID of the campsite.
     * @param onSuccess Called with the booked start and end dates.
     * @param onFailure Called if the Firestore query fails or data is invalid.
     */

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

    /** Retrieves all reservations associated with a campsite. */
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

    /** Rejects an active reservation and releases its booked nights. */
    override fun rejectReservation(
        reservationId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        updateReservationStatus(
            reservationId = reservationId,
            newStatus = "REJECTED",
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    /**
     * Creates a reservation if all requested nights are available.
     *
     * Validates the reservation details, then uses a Firestore transaction
     * to check for conflicting bookings and save the reservation.
     *
     * Each booked night is stored separately to prevent overlapping
     * reservations for the same campsite.
     */

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

            require(campsiteId.isNotBlank()) {"Campsite ID cannot be blank"}
            require(entrantId.isNotBlank()) {"Entrant ID cannot be blank"}
            require(guests > 0) {"Number of guests must be greater than 0"}

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
                nightRefs.forEach { ref ->
                    if (transaction.get(ref).exists()) {
                        throw IllegalStateException(
                            "Some selected dates are already booked"
                        )
                    }
                }

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

    /** Cancels an active reservation and releases its booked nights. */
    override fun cancelReservation(
        reservationId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        updateReservationStatus(
            reservationId = reservationId,
            newStatus = "CANCELLED",
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    /**
     * Updates an active reservation's status and releases its booked nights.
     *
     * Uses a Firestore transaction to ensure that the reservation status
     * and booked-night documents are updated together.
     *
     * The transaction verifies that each existing booked-night document
     * belongs to this reservation before deleting it.
     */
    private fun updateReservationStatus(
        reservationId: String,
        newStatus: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        require(newStatus == "REJECTED" || newStatus == "CANCELLED") {
            "Unsupported reservation status: $newStatus"
        }

        val reservationRef = firestore.collection("reservations")
            .document(reservationId)

        firestore.runTransaction { transaction ->

            val snapshot = transaction.get(reservationRef)

            if (!snapshot.exists()) {throw IllegalStateException("Reservation does not exist")}

            if (snapshot.getString("status") != "ACTIVE") {
                throw IllegalStateException("Reservation is not active")
            }

            val campsiteId = snapshot.getString("campsiteId")
                ?: throw IllegalStateException("Missing campsiteId")

            val startDate = snapshot.getString("startDate")
                ?: throw IllegalStateException("Missing startDate")

            val endDate = snapshot.getString("endDate")
                ?: throw IllegalStateException("Missing endDate")

            val format = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {isLenient = false}

            val start = requireNotNull(format.parse(startDate))
            val end = requireNotNull(format.parse(endDate))

            require(format.format(start) == startDate) {"Invalid start date format"}
            require(format.format(end) == endDate) {"Invalid end date format"}
            require(start.before(end)) {"End date must be after start date"}

            val calendar = Calendar.getInstance().apply {time = start }

            val nightRefs = mutableListOf<DocumentReference>()

            while (calendar.time.before(end)) {
                val date = format.format(calendar.time)

                nightRefs.add(
                    firestore.collection("reservationBookedNights")
                        .document("${campsiteId}_$date")
                )
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            // Firestore requires transaction reads before writes.
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

            transaction.update(reservationRef, "status", newStatus)
            null
        }
            .addOnSuccessListener {onSuccess()}
            .addOnFailureListener { exception -> onFailure(exception)}
    }
}