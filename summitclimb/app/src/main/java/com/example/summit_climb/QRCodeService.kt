package com.example.summit_climb

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.EncodeHintType
import android.content.Context
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

/**
 * Handles the creation of unique QR codes for each campsite
 * Allows users to scan QR Code to access campsite details
 */
class QRCodeService {
    private val linkPrefix = "summitclimb://campsite/"

    /**
     * Creates the text that is used to build campsite's unique QR code
     * Ensures that scanned text belongs to a campsite from the app
     *
     * @param campsiteID: ID of the campsite
     * @return the app's prefix with the unique ID attached
     */
    fun campsiteQRText(campsiteID: String): String {
        return linkPrefix + campsiteID
    }

    /**
     * Obtains the campsite ID from the text of a scanned QR code
     *
     * @param text: the text read from the QR code
     * @return the campsite ID, or null if the text or id is invalid
     */
    fun campsiteIDFromQRText(text: String): String? {
        if (!text.startsWith(linkPrefix)) {
            return null
        }

        val id = text.removePrefix(linkPrefix).trim()

        if (id.isEmpty()) {
            return null
        }

        return id
    }

    /**
     * The following function contains lines that were created by Nishikanto Sarkar in Medium at
     * https://medium.com/@nishikanto.abm/generating-custom-qr-codes-in-android-with-kotlin-a-technical-guide-a0adb06757d4
     */

    /**
     * Generates a unique QR Code
     * @param campsite: the campsite that will be linked to the code
     * @param size: width and height of the QR code in pixels (default = 512)
     * @return a bitmap, or null if the id, size, or bitmap is not valid
     */
    fun generateQRCode(campsiteID: String, size: Int = 512): Bitmap? {
        if (campsiteID.isBlank() || size <= 0) {
            return null
        }

        val text = campsiteQRText(campsiteID)
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 1
        )

        val bitMatrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size, hints)
        val pixels = IntArray(size * size)

        for (y in 0 until size) {
            for (x in 0 until size) {
                if (bitMatrix[x,y]) {
                    pixels[y * size + x] = Color.BLACK
                } else {
                    pixels[y * size + x] = Color.WHITE
                }
            }
        }

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
        return bitmap
    }


    /**
     * This function contains lines that were created by Google Developers
     * at https://developers.google.com/ml-kit/vision/barcode-scanning/code-scanner
     */

    /**
     * Opens the Google Scanner so users can scan a QR Code
     * @param context: screen the scanner opens from
     * @param onScan: determines outcome if scanner successfully scans
     * @param onError: provides error message if scan fails
     * @param onCancel: determines outcome if scanner is closed before finishing
     */
    fun scanCampsite(
        context: Context,
        onScan: (String) -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit = {}
    ) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        val scanner = GmsBarcodeScanning.getClient(context, options)

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                onScan(barcode.rawValue.orEmpty())
            }
            .addOnCanceledListener {
                onCancel()
            }
            .addOnFailureListener {
                onError("There was an error with the scan. Please try again")
            }
    }
}
