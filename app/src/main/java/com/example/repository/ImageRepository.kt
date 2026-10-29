package com.example.repository

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.model.OutputFormat
import com.example.model.SavedImageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class ImageRepository(private val context: Context) {

    /**
     * Saves processed image byte array to the device's gallery using standard MediaStore API.
     * Saved under Pictures/KADY TECH directory.
     */
    suspend fun saveImageToGallery(
        bytes: ByteArray,
        baseName: String,
        format: OutputFormat,
        width: Int = 0,
        height: Int = 0
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val sanitizedName = baseName.ifBlank { "photo" }
                .replace(Regex("[^a-zA-Z0-9_\\-\\u0600-\\u06FF]"), "_")
            val fileName = "${sanitizedName}-fhd-${System.currentTimeMillis()}.${format.extension}"
            val currentTimeSeconds = System.currentTimeMillis() / 1000
            val currentTimeMs = System.currentTimeMillis()

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.TITLE, fileName)
                put(MediaStore.Images.Media.DESCRIPTION, "Processed with KADY TECH FHD")
                put(MediaStore.Images.Media.MIME_TYPE, format.mimeType)
                put(MediaStore.Images.Media.DATE_ADDED, currentTimeSeconds)
                put(MediaStore.Images.Media.DATE_TAKEN, currentTimeMs)
                if (width > 0) put(MediaStore.Images.Media.WIDTH, width)
                if (height > 0) put(MediaStore.Images.Media.HEIGHT, height)
                put(MediaStore.Images.Media.SIZE, bytes.size)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/KADY TECH"
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val itemUri = context.contentResolver.insert(collection, values)
                ?: return@withContext Result.failure(Exception("تعذر إنشاء ملف في المعرض عبر MediaStore"))

            context.contentResolver.openOutputStream(itemUri)?.use { outputStream: OutputStream ->
                outputStream.write(bytes)
                outputStream.flush()
            } ?: return@withContext Result.failure(Exception("تعذر كتابة بيانات الصورة"))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(itemUri, values, null, null)
            }

            // Notify gallery scanner
            try {
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(itemUri.toString()),
                    arrayOf(format.mimeType),
                    null
                )
            } catch (_: Exception) {}

            Result.success(itemUri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Saves processed Bitmap directly to device gallery using MediaStore API.
     */
    suspend fun saveBitmapToGallery(
        bitmap: Bitmap,
        baseName: String,
        format: OutputFormat = OutputFormat.JPEG,
        quality: Int = 95
    ): Result<Uri> = withContext(Dispatchers.IO) {
        val stream = ByteArrayOutputStream()
        val compressFormat = when (format) {
            OutputFormat.JPEG -> Bitmap.CompressFormat.JPEG
            OutputFormat.PNG -> Bitmap.CompressFormat.PNG
            OutputFormat.WEBP -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
            }
        }
        bitmap.compress(compressFormat, quality.coerceIn(50, 100), stream)
        val bytes = stream.toByteArray()
        saveImageToGallery(
            bytes = bytes,
            baseName = baseName,
            format = format,
            width = bitmap.width,
            height = bitmap.height
        )
    }

    suspend fun getSavedKadyImages(): List<SavedImageItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<SavedImageItem>()
        try {
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_ADDED
            )

            val selection: String
            val selectionArgs: Array<String>

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
                selectionArgs = arrayOf("%KADY TECH%")
            } else {
                selection = "${MediaStore.Images.Media.DISPLAY_NAME} LIKE ?"
                selectionArgs = arrayOf("%-fhd%")
            }

            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn) ?: "photo.jpg"
                    val width = cursor.getInt(widthColumn)
                    val height = cursor.getInt(heightColumn)
                    val size = cursor.getLong(sizeColumn)
                    val dateAdded = cursor.getLong(dateColumn)

                    val uri = ContentUris.withAppendedId(collection, id)
                    list.add(
                        SavedImageItem(
                            id = id,
                            uri = uri,
                            displayName = name,
                            width = width,
                            height = height,
                            sizeBytes = size,
                            dateAdded = dateAdded
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        list
    }

    fun shareImage(imageUri: Uri, mimeType: String = "image/jpeg") {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "مشاركة الصورة عبر").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun viewImageInGallery(imageUri: Uri) {
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(imageUri, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(viewIntent)
        } catch (_: Exception) {}
    }

    suspend fun deleteImage(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.delete(uri, null, null) > 0
        } catch (_: Exception) {
            false
        }
    }
}
