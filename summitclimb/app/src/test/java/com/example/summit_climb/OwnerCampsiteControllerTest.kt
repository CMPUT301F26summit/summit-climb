package com.example.summit_climb

import android.graphics.Bitmap
import android.net.Uri
import com.example.summit_climb.model.Campsite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock

class OwnerCampsiteControllerTest {
    private val uri: Uri = mock()
    private val bitmap: Bitmap = mock()

    private val oldImage = "https://example.com/old.png"
    private val newImage = "https://example.com/new.png"

    private val camp = Campsite(
        id = "camp1",
        ownerId = "owner1",
        name = "Lake View",
        location = "Jasper",
        description = "Lakeside sites",
        maxOccupancy = 4,
        price = 25.0,
        latitude = 52.87,
        longitude = -118.08
    )

    private class FakeStore : CampsiteStore {
        val campsites = mutableListOf<Campsite>()

        var fails = false

        var newCampsite: Campsite? = null
        var editedCampsite: Campsite? = null
        var promoCampsiteId = ""
        var promoUrl: String? = null

        override fun getCampsite(campsiteId: String): Campsite? {
            return campsites.firstOrNull { it.id == campsiteId }
        }

        override fun allCampsites(): List<Campsite> {
            return campsites.toList()
        }

        override fun saveNew(campsite: Campsite, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
            if (fails) {
                onFailure(Exception("Test error"))
            } else {
                newCampsite = campsite
                onSuccess("newCamp")
            }
        }

        override fun save(campsite: Campsite, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
            if (fails) {
                onFailure(Exception("Test error"))
            } else {
                editedCampsite = campsite
                onSuccess()
            }
        }

        override fun setPromoImage(campsiteId: String, url: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
            if (fails) {
                onFailure(Exception("Test error"))
            } else {
                promoCampsiteId = campsiteId
                promoUrl = url
                onSuccess()
            }
        }
    }

    private class FakeImages : ImageUploader {

        var uploadResult: String? = null

        var uploadCalls = 0
        var receivedFolder = ""
        val deletedUrls = mutableListOf<String>()

        override fun upload(imageUri: Uri, folder: String, onDone: (String?) -> Unit) {
            uploadCalls++
            receivedFolder = folder
            onDone(uploadResult)
        }

        override fun delete(imageUrl: String, onDone: (Boolean) -> Unit) {
            deletedUrls.add(imageUrl)
            onDone(true)
        }
    }

    private class FakeQR(private val bitmap: Bitmap) : QRCodeMaker {

        var receivedId = ""

        override fun generateQRCode(campsite: Campsite): Bitmap? {
            receivedId = campsite.id
            return bitmap
        }
    }

    @Test
    fun validCampsiteHasNoIssue() {
        val controller = OwnerCampsiteController(FakeStore(), FakeImages(), FakeQR(bitmap))
        assertNull(controller.validate(camp))
    }

    @Test
    fun blankNameIsRejected() {
        val controller = OwnerCampsiteController(FakeStore(), FakeImages(), FakeQR(bitmap))

        assertEquals("Name is required", controller.validate(camp.copy(name = "  ")))
    }

    @Test
    fun blankLocationIsRejected() {
        val controller = OwnerCampsiteController(FakeStore(), FakeImages(), FakeQR(bitmap))

        assertEquals("Location is required", controller.validate(camp.copy(location = "")))
    }

    @Test
    fun blankDescriptionIsRejected() {
        val controller = OwnerCampsiteController(FakeStore(), FakeImages(), FakeQR(bitmap))

        assertEquals("Description is required", controller.validate(camp.copy(description = "")))
    }

    @Test
    fun zeroOrNegativeOccupancyIsRejected() {
        val controller = OwnerCampsiteController(FakeStore(), FakeImages(), FakeQR(bitmap))

        assertEquals("MaxOccupancy must be greater than 0", controller.validate(camp.copy(maxOccupancy = 0)))
        assertEquals("MaxOccupancy must be greater than 0", controller.validate(camp.copy(maxOccupancy = -3)))
    }

    @Test
    fun negativePriceIsRejected() {
        val controller = OwnerCampsiteController(FakeStore(), FakeImages(), FakeQR(bitmap))

        assertEquals("Price cannot be negative", controller.validate(camp.copy(price = -1.0)))
    }

    @Test
    fun freeCampsiteIsAllowed() {
        val controller = OwnerCampsiteController(FakeStore(), FakeImages(), FakeQR(bitmap))

        assertNull(controller.validate(camp.copy(price = 0.0)))
    }

    @Test
    fun missingMapLocationIsRejected() {
        val controller = OwnerCampsiteController(FakeStore(), FakeImages(), FakeQR(bitmap))

        assertEquals("Map location is not valid", controller.validate(camp.copy(latitude = 0.0, longitude = 0.0)))
    }

    @Test
    fun oneZeroCoordinateIsStillAValidLocation() {
        val controller = OwnerCampsiteController(FakeStore(), FakeImages(), FakeQR(bitmap))

        assertNull(controller.validate(camp.copy(latitude = 0.0, longitude = -118.08)))
    }

    @Test
    fun registerSavesCampsiteForOwnerWaitingForApproval() {
        val store = FakeStore()
        val controller = OwnerCampsiteController(store, FakeImages(), FakeQR(bitmap))

        controller.registerCampsite("owner1", camp.copy(ownerId = "someoneElse"), null, onSuccess = {}, onFailure = {})

        assertEquals("owner1", store.newCampsite?.ownerId)
        assertEquals(ListingStatus.PENDING, store.newCampsite?.listingStatus)
        assertEquals("", store.newCampsite?.id)
    }

    @Test
    fun registerResetsRatingsAndBookings() {
        val store = FakeStore()
        val controller = OwnerCampsiteController(store, FakeImages(), FakeQR(bitmap))

        controller.registerCampsite("owner1", camp.copy(avgRating = 4.9, promoImageUrl = "x"), null, onSuccess = {}, onFailure = {})

        assertEquals(0.0, store.newCampsite?.avgRating)
        assertTrue(store.newCampsite?.ratings?.isEmpty() == true)
        assertTrue(store.newCampsite?.bookedDates?.isEmpty() == true)
        assertNull(store.newCampsite?.activeLotteryId)
        assertEquals("", store.newCampsite?.promoImageUrl)
    }

    @Test
    fun registerWithoutImageCallsSuccessAndSkipsUpload() {
        val images = FakeImages()
        val controller = OwnerCampsiteController(FakeStore(), images, FakeQR(bitmap))
        var succeeded = false

        controller.registerCampsite("owner1", camp, null, onSuccess = { succeeded = true }, onFailure = {})

        assertTrue(succeeded)
        assertEquals(0, images.uploadCalls)
    }

    @Test
    fun registerInvalidCampsiteIsNotSavedAndGivesMessage() {
        val store = FakeStore()
        val controller = OwnerCampsiteController(store, FakeImages(), FakeQR(bitmap))
        var message: String? = null

        controller.registerCampsite("owner1", camp.copy(name = ""), null, onSuccess = {}, onFailure = { message = it.message })

        assertEquals("Name is required", message)
        assertNull(store.newCampsite)
    }

    @Test
    fun registerSaveFailureGivesMessage() {
        val store = FakeStore()
        store.fails = true
        val controller = OwnerCampsiteController(store, FakeImages(), FakeQR(bitmap))
        var message: String? = null

        controller.registerCampsite("owner1", camp, null, onSuccess = {}, onFailure = { message = it.message })

        assertEquals("Unable to save the campsite", message)
    }

    @Test
    fun registerWithImageUploadsIntoNewCampsiteFolderAndSavesUrl() {
        val store = FakeStore()
        val images = FakeImages()
        images.uploadResult = newImage
        val controller = OwnerCampsiteController(store, images, FakeQR(bitmap))
        var succeeded = false

        controller.registerCampsite("owner1", camp, uri, onSuccess = { succeeded = true }, onFailure = {})

        assertEquals("campsites/newCamp", images.receivedFolder)
        assertEquals("newCamp", store.promoCampsiteId)
        assertEquals(newImage, store.promoUrl)
        assertTrue(succeeded)
    }

    @Test
    fun registerWithImageDeletesNothing() {
        val images = FakeImages()
        images.uploadResult = newImage
        val controller = OwnerCampsiteController(FakeStore(), images, FakeQR(bitmap))

        controller.registerCampsite("owner1", camp, uri, onSuccess = {}, onFailure = {})

        assertTrue(images.deletedUrls.isEmpty())
    }

    @Test
    fun registerImageUploadFailureKeepsCampsiteAndGivesMessage() {
        val store = FakeStore()
        val controller = OwnerCampsiteController(store, FakeImages(), FakeQR(bitmap))
        var message: String? = null

        controller.registerCampsite("owner1", camp, uri, onSuccess = {}, onFailure = { message = it.message })

        assertEquals("Campsite information was saved, but the image failed to upload", message)
        assertEquals("owner1", store.newCampsite?.ownerId)
    }

    @Test
    fun editSavesNewDetailsOnTheStoredCampsite() {
        val store = FakeStore()
        store.campsites.add(camp)
        val controller = OwnerCampsiteController(store, FakeImages(), FakeQR(bitmap))
        var succeeded = false

        controller.editCampsite("owner1", camp.copy(name = "Pine Ridge", price = 40.0), onSuccess = { succeeded = true }, onFailure = {})

        assertEquals("Pine Ridge", store.editedCampsite?.name)
        assertEquals(40.0, store.editedCampsite?.price)
        assertTrue(succeeded)
    }

    @Test
    fun editKeepsOwnerAndRatingsFromStoredCampsite() {
        val store = FakeStore()
        store.campsites.add(camp.copy(avgRating = 4.2))
        val controller = OwnerCampsiteController(store, FakeImages(), FakeQR(bitmap))

        controller.editCampsite("owner1", camp.copy(ownerId = "someoneElse", avgRating = 0.0), onSuccess = {}, onFailure = {})

        assertEquals("owner1", store.editedCampsite?.ownerId)
        assertEquals(4.2, store.editedCampsite?.avgRating)
    }

    @Test
    fun editUnknownCampsiteGivesMessage() {
        val store = FakeStore()
        val controller = OwnerCampsiteController(store, FakeImages(), FakeQR(bitmap))
        var message: String? = null

        controller.editCampsite("owner1", camp, onSuccess = {}, onFailure = { message = it.message })

        assertEquals("Campsite not found", message)
        assertNull(store.editedCampsite)
    }

    @Test
    fun editByAnotherOwnerIsRejected() {
        val store = FakeStore()
        store.campsites.add(camp)
        val controller = OwnerCampsiteController(store, FakeImages(), FakeQR(bitmap))
        var message: String? = null

        controller.editCampsite("owner2", camp.copy(name = "Hijacked"), onSuccess = {}, onFailure = { message = it.message })

        assertEquals("Only the owner of this campsite can make changes", message)
        assertNull(store.editedCampsite)
    }

    @Test
    fun editWithInvalidDetailsIsRejected() {
        val store = FakeStore()
        store.campsites.add(camp)
        val controller = OwnerCampsiteController(store, FakeImages(), FakeQR(bitmap))
        var message: String? = null

        controller.editCampsite("owner1", camp.copy(name = ""), onSuccess = {}, onFailure = { message = it.message })

        assertEquals("Name is required", message)
        assertNull(store.editedCampsite)
    }

    @Test
    fun editSaveFailureGivesMessage() {
        val store = FakeStore()
        store.campsites.add(camp)
        val controller = OwnerCampsiteController(store, FakeImages(), FakeQR(bitmap))
        store.fails = true
        var message: String? = null

        controller.editCampsite("owner1", camp, onSuccess = {}, onFailure = { message = it.message })

        assertEquals("Unable to save changes to campsite", message)
    }

    @Test
    fun newPromoImageIsSavedAndOldOneDeleted() {
        val store = FakeStore()
        store.campsites.add(camp.copy(promoImageUrl = oldImage))
        val images = FakeImages()
        images.uploadResult = newImage
        val controller = OwnerCampsiteController(store, images, FakeQR(bitmap))
        var succeeded = false

        controller.changePromoImage("owner1", "camp1", uri, onSuccess = { succeeded = true }, onFailure = {})

        assertEquals("camp1", store.promoCampsiteId)
        assertEquals(newImage, store.promoUrl)
        assertEquals(listOf(oldImage), images.deletedUrls)
        assertTrue(succeeded)
    }

    @Test
    fun promoImageUploadsToCampsiteFolder() {
        val store = FakeStore()
        store.campsites.add(camp)
        val images = FakeImages()
        images.uploadResult = newImage
        val controller = OwnerCampsiteController(store, images, FakeQR(bitmap))

        controller.changePromoImage("owner1", "camp1", uri, onSuccess = {}, onFailure = {})

        assertEquals("campsites/camp1", images.receivedFolder)
    }

    @Test
    fun noOldPromoImageMeansNothingIsDeleted() {
        val store = FakeStore()
        store.campsites.add(camp.copy(promoImageUrl = ""))
        val images = FakeImages()
        images.uploadResult = newImage
        val controller = OwnerCampsiteController(store, images, FakeQR(bitmap))

        controller.changePromoImage("owner1", "camp1", uri, onSuccess = {}, onFailure = {})

        assertTrue(images.deletedUrls.isEmpty())
    }

    @Test
    fun promoImageForUnknownCampsiteGivesMessage() {
        val images = FakeImages()
        val controller = OwnerCampsiteController(FakeStore(), images, FakeQR(bitmap))
        var message: String? = null

        controller.changePromoImage("owner1", "missing", uri, onSuccess = {}, onFailure = { message = it.message })

        assertEquals("Campsite not found", message)
        assertEquals(0, images.uploadCalls)
    }

    @Test
    fun promoImageByAnotherOwnerIsRejectedBeforeUploading() {
        val store = FakeStore()
        store.campsites.add(camp)
        val images = FakeImages()
        images.uploadResult = newImage
        val controller = OwnerCampsiteController(store, images, FakeQR(bitmap))
        var message: String? = null

        controller.changePromoImage("owner2", "camp1", uri, onSuccess = {}, onFailure = { message = it.message })

        assertEquals("Only the owner of this campsite can make changes", message)
        assertEquals(0, images.uploadCalls)
    }

    @Test
    fun promoImageUploadFailureKeepsOldImageAndGivesMessage() {
        val store = FakeStore()
        store.campsites.add(camp.copy(promoImageUrl = oldImage))
        val images = FakeImages()
        val controller = OwnerCampsiteController(store, images, FakeQR(bitmap))
        var message: String? = null

        controller.changePromoImage("owner1", "camp1", uri, onSuccess = {}, onFailure = { message = it.message })

        assertEquals("Image was not able to be uploaded", message)
        assertNull(store.promoUrl)
        assertTrue(images.deletedUrls.isEmpty())
    }

    @Test
    fun promoImageSaveFailureDeletesTheNewUpload() {
        val store = FakeStore()
        store.campsites.add(camp.copy(promoImageUrl = oldImage))
        val images = FakeImages()
        images.uploadResult = newImage
        val controller = OwnerCampsiteController(store, images, FakeQR(bitmap))
        store.fails = true
        var message: String? = null

        controller.changePromoImage("owner1", "camp1", uri, onSuccess = {}, onFailure = { message = it.message })

        assertEquals("Unable to save the image", message)
        assertEquals(listOf(newImage), images.deletedUrls)
    }

    @Test
    fun listCampsitesOnlyReturnsTheOwnersCampsites() {
        val store = FakeStore()
        store.campsites.add(camp.copy(id = "a", ownerId = "owner1"))
        store.campsites.add(camp.copy(id = "b", ownerId = "owner2"))
        store.campsites.add(camp.copy(id = "c", ownerId = "owner1"))
        val controller = OwnerCampsiteController(store, FakeImages(), FakeQR(bitmap))

        assertEquals(listOf("a", "c"), controller.listCampsites("owner1").map { it.id })
    }

    @Test
    fun ownerWithNoCampsitesGetsEmptyList() {
        val store = FakeStore()
        store.campsites.add(camp)
        val controller = OwnerCampsiteController(store, FakeImages(), FakeQR(bitmap))

        assertTrue(controller.listCampsites("owner9").isEmpty())
    }

    @Test
    fun ownerGetsQRCodeForTheirCampsite() {
        val store = FakeStore()
        store.campsites.add(camp)
        val qr = FakeQR(bitmap)
        val controller = OwnerCampsiteController(store, FakeImages(), qr)

        assertSame(bitmap, controller.getQRCode("owner1", "camp1"))
        assertEquals("camp1", qr.receivedId)
    }

    @Test
    fun anotherOwnerGetsNoQRCode() {
        val store = FakeStore()
        store.campsites.add(camp)
        val qr = FakeQR(bitmap)
        val controller = OwnerCampsiteController(store, FakeImages(), qr)

        assertNull(controller.getQRCode("owner2", "camp1"))
        assertEquals("", qr.receivedId)
    }

    @Test
    fun unknownCampsiteGetsNoQRCode() {
        val controller = OwnerCampsiteController(FakeStore(), FakeImages(), FakeQR(bitmap))

        assertNull(controller.getQRCode("owner1", "missing"))
    }
}