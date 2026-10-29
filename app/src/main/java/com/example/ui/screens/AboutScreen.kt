package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.KadyBlue
import com.example.ui.theme.KadyCyan
import com.example.ui.theme.KadySuccess

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Emblem Image
        Surface(
            shape = CircleShape,
            shadowElevation = 8.dp,
            modifier = Modifier.size(130.dp)
        ) {
            AsyncImage(
                model = R.drawable.kady_logo,
                contentDescription = "شعار كادي تك",
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "KADY",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = KadyBlue
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "TECH",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = KadyCyan
            )
        }

        Text(
            text = "TECHNOLOGY SOLUTIONS",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            color = KadyBlue.copy(alpha = 0.12f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "الإصدار 1.0 (Full HD Edition)",
                style = MaterialTheme.typography.labelMedium,
                color = KadyBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "تطبيق كادي تك لتحويل الصور إلى دقة فائقة (Full HD 1080p / 2K / 4K) يتيح لك تحسين جودة وتفاصيل صورك باحترافية وسرعة فائقة مع فلتر الشحذ المعزز للتفاصيل الدقيقة.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Feature cards
        AboutFeatureCard(
            icon = Icons.Default.Security,
            iconTint = KadySuccess,
            title = "خصوصية مطلقة (100% Offline)",
            description = "معالجة الصور تتم بالكامل داخل جهازك عبر كود Kotlin الأصلي بدون الحاجة لإنترنت أو رفع الصور إلى خوادم خارجية."
        )

        Spacer(modifier = Modifier.height(12.dp))

        AboutFeatureCard(
            icon = Icons.Default.Hd,
            iconTint = KadyBlue,
            title = "تحويل عالي الجودة إلى Full HD",
            description = "خوارزمية تكبير متعددة المراحل تحافظ على النسب وتمنع التشويه، مع دعم الدقات القياسية 1080p و 1440p و 2160p."
        )

        Spacer(modifier = Modifier.height(12.dp))

        AboutFeatureCard(
            icon = Icons.Default.AutoAwesome,
            iconTint = KadyCyan,
            title = "فلتر الشحذ وإبراز التفاصيل (Unsharp Mask)",
            description = "مصفوفة معالجة رياضية 3×3 لإبراز الحواف واسترجاع التفاصيل المفقودة أثناء التكبير."
        )

        Spacer(modifier = Modifier.height(12.dp))

        AboutFeatureCard(
            icon = Icons.Default.PhotoAlbum,
            iconTint = Color(0xFFF59E0B),
            title = "حفظ منظم في المعرض",
            description = "حفظ الصور المنتهية مباشرة في مجلد مخصص باسم Pictures/KADY TECH يسهل الوصول إليها ومشاركتها."
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "© جميع الحقوق محفوظة لشركة KADY TECH",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun AboutFeatureCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(iconTint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
