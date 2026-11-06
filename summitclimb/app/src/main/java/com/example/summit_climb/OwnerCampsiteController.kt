/*
package com.example.summit_climb

import com.example.summit_climb.service.ImageService
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import android.net.Uri
import android.graphics.Bitmap
import com.example.summit_climb.data.CampsiteRepository
import com.example.summit_climb.model.Campsite
import com.example.summit_climb.service.QRCodeService

class OwnerCampsiteController(private val campsiteRepository: CampsiteRepository) {
    private val currentCamp = Firebase.firestore.collection("campsites")
    private val qrService = QRCodeService()
    private val imageService = ImageService()

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
            onFailure(Exception("Only the owner of this campsite can make changes"))            return
        }

        addImage(campsiteId, imageUri, storedCampsite.promoImageUrl, onSuccess, onFailure)
    }

    fun listCampsites(ownerId: String): List<Campsite> {
        return campsiteRepository.searchCampsites("", null).filter { it.ownerId == ownerId }
    }

    fun getQRCode(ownerId: String, campsiteId: String): Bitmap? {
        val storedCampsite = campsiteRepository.getCampsite(campsiteId) ?: return null
        if (storedCampsite.ownerId != ownerId) {
            return null
        }
        return qrService.generateQRCode(storedCampsite)
    }

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
*/
