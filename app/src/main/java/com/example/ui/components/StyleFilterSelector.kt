package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraRoll
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterBAndW
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.StyleFilter
import com.example.ui.theme.KadyBlue
import com.example.ui.theme.KadyCyan

@Composable
fun StyleFilterSelector(
    selectedFilter: StyleFilter,
    onFilterSelected: (StyleFilter) -> Unit,
    modifier: Modifier = Modifier,
    isConverted: Boolean = false
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(KadyBlue, KadyCyan)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "فلاتر وتأثيرات الألوان السريعة",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isConverted) "تبديل فوري للفلتر على النتيجة المحسّنة" else "تطبيق لمسات فنية مع ترقية الدقة",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = if (selectedFilter != StyleFilter.NONE) KadyBlue else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = selectedFilter.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedFilter != StyleFilter.NONE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Filter Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StyleFilter.values().forEach { filter ->
                    FilterItemCard(
                        filter = filter,
                        isSelected = selectedFilter == filter,
                        onClick = { onFilterSelected(filter) }
                    )
                }
            }

            // Description of active filter
            AnimatedVisibility(
                visible = selectedFilter != StyleFilter.NONE,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = KadyBlue.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getFilterIcon(selectedFilter),
                            contentDescription = null,
                            tint = KadyBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${selectedFilter.title} (${selectedFilter.subtitle}): ${selectedFilter.description}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterItemCard(
    filter: StyleFilter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) KadyBlue else Color.Transparent,
        label = "filter_border_color"
    )
    val cardBackground = if (isSelected) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
        modifier = Modifier
            .width(106.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) borderColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .testTag("filter_chip_${filter.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual circle with filter palette gradient & icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(getFilterGradient(filter)),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "محدد",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = getFilterIcon(filter),
                        contentDescription = null,
                        tint = getFilterIconTint(filter),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = filter.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) KadyBlue else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Text(
                text = filter.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

private fun getFilterIcon(filter: StyleFilter): ImageVector {
    return when (filter) {
        StyleFilter.NONE -> Icons.Default.AutoAwesome
        StyleFilter.VIBRANT -> Icons.Default.Whatshot
        StyleFilter.SEPIA -> Icons.Default.HistoryEdu
        StyleFilter.GRAYSCALE -> Icons.Default.FilterBAndW
        StyleFilter.WARM -> Icons.Default.WbSunny
        StyleFilter.COOL -> Icons.Default.AcUnit
        StyleFilter.VINTAGE -> Icons.Default.CameraRoll
    }
}

private fun getFilterIconTint(filter: StyleFilter): Color {
    return when (filter) {
        StyleFilter.NONE -> KadyBlue
        StyleFilter.GRAYSCALE -> Color.White
        else -> Color.White
    }
}

private fun getFilterGradient(filter: StyleFilter): Brush {
    return when (filter) {
        StyleFilter.NONE -> Brush.linearGradient(
            listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
        )
        StyleFilter.VIBRANT -> Brush.linearGradient(
            listOf(Color(0xFFFF5252), Color(0xFFFF7A00), Color(0xFFFFD600))
        )
        StyleFilter.SEPIA -> Brush.linearGradient(
            listOf(Color(0xFF704214), Color(0xFFA07855), Color(0xFFD2B48C))
        )
        StyleFilter.GRAYSCALE -> Brush.linearGradient(
            listOf(Color(0xFF1E293B), Color(0xFF64748B), Color(0xFF94A3B8))
        )
        StyleFilter.WARM -> Brush.linearGradient(
            listOf(Color(0xFFFF8C00), Color(0xFFFFB300), Color(0xFFFFE082))
        )
        StyleFilter.COOL -> Brush.linearGradient(
            listOf(Color(0xFF006699), Color(0xFF00A3C4), Color(0xFF67E8F9))
        )
        StyleFilter.VINTAGE -> Brush.linearGradient(
            listOf(Color(0xFF5A4A42), Color(0xFF8C7A6B), Color(0xFFD4C5B9))
        )
    }
}
