package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.ResolutionPreset
import com.example.model.SharpnessLevel
import com.example.model.StyleFilter
import com.example.ui.components.BeforeAfterSlider
import com.example.ui.components.ImageCropperDialog
import com.example.ui.components.ImagePickerComponent
import com.example.ui.components.StyleFilterSelector
import com.example.ui.theme.KadyBlue
import com.example.ui.theme.KadyCyan
import com.example.ui.theme.KadySuccess
import com.example.viewmodel.ConverterUiState

@Composable
fun ConverterScreen(
    state: ConverterUiState,
    onImageSelected: (Uri?) -> Unit,
    onSelectSampleImage: (Int, String) -> Unit,
    onApplyCrop: (normLeft: Float, normTop: Float, normRight: Float, normBottom: Float, rotation: Int) -> Unit,
    onPresetChanged: (ResolutionPreset) -> Unit,
    onFilterChanged: (StyleFilter) -> Unit,
    onSharpnessChanged: (SharpnessLevel) -> Unit,
    onSharpnessIntensityChanged: (Int) -> Unit,
    onSmoothingIntensityChanged: (Int) -> Unit,
    onQualityChanged: (Int) -> Unit,
    onSplitPositionChanged: (Float) -> Unit,
    onConvertClicked: () -> Unit,
    onSaveClicked: () -> Unit,
    onShareClicked: () -> Unit,
    onResetClicked: () -> Unit,
    modifier: Modifier = Modifier
) {

    var showAdvancedOptions by remember { mutableStateOf(true) }
    var showCropperDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App / Brand Header
        BrandHeader()

        Spacer(modifier = Modifier.height(16.dp))

        if (state.selectedUri == null) {
            // Dedicated Image Picker Component (Photo Picker + File Picker + Samples)
            ImagePickerComponent(
                selectedUri = null,
                stats = null,
                onImageSelected = onImageSelected,
                onSelectSampleImage = onSelectSampleImage,
                onClearSelection = onResetClicked
            )
        } else {
            // Active conversion card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Image Picker bar for changing photo & cropping
                    ImagePickerComponent(
                        selectedUri = state.selectedUri,
                        stats = state.originalStats,
                        onImageSelected = onImageSelected,
                        onSelectSampleImage = onSelectSampleImage,
                        onClearSelection = onResetClicked,
                        onOpenCropper = { showCropperDialog = true }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Before & After Interactive Comparison (Slider, Side-by-Side, Hold-to-Peek)
                    BeforeAfterSlider(
                        originalUri = state.selectedUri,
                        convertedBitmap = state.conversionResult?.convertedBitmap,
                        splitPosition = state.splitPosition,
                        onSplitPositionChanged = onSplitPositionChanged,
                        originalStats = state.originalStats,
                        convertedStats = state.conversionResult?.convertedStats
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stats Information Row
                    ComparisonStatsSection(state = state)

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                    Spacer(modifier = Modifier.height(14.dp))

                    // Target Resolution Preset
                    Text(
                        text = "الدقة المستهدفة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ResolutionPreset.values().forEach { preset ->
                            FilterChip(
                                selected = state.selectedPreset == preset,
                                onClick = { onPresetChanged(preset) },
                                label = {
                                    Text(
                                        text = preset.title,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                leadingIcon = if (state.selectedPreset == preset) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = KadyBlue,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Style Filters (Grayscale, Sepia, Vibrant, Warm, Cool, Vintage, Original)
                    StyleFilterSelector(
                        selectedFilter = state.selectedFilter,
                        onFilterSelected = onFilterChanged,
                        isConverted = state.conversionResult != null
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Advanced Options Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showAdvancedOptions = !showAdvancedOptions }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = KadyBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "خيارات الشحذ والجودة المتقدمة",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = if (showAdvancedOptions) "إخفاء ▲" else "تعديل ▼",
                            style = MaterialTheme.typography.labelSmall,
                            color = KadyBlue
                        )
                    }

                    AnimatedVisibility(visible = showAdvancedOptions) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            // 1. Slider: Detail Sharpness & Edge Definition
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "شدة حدة التفاصيل والشحذ (Sharpness):",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = KadyBlue.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${state.sharpnessIntensity}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = KadyBlue,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Dynamic label description for sharpness
                            val sharpnessDescription = when {
                                state.sharpnessIntensity < 15 -> "تنعيم كامل (بدون شحذ)"
                                state.sharpnessIntensity < 35 -> "حدة طبيعية خفيفة"
                                state.sharpnessIntensity < 65 -> "دقة FHD الموصى بها ⭐"
                                state.sharpnessIntensity < 85 -> "تفاصيل حادة وقوية"
                                else -> "أقصى شحذ سينمائي فائق"
                            }
                            Text(
                                text = sharpnessDescription,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Slider(
                                value = state.sharpnessIntensity.toFloat(),
                                onValueChange = { onSharpnessIntensityChanged(it.toInt()) },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = KadyBlue,
                                    activeTrackColor = KadyBlue
                                ),
                                modifier = Modifier.testTag("slider_sharpness_intensity")
                            )

                            // Quick preset chips for sharpness
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    20 to "ناعم (20%)",
                                    50 to "FHD متوازن (50%)",
                                    80 to "شديد الحدة (80%)"
                                ).forEach { (value, label) ->
                                    FilterChip(
                                        selected = state.sharpnessIntensity == value,
                                        onClick = { onSharpnessIntensityChanged(value) },
                                        label = {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = KadyCyan,
                                            selectedLabelColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 2. Slider: Smoothing & Denoising Strength
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "قوة التنعيم وإزالة التشويش (Smoothing):",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = KadyCyan.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${state.smoothingIntensity}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            val smoothingDescription = when {
                                state.smoothingIntensity == 0 -> "معطل (بدون تنعيم)"
                                state.smoothingIntensity <= 25 -> "تنعيم خفيف موصى به للصور الرقمية"
                                state.smoothingIntensity <= 60 -> "تنعيم متوسط لمعالجة الصور المضغوطة"
                                else -> "تنعيم قوي للصور ذات الحبوب والضوضاء العالية"
                            }
                            Text(
                                text = smoothingDescription,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Slider(
                                value = state.smoothingIntensity.toFloat(),
                                onValueChange = { onSmoothingIntensityChanged(it.toInt()) },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = KadyCyan,
                                    activeTrackColor = KadyCyan
                                ),
                                modifier = Modifier.testTag("slider_smoothing_intensity")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 3. Slider: Compression Quality
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "جودة ضغط الإخراج:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${state.quality}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = KadyBlue
                                )
                            }
                            Slider(
                                value = state.quality.toFloat(),
                                onValueChange = { onQualityChanged(it.toInt()) },
                                valueRange = 60f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = KadyBlue,
                                    activeTrackColor = KadyBlue
                                ),
                                modifier = Modifier.testTag("slider_quality")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Enhanced Processing Status / Progress Indicator
                    if (state.isProcessing) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                                .testTag("processing_indicator_card"),
                            color = KadyBlue.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, KadyBlue.copy(alpha = 0.35f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 3.dp,
                                            color = KadyBlue,
                                            trackColor = KadyBlue.copy(alpha = 0.2f)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "جاري تحسين الصورة بدقة فائقة…",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = KadyBlue
                                        )
                                    }

                                    Surface(
                                        color = KadyBlue,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        val percentInt = (state.processingProgress.coerceIn(0f, 1f) * 100).toInt()
                                        Text(
                                            text = "$percentInt%",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                LinearProgressIndicator(
                                    progress = { state.processingProgress.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .testTag("linear_progress_bar"),
                                    color = KadyBlue,
                                    trackColor = KadyBlue.copy(alpha = 0.15f)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = state.processingMessage.ifBlank { "جاري معالجة بيكسلات الصورة وإعادة الاستعيان…" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // Action buttons
                        if (state.conversionResult == null) {
                            Button(
                                onClick = onConvertClicked,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("convert_fhd_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = KadyBlue
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "تحويل إلى ${state.selectedPreset.title} الآن",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        } else {
                            // Converted Action Buttons
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onSaveClicked,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("save_gallery_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (state.lastSavedUri != null) KadySuccess else KadyBlue
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (state.lastSavedUri != null) Icons.Default.CheckCircle else Icons.Default.Save,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (state.lastSavedUri != null) "تم الحفظ في المعرض بنجاح ✓" else "حفظ في المعرض (Pictures/KADY TECH)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = onShareClicked,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("share_image_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = null,
                                            tint = KadyBlue
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "مشاركة", color = KadyBlue)
                                    }

                                    OutlinedButton(
                                        onClick = onResetClicked,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("new_photo_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "صورة جديدة")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Privacy & Feature Guarantee Box
        PrivacyFooterCard()
    }

    if (showCropperDialog && state.selectedUri != null) {
        ImageCropperDialog(
            imageUri = state.selectedUri,
            onCropConfirmed = { l, t, r, b, rot ->
                onApplyCrop(l, t, r, b, rot)
                showCropperDialog = false
            },
            onDismiss = { showCropperDialog = false }
        )
    }
}

@Composable
fun BrandHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                shadowElevation = 4.dp,
                modifier = Modifier.size(54.dp)
            ) {
                AsyncImage(
                    model = R.drawable.kady_logo,
                    contentDescription = "شعار كادي تك",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "KADY",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = KadyBlue
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "TECH",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = KadyCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = KadyBlue,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "FHD",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "TECHNOLOGY SOLUTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun UploadEmptyStateCard(
    onPickPhoto: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .border(
                        border = BorderStroke(2.dp, KadyBlue.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onPickPhoto() },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                brush = Brush.linearGradient(listOf(KadyBlue, KadyCyan)),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "اضغط لاختيار صورة من هاتفك",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "يدعم صور JPG و PNG و WebP بدقة عالية",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onPickPhoto,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("pick_image_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KadyBlue)
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "فتح المعرض لاختيار صورة",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ComparisonStatsSection(state: ConverterUiState) {
    val orig = state.originalStats
    val conv = state.conversionResult?.convertedStats

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Original Box
        Surface(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "الأصلية",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = orig?.dimensionsText ?: "—",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (orig?.formattedSize?.isNotBlank() == true) {
                    Text(
                        text = orig.formattedSize,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Arrow or separator
        Box(
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.CenterVertically)
                .background(KadyBlue.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = KadyBlue,
                modifier = Modifier.size(18.dp)
            )
        }

        // Converted Box
        Surface(
            modifier = Modifier.weight(1f),
            color = if (conv != null) KadyBlue.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(12.dp),
            border = if (conv != null) BorderStroke(1.dp, KadyBlue.copy(alpha = 0.3f)) else null
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = if (conv != null) "بعد التحويل (FHD)" else "المستهدفة",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (conv != null) KadyBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = conv?.dimensionsText ?: state.selectedPreset.subtitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (conv != null) KadyBlue else MaterialTheme.colorScheme.onSurface
                )
                if (conv != null) {
                    Text(
                        text = "${conv.formattedSize} · في ${state.conversionResult?.processingTimeMs}ms",
                        style = MaterialTheme.typography.bodySmall,
                        color = KadySuccess,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun PrivacyFooterCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = KadySuccess,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "خصوصية تامة ومعالجة محلية 100%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "كل عمليات التحسين والشحذ تتم على معالج جهازك مباشرة دون رفع صورك إلى أي سيرفر.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
