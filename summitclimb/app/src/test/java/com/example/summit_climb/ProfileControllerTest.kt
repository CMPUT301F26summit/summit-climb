package com.example.summit_climb

import android.net.Uri
import org.junit.Assert
import org.junit.Test

class ProfileControllerTest {
    private val oldPhoto = "https://example.com/old.png"
    private val newPhoto = "https://example.com/new.png"

    private val uri: Uri = mock()

    private val mila = User(
        userId = "user1",
        name = "Mila",
        email = "mila@example.com",
        phoneNumber = "7805551234",
        profileImageUrl = oldPhoto
    )

    private class FakeRepository(private val user: User? = null) : UserDataSource {
        var fails = false

        var getUserCalls = 0
        var receivedUid = ""

        var saveCalls = 0
        var savedUser: User? = null

        var notificationCalls = 0
        var notificationUserId = ""
        var receivedOptOut: Boolean? = null

        var deletedUser: User? = null

        override fun getUser(uid: String, onFailure: (Exception) -> Unit, onResult: (User) -> Unit) {
            getUserCalls++
            receivedUid = uid

            if (fails || user == null) {
                onFailure(Exception("Test error"))
            } else {
                onResult(user)
            }
        }

        override fun saveProfile(user: User, onFailure: (Exception) -> Unit, onSuccess: () -> Unit) {
            saveCalls++
            savedUser = user

            if (fails) {
                onFailure(Exception("Test error"))
            } else {
                onSuccess()
            }
        }

        override fun updateNotificationOpt(userId: String, optOut: Boolean, onFailure: (Exception) -> Unit) {
            notificationCalls++
            notificationUserId = userId
            receivedOptOut = optOut

            if (fails) {
                onFailure(Exception("Test error"))
            }
        }

        override fun deleteUser(user: User) {
            deletedUser = user
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

    private class FakeAccount(override var currentUserId: String? = "user1") : AccountService {
        var fails = false

        var deleteCalls = 0

        override fun deleteAccount(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
            deleteCalls++
            if (fails) {
                onFailure(Exception("Test error"))
            } else {
                onSuccess()
            }
        }
    }

    @Test
    fun loadProfileFillsEditableFields() {
        val controller = ProfileController(FakeRepository(mila), FakeImages(), FakeAccount())
        Assert.assertEquals("Mila", controller.name)
        Assert.assertEquals("mila@example.com", controller.email)
        Assert.assertEquals("7805551234", controller.phone)
        Assert.assertEquals(oldPhoto, controller.photoUrl)
        Assert.assertEquals("user1", controller.currentUser?.userId)
    }

    @Test
    fun loadProfileAsksForSignedInUser() {
        val repository = FakeRepository(mila)
        ProfileController(repository, FakeImages(), FakeAccount("user1"))
        Assert.assertEquals("user1", repository.receivedUid)
    }

    @Test
    fun optedOutUserHasNotificationsOff() {
        val user = User(userId = "user1", notificationsOptedOut = true)
        val controller = ProfileController(FakeRepository(user), FakeImages(), FakeAccount())
        Assert.assertFalse(controller.notificationsStatus)
    }

    @Test
    fun optedInUserHasNotificationsOn() {
        val user = User(userId = "user1", notificationsOptedOut = false)
        val controller = ProfileController(FakeRepository(user), FakeImages(), FakeAccount())

        Assert.assertTrue(controller.notificationsStatus)
    }


    @Test
    fun loadProfileFailureProvidesMessage() {
        val repository = FakeRepository(mila)
        repository.fails = true
        val controller = ProfileController(repository, FakeImages(), FakeAccount())
        var message: String? = null

        controller.loadProfile(onFailure = { message = it.message })

        Assert.assertEquals("Unable to load profile", message)
        Assert.assertNull(controller.currentUser)
    }

    @Test
    fun savedProfileStoresEditedDetails() {
        val repository = FakeRepository(mila)
        val controller = ProfileController(repository, FakeImages(), FakeAccount())

        controller.name = "Sammy"
        controller.email = "sam@gmail.com"
        controller.phone = "1234"
        controller.saveProfile()

        Assert.assertEquals("Sammy", repository.savedUser?.name)
        Assert.assertEquals("sam@gmail.com", repository.savedUser?.email)
        Assert.assertEquals("1234", repository.savedUser?.phoneNumber)
    }

    @Test
    fun editingDoesNotSaveUntilSavePressed() {
        val repository = FakeRepository(mila)
        val controller = ProfileController(repository, FakeImages(), FakeAccount())

        controller.name = "Sam"

        Assert.assertEquals(0, repository.saveCalls)
    }


    @Test
    fun saveProfileFailureProvidesMessage() {
        val repository = FakeRepository(mila)
        val controller = ProfileController(repository, FakeImages(), FakeAccount())
        repository.fails = true
        var message: String? = null

        controller.saveProfile(onFailure = { message = it.message })

        Assert.assertEquals("Unable to save profile", message)
    }

    @Test
    fun turningNotificationsOffOptsUserOut() {
        val repository = FakeRepository(mila)
        val controller = ProfileController(repository, FakeImages(), FakeAccount())

        controller.setNotifications(false)

        Assert.assertFalse(controller.notificationsStatus)
        Assert.assertEquals("user1", repository.notificationUserId)
        Assert.assertEquals(true, repository.receivedOptOut)
    }

    @Test
    fun turningNotificationsOnOptsUserIn() {
        val user = User(userId = "user1", notificationsOptedOut = true)
        val repository = FakeRepository(user)
        val controller = ProfileController(repository, FakeImages(), FakeAccount())

        controller.setNotifications(true)

        Assert.assertTrue(controller.notificationsStatus)
        Assert.assertEquals(false, repository.receivedOptOut)
    }

    @Test
    fun deleteProfileRemovesUserAndMarksDeleted() {
        val repository = FakeRepository(mila)
        val account = FakeAccount()
        val controller = ProfileController(repository, FakeImages(), account)

        controller.deleteProfile()

        Assert.assertEquals(1, account.deleteCalls)
        Assert.assertEquals("user1", repository.deletedUser?.userId)
        Assert.assertNull(controller.currentUser)
        Assert.assertTrue(controller.deleteStatus)
    }

    @Test
    fun deleteFailureKeepsUserAndGivesMessage() {
        val repository = FakeRepository(mila)
        val account = FakeAccount()
        val controller = ProfileController(repository, FakeImages(), account)
        account.fails = true
        var message: String? = null

        controller.deleteProfile(onFailure = { message = it.message })

        Assert.assertNull(repository.deletedUser)
        Assert.assertEquals("user1", controller.currentUser?.userId)
        Assert.assertFalse(controller.deleteStatus)
        Assert.assertEquals("Unable to delete account", message)
    }



    @Test
    fun newPhotoIsSavedAndOldOneDeleted() {
        val repository = FakeRepository(mila)
        val images = FakeImages()
        images.uploadResult = newPhoto
        val controller = ProfileController(repository, images, FakeAccount())

        controller.updateProfilePhoto(uri)

        Assert.assertEquals(newPhoto, controller.photoUrl)
        Assert.assertEquals(newPhoto, repository.savedUser?.profileImageUrl)
        Assert.assertFalse(controller.uploadPhoto)
        Assert.assertEquals(listOf(oldPhoto), images.deletedUrls)
    }

    @Test
    fun uploadFailureKeepsOldPhotoAndGivesMessage() {
        val repository = FakeRepository(mila)
        val images = FakeImages()
        val controller = ProfileController(repository, images, FakeAccount())
        var message: String? = null

        controller.updateProfilePhoto(uri, onFailure = { message = it.message })

        Assert.assertEquals(oldPhoto, controller.photoUrl)
        Assert.assertFalse(controller.uploadPhoto)
        Assert.assertEquals("Unable to upload photo", message)
        Assert.assertEquals(0, repository.saveCalls)
    }

}