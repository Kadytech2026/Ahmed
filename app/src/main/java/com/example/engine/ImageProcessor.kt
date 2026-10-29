package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.media.ExifInterface
import android.net.Uri
import com.example.model.ConversionResult
import com.example.model.ImageStats
import com.example.model.OutputFormat
import com.example.model.ResolutionPreset
import com.example.model.SharpnessLevel
import com.example.model.StyleFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


object ImageProcessor {

    suspend fun cropImage(
        context: Context,
        inputUri: Uri,
        normLeft: Float,
        normTop: Float,
        normRight: Float,
        normBottom: Float,
        rotationDegrees: Int = 0
    ): Pair<Bitmap, Uri> = withContext(Dispatchers.IO) {
        val sourceBitmap = loadSourceBitmap(context, inputUri)
        val rotated = if (rotationDegrees % 360 != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.width, sourceBitmap.height, matrix, true)
        } else {
            sourceBitmap
        }

        val w = rotated.width
        val h = rotated.height

        val l = normLeft.coerceIn(0f, 0.99f)
        val t = normTop.coerceIn(0f, 0.99f)
        val r = normRight.coerceIn(l + 0.01f, 1f)
        val b = normBottom.coerceIn(t + 0.01f, 1f)

        val x = (l * w).roundToInt().coerceIn(0, w - 1)
        val y = (t * h).roundToInt().coerceIn(0, h - 1)
        val cropW = ((r - l) * w).roundToInt().coerceIn(1, w - x)
        val cropH = ((b - t) * h).roundToInt().coerceIn(1, h - y)

        val cropped = Bitmap.createBitmap(rotated, x, y, cropW, cropH)
        val cacheFile = java.io.File(context.cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
        cacheFile.outputStream().use { out ->
            cropped.compress(Bitmap.CompressFormat.JPEG, 98, out)
        }
        val uri = Uri.fromFile(cacheFile)
        Pair(cropped, uri)
    }

    suspend fun decodeImageStats(context: Context, uri: Uri): ImageStats = withContext(Dispatchers.IO) {
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)

            val byteSize = try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use {
                    it.statSize
                } ?: 0L
            } catch (_: Exception) {
                0L
            }

            var w = options.outWidth
            var h = options.outHeight

            // Check EXIF orientation
            try {
                context.contentResolver.openInputStream(uri)?.use { exifStream ->
                    val exif = ExifInterface(exifStream)
                    val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
                        orientation == ExifInterface.ORIENTATION_ROTATE_270
                    ) {
                        val temp = w
                        w = h
                        h = temp
                    }
                }
            } catch (_: Exception) {}

            ImageStats(width = max(1, w), height = max(1, h), byteSize = byteSize)
        } finally {
            inputStream?.close()
        }
    }

    suspend fun loadSourceBitmap(context: Context, uri: Uri, maxDimension: Int = 4096): Bitmap =
        withContext(Dispatchers.IO) {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, boundsOptions)
            }

            var inSampleSize = 1
            var rawW = boundsOptions.outWidth
            var rawH = boundsOptions.outHeight

            while (rawW / inSampleSize > maxDimension || rawH / inSampleSize > maxDimension) {
                inSampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val decoded = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            } ?: throw IllegalStateException("تعذر قراءة الصورة المحددة")

            // Adjust orientation from EXIF
            var rotationDegrees = 0
            try {
                context.contentResolver.openInputStream(uri)?.use { exifStream ->
                    val exif = ExifInterface(exifStream)
                    rotationDegrees = when (exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> 0
                    }
                }
            } catch (_: Exception) {}

            if (rotationDegrees != 0) {
                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
            } else {
                decoded
            }
        }

    fun createColorMatrix(filter: StyleFilter): ColorMatrix? {
        return when (filter) {
            StyleFilter.NONE -> null
            StyleFilter.VIBRANT -> {
                // Saturated colors + slight contrast punch
                val satMatrix = ColorMatrix().apply { setSaturation(1.42f) }
                val scale = 1.10f
                val translate = (-0.5f * scale + 0.5f) * 255f
                val contrastMatrix = ColorMatrix(floatArrayOf(
                    scale, 0f, 0f, 0f, translate,
                    0f, scale, 0f, 0f, translate,
                    0f, 0f, scale, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                ))
                satMatrix.apply { postConcat(contrastMatrix) }
            }
            StyleFilter.SEPIA -> {
                // Classic warm photographic sepia tone
                ColorMatrix(floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f,     0f,     0f,     1f, 0f
                ))
            }
            StyleFilter.GRAYSCALE -> {
                // High contrast cinematic black and white
                val satMatrix = ColorMatrix().apply { setSaturation(0f) }
                val scale = 1.15f
                val translate = (-0.5f * scale + 0.5f) * 255f
                val contrastMatrix = ColorMatrix(floatArrayOf(
                    scale, 0f, 0f, 0f, translate,
                    0f, scale, 0f, 0f, translate,
                    0f, 0f, scale, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                ))
                satMatrix.apply { postConcat(contrastMatrix) }
            }
            StyleFilter.WARM -> {
                // Golden hour warmth
                ColorMatrix(floatArrayOf(
                    1.16f, 0f,    0f,    0f, 12f,
                    0f,    1.06f, 0f,    0f, 6f,
                    0f,    0f,    0.86f, 0f, -8f,
                    0f,    0f,    0f,    1f, 0f
                ))
            }
            StyleFilter.COOL -> {
                // Modern cinematic cool teal/cyan tone
                ColorMatrix(floatArrayOf(
                    0.88f, 0f,    0f,    0f, -6f,
                    0f,    1.06f, 0f,    0f, 6f,
                    0f,    0f,    1.20f, 0f, 16f,
                    0f,    0f,    0f,    1f, 0f
                ))
            }
            StyleFilter.VINTAGE -> {
                // Retro 90s film aesthetic with lifted blacks
                val satMatrix = ColorMatrix().apply { setSaturation(0.85f) }
                val vintageMatrix = ColorMatrix(floatArrayOf(
                    1.05f, 0.02f, 0.02f, 0f, 18f,
                    0.02f, 1.02f, 0.02f, 0f, 12f,
                    0.02f, 0.02f, 0.90f, 0f, 8f,
                    0f,    0f,    0f,    1f, 0f
                ))
                satMatrix.apply { postConcat(vintageMatrix) }
            }
        }
    }

    fun applyStyleFilter(bitmap: Bitmap, filter: StyleFilter): Bitmap {
        val colorMatrix = createColorMatrix(filter) ?: return bitmap
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return output
    }

    fun compressBitmap(bitmap: Bitmap, format: OutputFormat, quality: Int): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val compressFormat = when (format) {
            OutputFormat.JPEG -> Bitmap.CompressFormat.JPEG
            OutputFormat.PNG -> Bitmap.CompressFormat.PNG
            OutputFormat.WEBP -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
            }
        }
        bitmap.compress(compressFormat, quality.coerceIn(50, 100), outputStream)
        return outputStream.toByteArray()
    }

    suspend fun processImage(
        context: Context,
        inputUri: Uri,
        preset: ResolutionPreset,
        sharpnessIntensity: Float = 0.5f,
        smoothingIntensity: Float = 0.15f,
        filter: StyleFilter = StyleFilter.NONE,
        quality: Int = 95,
        format: OutputFormat = OutputFormat.JPEG,
        onProgress: ((Float, String) -> Unit)? = null
    ): ConversionResult = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()

        onProgress?.invoke(0.15f, "جاري فك تشفير وقراءة الصورة الأصلية…")
        val originalStats = decodeImageStats(context, inputUri)
        val sourceBitmap = loadSourceBitmap(context, inputUri)

        val srcW = sourceBitmap.width
        val srcH = sourceBitmap.height

        val (targetW, targetH, _) = calculateTargetDimensions(srcW, srcH, preset)

        // 1. Multi-step progressive resize for ultra-clean scaling
        onProgress?.invoke(0.40f, "جاري تكبير الدقة إلى ${targetW} × ${targetH} بمراحل متعددة…")
        val resizedBitmap = progressiveResize(sourceBitmap, targetW, targetH)

        // 2. Apply gentle noise smoothing filter if requested
        val smoothedBitmap = if (smoothingIntensity > 0.02f) {
            onProgress?.invoke(0.60f, "جاري تنعيم وإزالة تشويش الصورة…")
            applySmoothingFilter(resizedBitmap, smoothingIntensity.coerceIn(0f, 1f))
        } else {
            resizedBitmap
        }

        // 3. Apply sharpness filter based on intensity (0..1 maps to alpha 0..0.70f)
        val alpha = (sharpnessIntensity.coerceIn(0f, 1f) * 0.70f)
        val processedBitmap = if (alpha > 0.01f) {
            onProgress?.invoke(0.75f, "جاري تطبيق فلتر شحذ وإبراز التفاصيل الدقيقة…")
            applySharpenFilter(smoothedBitmap, alpha)
        } else {
            smoothedBitmap
        }

        // 4. Apply Style Filter if selected
        val finalBitmap = if (filter != StyleFilter.NONE) {
            onProgress?.invoke(0.85f, "جاري تطبيق فلتر ${filter.title}…")
            applyStyleFilter(processedBitmap, filter)
        } else {
            processedBitmap
        }

        // 5. Compress to output format
        onProgress?.invoke(0.92f, "جاري تجهيز وضغط الصورة بصيغة ${format.extension.uppercase()}…")
        val outputBytes = compressBitmap(finalBitmap, format, quality)

        onProgress?.invoke(1.0f, "اكتملت المعالجة بنجاح!")

        val convertedStats = ImageStats(
            width = finalBitmap.width,
            height = finalBitmap.height,
            byteSize = outputBytes.size.toLong()
        )

        val processingTimeMs = System.currentTimeMillis() - startTime

        ConversionResult(
            originalStats = originalStats,
            convertedStats = convertedStats,
            processingTimeMs = processingTimeMs,
            convertedBitmap = finalBitmap,
            baseEnhancedBitmap = processedBitmap,
            outputBytes = outputBytes,
            appliedFilter = filter
        )
    }

    suspend fun processImage(
        context: Context,
        inputUri: Uri,
        preset: ResolutionPreset,
        sharpness: SharpnessLevel,
        quality: Int = 95,
        format: OutputFormat = OutputFormat.JPEG
    ): ConversionResult = processImage(
        context = context,
        inputUri = inputUri,
        preset = preset,
        sharpnessIntensity = (sharpness.alpha / 0.70f).coerceIn(0f, 1f),
        smoothingIntensity = 0.15f,
        filter = StyleFilter.NONE,
        quality = quality,
        format = format
    )


    private fun calculateTargetDimensions(
        w: Int,
        h: Int,
        preset: ResolutionPreset
    ): Triple<Int, Int, Boolean> {
        if (preset == ResolutionPreset.UPSCALE_2X) {
            return Triple(w * 2, h * 2, true)
        }

        val isLandscape = w >= h
        val boxW = if (isLandscape) preset.maxLongSide else preset.maxShortSide
        val boxH = if (isLandscape) preset.maxShortSide else preset.maxLongSide

        val scale = min(boxW.toFloat() / w, boxH.toFloat() / h)
        val targetW = max(1, (w * scale).roundToInt())
        val targetH = max(1, (h * scale).roundToInt())

        return Triple(targetW, targetH, scale > 1.0f)
    }

    private fun progressiveResize(source: Bitmap, targetW: Int, targetH: Int): Bitmap {
        var currentW = source.width
        var currentH = source.height
        var currentBitmap = source

        val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG).apply {
            isAntiAlias = true
        }

        while (currentW != targetW || currentH != targetH) {
            val nextW = if (targetW > currentW) {
                min(targetW, currentW * 2)
            } else {
                max(targetW, ceil(currentW / 2.0).toInt())
            }

            val nextH = if (targetH > currentH) {
                min(targetH, currentH * 2)
            } else {
                max(targetH, ceil(currentH / 2.0).toInt())
            }

            val stepBitmap = Bitmap.createBitmap(nextW, nextH, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(stepBitmap)
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(
                currentBitmap,
                android.graphics.Rect(0, 0, currentBitmap.width, currentBitmap.height),
                android.graphics.Rect(0, 0, nextW, nextH),
                paint
            )

            if (currentBitmap != source) {
                currentBitmap.recycle()
            }

            currentBitmap = stepBitmap
            currentW = nextW
            currentH = nextH
        }

        return currentBitmap
    }

    /**
     * Denoising/Smoothing low-pass kernel:
     * Suppresses compression grain and pixel artifacts before sharpening.
     */
    private fun applySmoothingFilter(bitmap: Bitmap, intensity: Float): Bitmap {
        val w = bitmap.width
        val h = bitmap.height

        val srcPixels = IntArray(w * h)
        bitmap.getPixels(srcPixels, 0, w, 0, 0, w, h)

        val outPixels = IntArray(w * h)
        System.arraycopy(srcPixels, 0, outPixels, 0, srcPixels.size)

        // Blend factor for neighbors: max 0.10f each (total 0.40f for 4 neighbors)
        val neighborWeight = 0.10f * intensity
        val centerWeight = 1.0f - (4.0f * neighborWeight)

        for (y in 1 until h - 1) {
            val rowOffset = y * w
            val upOffset = (y - 1) * w
            val downOffset = (y + 1) * w

            for (x in 1 until w - 1) {
                val center = srcPixels[rowOffset + x]
                val left = srcPixels[rowOffset + (x - 1)]
                val right = srcPixels[rowOffset + (x + 1)]
                val up = srcPixels[upOffset + x]
                val down = srcPixels[downOffset + x]

                val a = (center ushr 24) and 0xFF

                val cr = (center ushr 16) and 0xFF
                val cg = (center ushr 8) and 0xFF
                val cb = center and 0xFF

                val lr = (left ushr 16) and 0xFF
                val lg = (left ushr 8) and 0xFF
                val lb = left and 0xFF

                val rr = (right ushr 16) and 0xFF
                val rg = (right ushr 8) and 0xFF
                val rb = right and 0xFF

                val ur = (up ushr 16) and 0xFF
                val ug = (up ushr 8) and 0xFF
                val ub = up and 0xFF

                val dr = (down ushr 16) and 0xFF
                val dg = (down ushr 8) and 0xFF
                val db = down and 0xFF

                val newR = (centerWeight * cr + neighborWeight * (lr + rr + ur + dr)).roundToInt().coerceIn(0, 255)
                val newG = (centerWeight * cg + neighborWeight * (lg + rg + ug + dg)).roundToInt().coerceIn(0, 255)
                val newB = (centerWeight * cb + neighborWeight * (lb + rb + ub + db)).roundToInt().coerceIn(0, 255)

                outPixels[rowOffset + x] = (a shl 24) or (newR shl 16) or (newG shl 8) or newB
            }
        }

        val resultBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        resultBitmap.setPixels(outPixels, 0, w, 0, 0, w, h)
        return resultBitmap
    }

    /**
     * Unsharp masking kernel:
     * k = 1 + 4 * alpha
     * Center pixel = k * Center - alpha * (Up + Down + Left + Right)
     */
    private fun applySharpenFilter(bitmap: Bitmap, alpha: Float): Bitmap {
        val w = bitmap.width
        val h = bitmap.height

        val srcPixels = IntArray(w * h)
        bitmap.getPixels(srcPixels, 0, w, 0, 0, w, h)

        val outPixels = IntArray(w * h)
        System.arraycopy(srcPixels, 0, outPixels, 0, srcPixels.size)

        val k = 1.0f + 4.0f * alpha

        for (y in 1 until h - 1) {
            val rowOffset = y * w
            val upOffset = (y - 1) * w
            val downOffset = (y + 1) * w

            for (x in 1 until w - 1) {
                val center = srcPixels[rowOffset + x]
                val left = srcPixels[rowOffset + (x - 1)]
                val right = srcPixels[rowOffset + (x + 1)]
                val up = srcPixels[upOffset + x]
                val down = srcPixels[downOffset + x]

                val a = (center ushr 24) and 0xFF

                val cr = (center ushr 16) and 0xFF
                val cg = (center ushr 8) and 0xFF
                val cb = center and 0xFF

                val lr = (left ushr 16) and 0xFF
                val lg = (left ushr 8) and 0xFF
                val lb = left and 0xFF

                val rr = (right ushr 16) and 0xFF
                val rg = (right ushr 8) and 0xFF
                val rb = right and 0xFF

                val ur = (up ushr 16) and 0xFF
                val ug = (up ushr 8) and 0xFF
                val ub = up and 0xFF

                val dr = (down ushr 16) and 0xFF
                val dg = (down ushr 8) and 0xFF
                val db = down and 0xFF

                val newR = (k * cr - alpha * (lr + rr + ur + dr)).roundToInt().coerceIn(0, 255)
                val newG = (k * cg - alpha * (lg + rg + ug + dg)).roundToInt().coerceIn(0, 255)
                val newB = (k * cb - alpha * (lb + rb + ub + db)).roundToInt().coerceIn(0, 255)

                outPixels[rowOffset + x] = (a shl 24) or (newR shl 16) or (newG shl 8) or newB
            }
        }

        val resultBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        resultBitmap.setPixels(outPixels, 0, w, 0, 0, w, h)
        return resultBitmap
    }
}
