package com.routee.android.core.data.photo

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class ImageMimeTypeTest {
    @Test
    fun `PNG header overrides JPEG metadata without consuming the image`() {
        val bytes = byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a) + ByteArray(64)
        val input = bytes.inputStream().buffered()

        assertEquals("image/png", imageMimeType(input, "image/jpeg", "image/jpeg"))
        assertArrayEquals(bytes, input.readBytes())
    }

    @Test
    fun `response MIME and extension cover formats not recognized by the platform`() {
        val bytes = "RIFF0000WEBP".toByteArray() + ByteArray(32)

        assertEquals("image/webp", imageMimeType(bytes.inputStream(), "IMAGE/WEBP; charset=binary", null))
        assertEquals("image/png", imageMimeType(ByteArray(32).inputStream(), "application/octet-stream", "image/png"))
    }

    @Test(expected = IllegalStateException::class)
    fun `unknown data is not silently named JPEG`() {
        imageMimeType("not an image".byteInputStream(), "text/html", null)
    }
}
