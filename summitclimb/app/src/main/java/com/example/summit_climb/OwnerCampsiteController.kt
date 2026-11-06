
package com.example.summit_climb

import com.example.summit_climb.service.ImageService
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import android.net.Uri
import android.graphics.Bitmap

/**
 * Handles the registation, validation, and editing of campsites
 * Changes the promo image, lists all the campsites belonging to an owner and provides QR codes
 */
class OwnerCampsiteController(private val campsiteRepository: CampsiteRepository) {
    private val currentCamp = Firebase.firestore.collection("campsites")
    private val qrService = QRCodeService()
    private val imageService = ImageService()


    /**
     * Checks that a campsite's details are valid.
     *
     * @param campsite: campsite to check
     * @return a message about the error or null if the campsite is valid
     */
    fun validate(campsite: Campsite): String? {
        if (campsite.name.isBlank()) {
            return "Name is required"
        }
        if (campsite.location.isBlank()) {
            return "Location is required"
        }
        if (campsite.description.isBlank()) {
            return "Description is required"
        }
        if (campsite.maxOccupancy <= 0) {
            return "MaxOccupancy must be greater than 0"
        }
        if (campsite.price < 0) {
            return "Price cannot be negative"
        }
        if (campsite.latitude == 0.0 && campsite.longitude == 0.0) {
            return "Map location is not valid"
        }
        return null
    }

    /**
     * Saves a new campsite for the owner
     *
     * @param ownerId: ID of the owner registering the campsite
     * @param campsite: details the owner entered
     * @param imageUri: the chosen promo image
     * @param onSuccess: decides outcome upon success
     * @param onFailure: gives message upon failure
     */
    fun registerCampsite(
        ownerId: String,
        campsite: Campsite,
        imageUri: Uri?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {

        val issue = validate(campsite)
        if (issue != null) {
            onFailure(Exception(issue))
            return
        }

        val newCampsite = campsite.copy(
            id = "",
            ownerId = ownerId,
            avgRating = 0.0,
            ratings = emptyMap(),
            bookedDates = emptyList(),
            activeLotteryId = null,
            listingStatus = ListingStatus.PENDING,
            adminNotes = "",
            promoImageUrl = ""
        )

        val document = currentCamp.document()
        document.set(newCampsite)
            .addOnSuccessListener {
                if (imageUri == null) {
                    onSuccess()
                } else {
                    addImage(document.id, imageUri, "", onSuccess) {
                        onFailure(Exception("Campsite information was saved, but the image failed to upload"))
                    }
                }
            }
            .addOnFailureListener { onFailure(Exception("Unable to save the campsite")) }
    }

    /**
     * Updates campsite details for a campsite owner
     *
     * @param ownerId: ID of the owner making the changes
     * @param editCampsite: campsite with the details to change
     * @param onSuccess: decides outcome when the changes were saved
     * @param onFailure: provides a message if something failed
     */
    fun editCampsite(
        ownerId: String,
        editCampsite: Campsite,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val storedCampsite = campsiteRepository.getCampsite(editCampsite.id)
        if (storedCampsite == null) {
            onFailure(Exception("Campsite not found"))
            return
        }

        if (storedCampsite.ownerId != ownerId) {
            onFailure(Exception("Only the owner of this campsite can make changes"))
            return
        }

        val issue = validate(editCampsite)
        if (issue != null) {
            onFailure(Exception(issue))
            return
        }

        val updatedCampsite = storedCampsite.copy(
            name = editCampsite.name,
            location = editCampsite.location,
            maxOccupancy = editCampsite.maxOccupancy,
            description = editCampsite.description,
            amenities = editCampsite.amenities,
            tags = editCampsite.tags,
            price = editCampsite.price,
            rules = editCampsite.rules,
            cancellationPolicy = editCampsite.cancellationPolicy,
            latitude = editCampsite.latitude,
            longitude = editCampsite.longitude,
            availableDates = editCampsite.availableDates
        )
        currentCamp.document(editCampsite.id).set(updatedCampsite)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(Exception("Unable to save changes to campsite")) }
    }

    /**
     * Replaces a campsite's promo image and deletes the old one.
     *
     * @param ownerId: ID of the owner making the change
     * @param campsiteId: campsite whose image changes
     * @param imageUri: the  image
     * @param onSuccess: decides outcome when new image is saved
     * @param onFailure: provides a message if something failed
     */
    fun changePromoImage(
        ownerId: String,
        campsiteId: String,
        imageUri: Uri,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val storedCampsite = campsiteRepository.getCampsite(campsiteId)
        if (storedCampsite == null) {
            onFailure(Exception("Campsite not found"))
            return
        }

        if (storedCampsite.ownerId != ownerId) {
            onFailure(Exception("Only the owner of this campsite can make changes"))
            return
        }

        addImage(campsiteId, imageUri, storedCampsite.promoImageUrl, onSuccess, onFailure)
    }

    /**
     * Lists the campsites that belong to an owner.
     *
     * @param ownerId: ID of the owner
     * @return the owner's campsites
     */
    fun listCampsites(ownerId: String): List<Campsite> {
        return campsiteRepository.searchCampsites("", null).filter { it.ownerId == ownerId }
    }

    /**
     * Generates the QR code for one of the owner's campsites.
     *
     * @param ownerId: ID of campsite owner
     * @param campsiteId: campsite the code points to
     * @return the QR code image or null
     */
    fun getQRCode(ownerId: String, campsiteId: String): Bitmap? {
        val storedCampsite = campsiteRepository.getCampsite(campsiteId) ?: return null
        if (storedCampsite.ownerId != ownerId) {
            return null
        }
        return qrService.generateQRCode(storedCampsite.id)
    }

    // Helper function for changePromoImage
    private fun addImage(
        campsiteId: String,
        imageUri: Uri,
        oldImageUrl: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        imageService.upload(imageUri, "campsites/$campsiteId") { url ->
            if (url == null) {
                onFailure(Exception("Image was not able to be uploaded"))
            } else {
                currentCamp.document(campsiteId).update("promoImageUrl", url)
                    .addOnSuccessListener {
                        imageService.delete(oldImageUrl)
                        onSuccess()
                    }
                    .addOnFailureListener {
                        imageService.delete(url)
                        onFailure(Exception("Unable to save the image"))
                    }
            }
        }
    }
}

