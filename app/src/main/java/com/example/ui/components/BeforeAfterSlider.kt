package com.example.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.ImageStats
import com.example.ui.theme.KadyBlue
import com.example.ui.theme.KadyCyan
import kotlin.math.roundToInt

enum class ComparisonViewMode(val title: String, val icon: ImageVector) {
    SLIDER("سلايدر مسح", Icons.Default.CompareArrows),
    SIDE_BY_SIDE("جنباً إلى جنب", Icons.Default.ViewColumn),
    HOLD_TO_COMPARE("اضغط للمقارنة", Icons.Default.TouchApp)
}

/**
 * Comprehensive Before/After Split Comparison Component.
 * Supports:
 * 1. Interactive Wipe Slider (سلايدر مسح تفاعلي)
 * 2. Side-by-Side Dual View (عرض جنباً إلى جنب)
 * 3. Hold-to-Compare Peek (ضغط مطول للمعاينة الفورية)
 * 4. Fullscreen Zoom & Pan Modal
 */
@Composable
fun BeforeAfterSlider(
    originalUri: Uri?,
    convertedBitmap: Bitmap?,
    splitPosition: Float,
    onSplitPositionChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    originalStats: ImageStats? = null,
    convertedStats: ImageStats? = null
) {
    var comparisonMode by remember { mutableStateOf(ComparisonViewMode.SLIDER) }
    var isHoldingOriginal by remember { mutableStateOf(false) }
    var showFullscreenDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Controls Row: View Mode Selector & Fullscreen Button
        if (convertedBitmap != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    ComparisonViewMode.values().forEach { mode ->
                        FilterChip(
                            selected = comparisonMode == mode,
                            onClick = { comparisonMode = mode },
                            label = {
                                Text(
                                    text = mode.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = mode.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KadyBlue,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("chip_mode_${mode.name}")
                        )
                    }
                }

                IconButton(
                    onClick = { showFullscreenDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("btn_fullscreen_compare")
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "تكبير ملء الشاشة",
                        tint = KadyBlue
                    )
                }
            }
        }

        // Main Display Area depending on Comparison Mode
        Crossfade(targetState = comparisonMode, label = "mode_crossfade") { mode ->
            when {
                convertedBitmap == null -> {
                    // Single Original Image View
                    OriginalOnlyCard(originalUri = originalUri)
                }
                mode == ComparisonViewMode.SLIDER -> {
                    InteractiveWipeSlider(
                        originalUri = originalUri,
                        convertedBitmap = convertedBitmap,
                        splitPosition = splitPosition,
                        onSplitPositionChanged = onSplitPositionChanged,
                        heightDp = 300
                    )
                }
                mode == ComparisonViewMode.SIDE_BY_SIDE -> {
                    SideBySideComparisonCard(
                        originalUri = originalUri,
                        convertedBitmap = convertedBitmap,
                        originalStats = originalStats,
                        convertedStats = convertedStats
                    )
                }
                mode == ComparisonViewMode.HOLD_TO_COMPARE -> {
                    HoldToCompareCard(
                        originalUri = originalUri,
                        convertedBitmap = convertedBitmap,
                        isHolding = isHoldingOriginal,
                        onHoldingChanged = { isHoldingOriginal = it }
                    )
                }
            }
        }

        // Helper explanation hint under comparison
        if (convertedBitmap != null) {
            val hintText = when (comparisonMode) {
                ComparisonViewMode.SLIDER -> "💡 اسحب المقبض في المنتصف يميناً ويساراً للمقارنة المباشرة"
                ComparisonViewMode.SIDE_BY_SIDE -> "💡 مقارنة متوازية بين تفاصيل الصورة الأصلية والمحسّنة"
                ComparisonViewMode.HOLD_TO_COMPARE -> "👆 المس الشاشة مع الاستمرار لرؤية الصورة الأصلية، واتركها لرؤية FHD"
            }
            Text(
                text = hintText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }

    // Fullscreen Dialog
    if (showFullscreenDialog && convertedBitmap != null) {
        FullscreenComparisonDialog(
            originalUri = originalUri,
            convertedBitmap = convertedBitmap,
            splitPosition = splitPosition,
            onSplitPositionChanged = onSplitPositionChanged,
            onDismiss = { showFullscreenDialog = false }
        )
    }
}

@Composable
fun OriginalOnlyCard(originalUri: Uri?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AsyncImage(
            model = originalUri,
            contentDescription = "الصورة الأصلية",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
        Surface(
            color = Color.Black.copy(alpha = 0.65f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Text(
                text = "الأصلية",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun InteractiveWipeSlider(
    originalUri: Uri?,
    convertedBitmap: Bitmap?,
    splitPosition: Float,
    onSplitPositionChanged: (Float) -> Unit,
    heightDp: Int = 300,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .testTag("interactive_wipe_slider")
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val splitX = (widthPx * splitPosition).coerceIn(0f, widthPx)

        // 1. Background layer: Converted FHD Image
        AsyncImage(
            model = convertedBitmap,
            contentDescription = "الصورة بعد التحويل",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Foreground layer clipped to splitX: Original Image
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(object : androidx.compose.ui.graphics.Shape {
                    override fun createOutline(
                        size: androidx.compose.ui.geometry.Size,
                        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
                        density: androidx.compose.ui.unit.Density
                    ): androidx.compose.ui.graphics.Outline {
                        return androidx.compose.ui.graphics.Outline.Rectangle(
                            Rect(0f, 0f, splitX, size.height)
                        )
                    }
                })
        ) {
            AsyncImage(
                model = originalUri,
                contentDescription = "الصورة الأصلية",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 3. Vertical Divider Line with glowing shadow
        Box(
            modifier = Modifier
                .offset { IntOffset(splitX.roundToInt() - 1, 0) }
                .width(2.dp)
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.95f))
        )

        // 4. Center Drag Handle
        val knobY = (heightPx / 2f) - 22f
        Box(
            modifier = Modifier
                .offset { IntOffset(splitX.roundToInt() - 22, knobY.roundToInt()) }
                .size(44.dp)
                .shadow(8.dp, CircleShape)
                .background(
                    Brush.linearGradient(listOf(KadyBlue, KadyCyan)),
                    CircleShape
                )
                .border(2.dp, Color.White, CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newPos = (splitX + dragAmount.x) / widthPx
                        onSplitPositionChanged(newPos.coerceIn(0.02f, 0.98f))
                    }
                }
                .testTag("slider_drag_knob"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CompareArrows,
                contentDescription = "مقبض المقارنة",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // 5. Left Badge: Original
        Surface(
            color = Color.Black.copy(alpha = 0.7f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
        ) {
            Text(
                text = "الأصلية",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // 6. Right Badge: FHD Enhanced
        Surface(
            color = KadyBlue.copy(alpha = 0.92f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
        ) {
            Text(
                text = "FHD المحسّنة ✨",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun SideBySideComparisonCard(
    originalUri: Uri?,
    convertedBitmap: Bitmap?,
    originalStats: ImageStats?,
    convertedStats: ImageStats?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Original Panel
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = originalUri,
                    contentDescription = "الصورة الأصلية",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 14.dp, bottomEnd = 14.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "الأصلية",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (originalStats != null) {
                            Text(
                                text = "${originalStats.width}×${originalStats.height}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Converted Panel
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .border(BorderStroke(1.5.dp, KadyBlue), RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = convertedBitmap,
                    contentDescription = "الصورة بعد التحويل",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    color = KadyBlue.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 14.dp, bottomEnd = 14.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "FHD المحسّنة",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (convertedStats != null) {
                            Text(
                                text = "${convertedStats.width}×${convertedStats.height}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HoldToCompareCard(
    originalUri: Uri?,
    convertedBitmap: Bitmap?,
    isHolding: Boolean,
    onHoldingChanged: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onHoldingChanged(true)
                        tryAwaitRelease()
                        onHoldingChanged(false)
                    }
                )
            }
            .testTag("hold_to_compare_area"),
        contentAlignment = Alignment.Center
    ) {
        // Display Converted unless holding
        if (isHolding) {
            AsyncImage(
                model = originalUri,
                contentDescription = "معاينة الأصلية",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AsyncImage(
                model = convertedBitmap,
                contentDescription = "FHD المحسّنة",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Live status indicator badge
        val badgeColor by animateColorAsState(
            targetValue = if (isHolding) Color.Black.copy(alpha = 0.8f) else KadyBlue.copy(alpha = 0.9f),
            label = "badge_color"
        )
        Surface(
            color = badgeColor,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
        ) {
            Text(
                text = if (isHolding) "👁️ المعروض الآن: الصورة الأصلية" else "✨ المعروض الآن: دقة FHD المحسّنة (المس مع الاستمرار للأصلية)",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun FullscreenComparisonDialog(
    originalUri: Uri?,
    convertedBitmap: Bitmap?,
    splitPosition: Float,
    onSplitPositionChanged: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top controls bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .size(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color.White
                        )
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "المقارنة بملء الشاشة (يمكنك التكبير باللمس)",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            scale = 1f
                            offset = Offset.Zero
                        },
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .size(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "إعادة ضبط التكبير",
                            tint = Color.White
                        )
                    }
                }

                // Centered Interactive Wipe Slider with pinch-to-zoom
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 70.dp)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 4f)
                                if (scale > 1f) {
                                    offset = Offset(offset.x + pan.x, offset.y + pan.y)
                                } else {
                                    offset = Offset.Zero
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    InteractiveWipeSlider(
                        originalUri = originalUri,
                        convertedBitmap = convertedBitmap,
                        splitPosition = splitPosition,
                        onSplitPositionChanged = onSplitPositionChanged,
                        heightDp = 480
                    )
                }
            }
        }
    }
}
