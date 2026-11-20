/** package com.example.summit_climb

import com.example.summit_climb.service.QRCodeService
import com.google.zxing.BinaryBitmap
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import com.google.zxing.common.BitMatrix

class QRCodeServiceTest {
    private val dark = 0
    private val light = 255
    private val service = QRCodeService()

    // The following function is from Claude, Anthropic,
    // "How do I write a decoding function to test a qr code scanner?", 2026-11-06
    private fun decode(matrix: BitMatrix): String {
        val width = matrix.width
        val height = matrix.height
        val luminance = ByteArray(width * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                if (matrix[x, y]) {
                    luminance[y * width + x] = dark.toByte()
                } else {
                    luminance[y * width + x] = light.toByte()
                }
            }
        }
        val source = PlanarYUVLuminanceSource(luminance, width, height, 0, 0, width, height, false)
        return QRCodeReader().decode(BinaryBitmap(HybridBinarizer(source))).text
    }

    @Test
    fun returnsNullForBlankId() {
        assertNull(service.generateQRMatrix(""))
        assertNull(service.generateQRMatrix(" "))
    }

    @Test
    fun returnsMatrixWithCorrectSize() {
        val m = service.generateQRMatrix("123", size = 256)!!
        assertEquals(256, m.width)
        assertEquals(256, m.height)
    }

    @Test
    fun differentIDsGenerateDifferentCodes() {
        assertNotEquals(
            service.generateQRMatrix("campA", 128),
            service.generateQRMatrix("campB", 128)
        )
    }

    @Test
    fun identicalIDsGenerateIdenticalCodes() {
        assertEquals(
            service.generateQRMatrix("campA", 128),
            service.generateQRMatrix("campA", 128)
        )
    }

    @Test
    fun scanningCodeReturnsCampsiteID() {
        val id = "abc123"
        val text = decode(service.generateQRMatrix(id)!!)
        assertEquals("summitclimb://campsite/$id", text)
        assertEquals(id, service.campsiteIDFromQRText(text))
    }
}
 */