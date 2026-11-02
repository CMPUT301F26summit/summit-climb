package com.example.summit_climb.service

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

/**
 * Purpose: uploads and deletes images in Firebase Storage.
 * Screens load images by giving the saved URL to an image loader (like Coil's AsyncImage),
 * so this class only needs to hand back that URL.
 */

// Uploads and deletes images (campsite photos, profile pictures).
class ImageService(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    /**
     * uploads an image picked from the phone into a folder (e.g. "campsites", "profiles")
     * calls onDone with the image's download URL, or null if the upload failed
     */
    fun upload(imageUri: Uri, folder: String, onDone: (String?) -> Unit) {
        val ref = storage.reference.child("$folder/${UUID.randomUUID()}.jpg")

        ref.putFile(imageUri)
            .addOnSuccessListener {
                ref.downloadUrl
                    .addOnSuccessListener { url -> onDone(url.toString()) }
                    .addOnFailureListener {
                        e -> Log.e("ImageService", "Couldn't get download URL", e)
                        onDone(null)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("ImageService", "Upload failed", e)
                onDone(null)
            }
    }

    // deletes an image using the URL that upload() returned
    fun delete(imageUrl: String, onDone: (Boolean) -> Unit = {}) {
        // Nothing to delete
        if (imageUrl.isBlank()) {
            onDone(true)
            return
        }
        storage.getReferenceFromUrl(imageUrl).delete()
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener {
                e -> Log.e("ImageService", "Delete failed", e)
                onDone(false)
            }
    }
}