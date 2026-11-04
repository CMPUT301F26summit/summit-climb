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

class QRCodeService {
    val prefix = "summitclimb://campsite/"

    fun campsiteQRText(campsiteID: String): String {
        return prefix + campsiteID
    }

    fun campsiteIDFromQRText(text: String): String? {
        if (!text.startsWith(prefix)) {
            return null
        }

        val id =  text.removePrefix(prefix).trim()

        if (id.isEmpty()) {
            return null
        }

        return id
    }

    fun generateQRCode(campsite: Campsite, size: Int = 512): Bitmap? {
        if (campsite.id.isBlank()) {
            return null
        }

        val text = campsiteQRText(campsite.id)
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

    // https://developers.google.com/ml-kit/vision/barcode-scanning/code-scanner
    fun scanCampsite(
        context: Context,
        onScan: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        val scanner = GmsBarcodeScanning.getClient(context, options)

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val id = campsiteIDFromQRText(barcode.rawValue.orEmpty())
                if (id != null) {
                    onScan(id)
                } else {
                    onError("There was an error with the campsite code. Please try again")
                }

            }

            .addOnFailureListener {
                onError("There was an error with the scan. Please try again")

            }
    }
}
