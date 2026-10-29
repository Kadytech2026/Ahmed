package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.ui.theme.KadyBlue
import com.example.ui.theme.KadyCyan
import kotlin.math.roundToInt

enum class CropAspectRatio(val title: String, val ratio: Float?) {
    FREE("حر", null),
    FHD_16_9("16:9 FHD", 16f / 9f),
    PORTRAIT_9_16("9:16 ستوري", 9f / 16f),
    SQUARE_1_1("1:1 مربع", 1f),
    CLASSIC_4_3("4:3 كلاسيكي", 4f / 3f)
}

@Composable
fun ImageCropperDialog(
    imageUri: Uri,
    onCropConfirmed: (normLeft: Float, normTop: Float, normRight: Float, normBottom: Float, rotation: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedRatio by remember { mutableStateOf(CropAspectRatio.FHD_16_9) }
    var rotationDegrees by remember { mutableIntStateOf(0) }

    // Normalized coordinates (0f..1f relative to container box)
    var cropLeft by remember { mutableFloatStateOf(0.1f) }
    var cropTop by remember { mutableFloatStateOf(0.15f) }
    var cropRight by remember { mutableFloatStateOf(0.9f) }
    var cropBottom by remember { mutableFloatStateOf(0.85f) }

    fun adjustRatio(ratioEnum: CropAspectRatio) {
        selectedRatio = ratioEnum
        val targetRatio = ratioEnum.ratio
        if (targetRatio != null) {
            val currentWidth = (cropRight - cropLeft).coerceAtLeast(0.2f)
            val newHeight = (currentWidth / targetRatio).coerceIn(0.1f, 0.9f)
            val centerY = (cropTop + cropBottom) / 2f
            cropTop = (centerY - (newHeight / 2f)).coerceIn(0f, 1f - newHeight)
            cropBottom = cropTop + newHeight
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF070E1A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إلغاء",
                            tint = Color.White
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Crop,
                            contentDescription = null,
                            tint = KadyCyan
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "قص وتحديد منطقة التركيز",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Row {
                        IconButton(onClick = { rotationDegrees = (rotationDegrees + 90) % 360 }) {
                            Icon(
                                imageVector = Icons.Default.RotateRight,
                                contentDescription = "تدوير 90°",
                                tint = Color.White
                            )
                        }

                        IconButton(onClick = {
                            rotationDegrees = 0
                            cropLeft = 0.05f
                            cropTop = 0.05f
                            cropRight = 0.95f
                            cropBottom = 0.95f
                            adjustRatio(CropAspectRatio.FHD_16_9)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "إعادة ضبط",
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Aspect Ratio Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CropAspectRatio.values().forEach { aspect ->
                        FilterChip(
                            selected = selectedRatio == aspect,
                            onClick = { adjustRatio(aspect) },
                            label = {
                                Text(
                                    text = aspect.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KadyBlue,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Crop Area Canvas
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                        .testTag("crop_container_box")
                ) {
                    val boxW = constraints.maxWidth.toFloat()
                    val boxH = constraints.maxHeight.toFloat()
                    val density = LocalDensity.current

                    val leftPx = (cropLeft * boxW).coerceIn(0f, boxW - 50f)
                    val topPx = (cropTop * boxH).coerceIn(0f, boxH - 50f)
                    val rightPx = (cropRight * boxW).coerceIn(leftPx + 50f, boxW)
                    val bottomPx = (cropBottom * boxH).coerceIn(topPx + 50f, boxH)

                    val cropWidthDp = with(density) { (rightPx - leftPx).toDp() }
                    val cropHeightDp = with(density) { (bottomPx - topPx).toDp() }

                    // 1. Base Image with rotation
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "معاينة القص",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(rotationDegrees.toFloat())
                    )

                    // 2. Dimmed Overlay outside crop rect & Rule-of-Thirds grid lines
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cropPath = Path().apply {
                            addRect(Rect(leftPx, topPx, rightPx, bottomPx))
                        }

                        // Dimmed exterior
                        clipPath(cropPath, clipOp = ClipOp.Difference) {
                            drawRect(color = Color.Black.copy(alpha = 0.65f))
                        }

                        // Crop border stroke
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(leftPx, topPx),
                            size = Size(rightPx - leftPx, bottomPx - topPx),
                            style = Stroke(width = 2.dp.toPx())
                        )

                        // 3x3 Grid Lines (Rule of Thirds)
                        val cropW = rightPx - leftPx
                        val cropH = bottomPx - topPx

                        val oneThirdX = leftPx + (cropW / 3f)
                        val twoThirdsX = leftPx + (cropW * 2f / 3f)
                        val oneThirdY = topPx + (cropH / 3f)
                        val twoThirdsY = topPx + (cropH * 2f / 3f)

                        val gridColor = Color.White.copy(alpha = 0.45f)
                        val stroke = Stroke(width = 1.dp.toPx())

                        drawLine(gridColor, Offset(oneThirdX, topPx), Offset(oneThirdX, bottomPx), stroke.width)
                        drawLine(gridColor, Offset(twoThirdsX, topPx), Offset(twoThirdsX, bottomPx), stroke.width)
                        drawLine(gridColor, Offset(leftPx, oneThirdY), Offset(rightPx, oneThirdY), stroke.width)
                        drawLine(gridColor, Offset(leftPx, twoThirdsY), Offset(rightPx, twoThirdsY), stroke.width)
                    }

                    // 3. Move Entire Crop Box (Center Drag)
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(leftPx.roundToInt(), topPx.roundToInt()) }
                            .size(cropWidthDp, cropHeightDp)
                            .pointerInput(boxW, boxH) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val dxNorm = dragAmount.x / boxW
                                    val dyNorm = dragAmount.y / boxH
                                    val currentW = cropRight - cropLeft
                                    val currentH = cropBottom - cropTop

                                    val newLeft = (cropLeft + dxNorm).coerceIn(0f, 1f - currentW)
                                    val newTop = (cropTop + dyNorm).coerceIn(0f, 1f - currentH)
                                    cropLeft = newLeft
                                    cropTop = newTop
                                    cropRight = newLeft + currentW
                                    cropBottom = newTop + currentH
                                }
                            }
                    )

                    // 4. Corner Handles
                    // Top-Left Handle
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(leftPx.roundToInt() - 14, topPx.roundToInt() - 14) }
                            .size(28.dp)
                            .background(KadyCyan, CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                            .pointerInput(boxW, boxH) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val newL = (cropLeft + dragAmount.x / boxW).coerceIn(0f, cropRight - 0.15f)
                                    val newT = (cropTop + dragAmount.y / boxH).coerceIn(0f, cropBottom - 0.15f)
                                    cropLeft = newL
                                    cropTop = newT
                                    selectedRatio.ratio?.let { r ->
                                        cropBottom = (cropTop + (cropRight - cropLeft) / r).coerceIn(cropTop + 0.1f, 1f)
                                    }
                                }
                            }
                    )

                    // Bottom-Right Handle
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(rightPx.roundToInt() - 14, bottomPx.roundToInt() - 14) }
                            .size(28.dp)
                            .background(KadyCyan, CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                            .pointerInput(boxW, boxH) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val newR = (cropRight + dragAmount.x / boxW).coerceIn(cropLeft + 0.15f, 1f)
                                    val newB = (cropBottom + dragAmount.y / boxH).coerceIn(cropTop + 0.15f, 1f)
                                    cropRight = newR
                                    cropBottom = newB
                                    selectedRatio.ratio?.let { r ->
                                        cropBottom = (cropTop + (cropRight - cropLeft) / r).coerceIn(cropTop + 0.1f, 1f)
                                    }
                                }
                            }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "إلغاء", color = Color.White)
                    }

                    Button(
                        onClick = {
                            onCropConfirmed(cropLeft, cropTop, cropRight, cropBottom, rotationDegrees)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp)
                            .testTag("btn_confirm_crop"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KadyBlue)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تطبيق القص وبدء التحسين",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
