package com.example.model

import android.graphics.Bitmap
import android.net.Uri

enum class ResolutionPreset(
    val title: String,
    val subtitle: String,
    val maxLongSide: Int,
    val maxShortSide: Int
) {
    FHD_1080P("Full HD 1080p", "1920 × 1080", 1920, 1080),
    QHD_1440P("2K QHD 1440p", "2560 × 1440", 2560, 1440),
    UHD_2160P("4K UHD 2160p", "3840 × 2160", 3840, 2160),
    UPSCALE_2X("مضاعفة 2X", "تكبير الدقة الأصلية ×2", -1, -1)
}

enum class SharpnessLevel(
    val title: String,
    val alpha: Float
) {
    NONE("بدون شحذ", 0.0f),
    NATURAL("طبيعي", 0.20f),
    CRISP("فائق الوضوح (FHD)", 0.35f),
    ULTRA("شديد الحدة", 0.50f)
}

enum class OutputFormat(val extension: String, val mimeType: String) {
    JPEG("jpg", "image/jpeg"),
    PNG("png", "image/png"),
    WEBP("webp", "image/webp")
}

enum class StyleFilter(
    val title: String,
    val subtitle: String,
    val description: String,
    val iconName: String
) {
    NONE("طبيعي", "الأصل", "الألوان الطبيعية الأصلية بدون أي فلتر", "original"),
    VIBRANT("حيوية فائقة", "Vibrant", "تعزيز حيوية الألوان والتباين لإبراز التفاصيل المشرقة", "vibrant"),
    SEPIA("سيبيا كلاسيكي", "Sepia", "دفء كلاسيكي عتيق بنغمات بنية سينمائية جذابة", "sepia"),
    GRAYSCALE("أبيض وأسود", "B&W Mono", "تدرج رمادي سينمائي عالي التباين بأسلوب فوتوغرافي فني", "mono"),
    WARM("غروب دافئ", "Warm", "نغمات ذهبية ناعمة تحاكي إضاءة الساعة الذهبية الدافئة", "warm"),
    COOL("سينمائي بارد", "Cool Teal", "ألوان فيروزية وزرقاء تمنح الصورة طابعاً سينمائياً عصرياً", "cool"),
    VINTAGE("ريترو قديم", "Vintage", "طابع كلاسيكي عتيق مع تدرجات لونية ناعمة وظلال خفيفة", "vintage")
}

data class ImageStats(
    val width: Int,
    val height: Int,
    val byteSize: Long
) {
    val dimensionsText: String get() = "${width} × ${height}"
    val formattedSize: String
        get() {
            if (byteSize <= 0) return ""
            return if (byteSize > 1048576) {
                String.format(java.util.Locale.US, "%.1f MB", byteSize / 1048576f)
            } else {
                "${byteSize / 1024} KB"
            }
        }
}

data class ConversionResult(
    val originalStats: ImageStats,
    val convertedStats: ImageStats,
    val processingTimeMs: Long,
    val convertedBitmap: Bitmap,
    val baseEnhancedBitmap: Bitmap? = null,
    val outputBytes: ByteArray,
    val appliedFilter: StyleFilter = StyleFilter.NONE
)

data class SavedImageItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val width: Int,
    val height: Int,
    val sizeBytes: Long,
    val dateAdded: Long
)

