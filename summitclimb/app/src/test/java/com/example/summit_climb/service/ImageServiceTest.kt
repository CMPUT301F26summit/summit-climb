package com.example.summit_climb.service

import android.net.Uri
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Purpose: tests ImageService without touching real Firebase.
 * Firebase Storage is replaced with fake objects, and each fake task
 * immediately reports success or failure, so we can check what ImageService does.
 */

class ImageServiceTest {

    private lateinit var storage: FirebaseStorage
    private lateinit var rootRef: StorageReference
    private lateinit var imageRef: StorageReference
    private lateinit var service: ImageService
    private val imageUri: Uri = mock()
    private val fakeUrl = "https://fake.url/photo.jpg"

    // runs before every test: builds a fresh fake storage
    @Before
    fun setUp() {
        storage = mock()
        rootRef = mock()
        imageRef = mock()
        whenever(storage.reference).thenReturn(rootRef)
        whenever(rootRef.child(any())).thenReturn(imageRef)
        service = ImageService(storage)
    }

    // a fake upload that immediately succeeds or fails
    @Suppress("UNCHECKED_CAST")
    private fun fakeUpload(succeeds: Boolean): UploadTask {
        val task: UploadTask = mock()
        whenever(task.addOnSuccessListener(any())).thenAnswer {
            if (succeeds) (it.arguments[0] as OnSuccessListener<UploadTask.TaskSnapshot>).onSuccess(mock())
            task
        }
        whenever(task.addOnFailureListener(any())).thenAnswer {
            if (!succeeds) (it.arguments[0] as OnFailureListener).onFailure(Exception("upload failed"))
            task
        }
        return task
    }

    //A fake "get download URL" task that immediately succeeds or fails
    @Suppress("UNCHECKED_CAST")
    private fun fakeUrlTask(succeeds: Boolean): Task<Uri> {
        val url: Uri = mock()
        whenever(url.toString()).thenReturn(fakeUrl)
        val task: Task<Uri> = mock()
        whenever(task.addOnSuccessListener(any())).thenAnswer {
            if (succeeds) (it.arguments[0] as OnSuccessListener<Uri>).onSuccess(url)
            task
        }
        whenever(task.addOnFailureListener(any())).thenAnswer {
            if (!succeeds) (it.arguments[0] as OnFailureListener).onFailure(Exception("no url"))
            task
        }
        return task
    }

    //A fake delete task that immediately succeeds or fails
    @Suppress("UNCHECKED_CAST")
    private fun fakeDeleteTask(succeeds: Boolean): Task<Void> {
        val task: Task<Void> = mock()
        whenever(task.addOnSuccessListener(any())).thenAnswer {
            if (succeeds) (it.arguments[0] as OnSuccessListener<Void?>).onSuccess(null)
            task
        }
        whenever(task.addOnFailureListener(any())).thenAnswer {
            if (!succeeds) (it.arguments[0] as OnFailureListener).onFailure(Exception("delete failed"))
            task
        }
        return task
    }

    @Test
    fun uploadSuccessGivesBackDownloadUrl() {
        val upload = fakeUpload(succeeds = true)
        val urlTask = fakeUrlTask(succeeds = true)
        whenever(imageRef.putFile(imageUri)).thenReturn(upload)
        whenever(imageRef.downloadUrl).thenReturn(urlTask)

        var result: String? = "not called"
        service.upload(imageUri, "campsites") { result = it }

        assertEquals(fakeUrl, result)
    }

    @Test
    fun uploadFailureGivesNull() {
        val upload = fakeUpload(succeeds = false)
        whenever(imageRef.putFile(imageUri)).thenReturn(upload)

        var result: String? = "not called"
        service.upload(imageUri, "campsites") { result = it }

        assertNull(result)
    }

    @Test
    fun uploadWorksButUrlFailsGivesNull() {
        val upload = fakeUpload(succeeds = true)
        val urlTask = fakeUrlTask(succeeds = false)
        whenever(imageRef.putFile(imageUri)).thenReturn(upload)
        whenever(imageRef.downloadUrl).thenReturn(urlTask)

        var result: String? = "not called"
        service.upload(imageUri, "campsites") { result = it }

        assertNull(result)
    }

    @Test
    fun uploadSavesInGivenFolderAsJpg() {
        val upload = fakeUpload(succeeds = false)
        whenever(imageRef.putFile(imageUri)).thenReturn(upload)

        service.upload(imageUri, "campsites") {}

        val path = argumentCaptor<String>()
        verify(rootRef).child(path.capture())
        assertTrue(path.firstValue.startsWith("campsites/"))
        assertTrue(path.firstValue.endsWith(".jpg"))
    }

    @Test
    fun twoUploadsGetDifferentFileNames() {
        val upload = fakeUpload(succeeds = false)
        whenever(imageRef.putFile(imageUri)).thenReturn(upload)

        service.upload(imageUri, "profiles") {}
        service.upload(imageUri, "profiles") {}

        val paths = argumentCaptor<String>()
        verify(rootRef, times(2)).child(paths.capture())
        assertNotEquals(paths.firstValue, paths.secondValue)
    }

    @Test
    fun deleteBlankUrlDoesNothingAndReportsTrue() {
        var result: Boolean? = null
        service.delete("") { result = it }

        assertEquals(true, result)
        verify(storage, never()).getReferenceFromUrl(any())
    }

    @Test
    fun deleteSuccessGivesTrue() {
        val deleteTask = fakeDeleteTask(succeeds = true)
        whenever(storage.getReferenceFromUrl(fakeUrl)).thenReturn(imageRef)
        whenever(imageRef.delete()).thenReturn(deleteTask)

        var result: Boolean? = null
        service.delete(fakeUrl) { result = it }

        assertEquals(true, result)
    }

    @Test
    fun deleteFailureGivesFalse() {
        val deleteTask = fakeDeleteTask(succeeds = false)
        whenever(storage.getReferenceFromUrl(fakeUrl)).thenReturn(imageRef)
        whenever(imageRef.delete()).thenReturn(deleteTask)

        var result: Boolean? = null
        service.delete(fakeUrl) { result = it }

        assertEquals(false, result)
    }
}