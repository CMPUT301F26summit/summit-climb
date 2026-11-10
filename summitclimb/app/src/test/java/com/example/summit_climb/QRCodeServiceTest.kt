package com.example.summit_climb

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QRCodeGenerationTest {

    private val service = QRCodeService()

    /*
        Author: Jameo https://stackoverflow.com/users/jameo
        Title: "ZXing convert Bitmap to BinaryBitmap"
        Answer: https://stackoverflow.com/questions/14861553/zxing-convert-bitmap-to-binarybitmap
        Date: 2013-02-18
        License: CC-BY-SA 4.0 (International)
    */
    private fun decode(bitmap: Bitmap): String {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val source = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
        return QRCodeReader().decode(BinaryBitmap(HybridBinarizer(source))).text
    }

    @Test
    fun returnsNullForBlankId() {
        assertNull(service.generateQRCode( ""))
        assertNull(service.generateQRCode(" "))
    }

    @Test
    fun returnsBitmapForValidId() {
        assertNotNull(service.generateQRCode("abc123"))
    }

    @Test
    fun defaultSizeIs512Square() {
        val bitmap = service.generateQRCode("abc123")!!
        assertEquals(512, bitmap.width)
        assertEquals(512, bitmap.height)
    }

    @Test
    fun customSizeIsRespected() {
        val bitmap = service.generateQRCode("abc123", size = 256)!!
        assertEquals(256, bitmap.width)
        assertEquals(256, bitmap.height)
    }

    // The following test was suggested by Claude, Anthropic "What are some possible tests for a QR Code Generation Function?"
    @Test
    fun usesOnlyBlackAndWhitePixels() {
        val bitmap = service.generateQRCode("abc123", size = 128)!!
        val pixels = IntArray(128 * 128)
        bitmap.getPixels(pixels, 0, 128, 0, 0, 128, 128)

        assertTrue(pixels.all { it == Color.BLACK || it == Color.WHITE })
        assertTrue("no black pixels", pixels.any { it == Color.BLACK })
        assertTrue("no white pixels", pixels.any { it == Color.WHITE })
    }

    @Test
    fun differentIdsMakeDifferentImages() {
        val a = service.generateQRCode("campA", size = 128)!!
        val b = service.generateQRCode("campB", size = 128)!!
        assertFalse(a.sameAs(b))
    }

    @Test
    fun sameIdMakesIdenticalImages() {
        val a = service.generateQRCode("campA", size = 128)!!
        val b = service.generateQRCode("campA", size = 128)!!
        assertTrue(a.sameAs(b))
    }

    // The following test was suggested by Claude, Anthropic, "What are some possible tests for a QR Code Generation Function?"
    @Test
    fun scanningTheGeneratedCodeGivesBackTheCampsiteId() {
        val id = "Xk3pQ9mZaB7vL2nRtY8w"
        val bitmap = service.generateQRCode("")!!

        val text = decode(bitmap)

        assertEquals("summitclimb://campsite/$id", text)
        assertEquals(id, service.campsiteIDFromQRText(text))
    }
}