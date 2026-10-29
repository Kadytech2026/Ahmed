package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.ImageStats
import com.example.model.ResolutionPreset
import com.example.model.SharpnessLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies KADY TECH app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("KADY TECH FHD", appName)
    }

    @Test
    fun `image stats dimensions and size formatting`() {
        val statsKb = ImageStats(width = 1280, height = 720, byteSize = 256 * 1024)
        assertEquals("1280 × 720", statsKb.dimensionsText)
        assertEquals("256 KB", statsKb.formattedSize)

        val statsMb = ImageStats(width = 1920, height = 1080, byteSize = 2 * 1024 * 1024)
        assertTrue(statsMb.formattedSize.contains("MB"))
    }

    @Test
    fun `resolution presets have valid bounding boxes`() {
        val fhd = ResolutionPreset.FHD_1080P
        assertEquals(1920, fhd.maxLongSide)
        assertEquals(1080, fhd.maxShortSide)

        val qhd = ResolutionPreset.QHD_1440P
        assertEquals(2560, qhd.maxLongSide)
        assertEquals(1440, qhd.maxShortSide)
    }

    @Test
    fun `sharpness levels have correct alpha coefficients`() {
        assertEquals(0.0f, SharpnessLevel.NONE.alpha, 0.001f)
        assertEquals(0.35f, SharpnessLevel.CRISP.alpha, 0.001f)
    }

    @Test
    fun `cropImage correctly crops bitmap to specified bounding box`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testBitmap = android.graphics.Bitmap.createBitmap(200, 200, android.graphics.Bitmap.Config.ARGB_8888)
        val file = java.io.File(context.cacheDir, "test_crop_in.png")
        file.outputStream().use { testBitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        val uri = android.net.Uri.fromFile(file)

        // Crop middle 50%: [0.25, 0.25] to [0.75, 0.75] -> 100x100
        val (croppedBitmap, cropUri) = com.example.engine.ImageProcessor.cropImage(
            context = context,
            inputUri = uri,
            normLeft = 0.25f,
            normTop = 0.25f,
            normRight = 0.75f,
            normBottom = 0.75f
        )

        assertEquals(100, croppedBitmap.width)
        assertEquals(100, croppedBitmap.height)
        assertTrue(cropUri.toString().isNotEmpty())
    }

    @Test
    fun `style filters have descriptive titles and subtitles`() {
        val filters = com.example.model.StyleFilter.values()
        assertTrue(filters.isNotEmpty())
        assertTrue(filters.any { it == com.example.model.StyleFilter.GRAYSCALE })
        assertTrue(filters.any { it == com.example.model.StyleFilter.SEPIA })
        assertTrue(filters.any { it == com.example.model.StyleFilter.VIBRANT })

        val sepia = com.example.model.StyleFilter.SEPIA
        assertEquals("سيبيا كلاسيكي", sepia.title)
        assertEquals("Sepia", sepia.subtitle)

        val grayscale = com.example.model.StyleFilter.GRAYSCALE
        assertEquals("أبيض وأسود", grayscale.title)
    }

    @Test
    fun `applyStyleFilter applies color matrices without changing bitmap dimensions`() {
        val testBitmap = android.graphics.Bitmap.createBitmap(150, 100, android.graphics.Bitmap.Config.ARGB_8888)
        
        // Grayscale filter
        val bwBitmap = com.example.engine.ImageProcessor.applyStyleFilter(testBitmap, com.example.model.StyleFilter.GRAYSCALE)
        assertEquals(150, bwBitmap.width)
        assertEquals(100, bwBitmap.height)

        // Sepia filter
        val sepiaBitmap = com.example.engine.ImageProcessor.applyStyleFilter(testBitmap, com.example.model.StyleFilter.SEPIA)
        assertEquals(150, sepiaBitmap.width)
        assertEquals(100, sepiaBitmap.height)

        // Vibrant filter
        val vibrantBitmap = com.example.engine.ImageProcessor.applyStyleFilter(testBitmap, com.example.model.StyleFilter.VIBRANT)
        assertEquals(150, vibrantBitmap.width)
        assertEquals(100, vibrantBitmap.height)
    }
}


