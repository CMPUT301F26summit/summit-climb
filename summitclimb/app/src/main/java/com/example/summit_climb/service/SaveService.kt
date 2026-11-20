package com.example.summit_climb.service

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore

// The implementation of the save feature uses code from Android Developers
// at https://developer.android.com/training/data-storage/shared/media#kotlin 2026

/** Purpose: saves the QR Code to the device gallery
 * @param context: Context used to access the device's MediaStore
 * @param bitmap: QR code bitmap to save to device
 * @param fileName: Unique filename to store QR Code under, based on the campsite ID
 * @return true if the image successfully saved, otherwise return false
 */
fun saveQRCodeToGallery(
    context: Context,
    bitmap: Bitmap?,
    fileName: String
): Boolean {
    if (bitmap == null || fileName.isBlank()) {
        return false
    }
    val resolver = context.contentResolver

    // find all image files
    val imageCollection =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

    // add a new image
    val newImageDetails = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.png")
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
    }

    val myQrImageUri = resolver.insert(imageCollection, newImageDetails) ?: return false

    return resolver.openOutputStream(myQrImageUri)?.use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    } ?: false
}