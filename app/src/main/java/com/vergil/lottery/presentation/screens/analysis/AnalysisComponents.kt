package com.vergil.lottery.presentation.screens.analysis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.vergil.lottery.domain.analyzer.AnalysisEngine
import com.vergil.lottery.presentation.components.LiquidGlassCard
import com.vergil.lottery.presentation.components.LiquidButton
import com.vergil.lottery.presentation.components.ShadowText
import com.vergil.lottery.core.constants.ThemeMode
import java.util.Locale


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalysisDimensionSelector(
    backdrop: Backdrop,
    selectedDimension: AnalysisContract.AnalysisDimension,
    onDimensionSelected: (AnalysisContract.AnalysisDimension) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ShadowText(
            text = "分析维度",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnalysisContract.AnalysisDimension.entries.forEach { dimension ->
                val isSelected = selectedDimension == dimension
                LiquidButton(
                    onClick = { onDimensionSelected(dimension) },
                    backdrop = backdrop,
                    modifier = Modifier.height(40.dp),
                    tint = if (isSelected) Color(0xFF0088FF) else Color.Unspecified,
                    surfaceColor = if (isSelected) Color.Unspecified else Color.White.copy(0.3f)
                ) {
                    ShadowText(
                        text = dimension.displayName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isSelected) Color.White else Color.Black
                        )
                    )
                }
            }
        }
    }
}


@Composable
fun BallTypeSelector(
    backdrop: Backdrop,
    selectedBallType: AnalysisContract.BallType,
    onBallTypeSelected: (AnalysisContract.BallType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnalysisContract.BallType.entries.forEach { ballType ->
            val isSelected = selectedBallType == ballType
            val ballColor = if (ballType == AnalysisContract.BallType.RED) {
                Color(0xFFE53935)
            } else {
                Color(0xFF1E88E5)
            }

            LiquidButton(
                onClick = { onBallTypeSelected(ballType) },
                backdrop = backdrop,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                tint = if (isSelected) ballColor else Color.Unspecified,
                surfaceColor = if (isSelected) Color.Unspecified else Color.White.copy(0.3f)
            ) {
                ShadowText(
                    text = ballType.displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isSelected) Color.White else Color.Black
                    )
                )
            }
        }
    }
}




@Composable
fun OmissionDataCard(
    backdrop: Backdrop,
    data: AnalysisEngine.OmissionData,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier.fillMaxWidth(),
        themeMode = themeMode
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = data.number,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )


            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "当前遗漏: ${data.currentOmission}期",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "最大遗漏: ${data.maxOmission}期",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "平均遗漏: ${String.format(Locale.getDefault(), "%.1f", data.avgOmission)}期",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
fun FrequencyDataCard(
    backdrop: Backdrop,
    data: AnalysisEngine.FrequencyData,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier.fillMaxWidth(),
        themeMode = themeMode
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.number,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "出现次数: ${data.count}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "频率: ${String.format(Locale.getDefault(), "%.1f", data.frequency)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }


            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (data.frequency / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}


@Composable
fun HotColdDataCard(
    backdrop: Backdrop,
    data: AnalysisEngine.HotColdData,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    val categoryColor = when (data.category) {
        AnalysisEngine.HotColdCategory.HOT -> Color(0xFFE53935)    
        AnalysisEngine.HotColdCategory.WARM -> Color(0xFFFFA726)   
        AnalysisEngine.HotColdCategory.COLD -> Color(0xFF42A5F5)   
    }

    val categoryText = when (data.category) {
        AnalysisEngine.HotColdCategory.HOT -> "热号"
        AnalysisEngine.HotColdCategory.WARM -> "温号"
        AnalysisEngine.HotColdCategory.COLD -> "冷号"
    }

    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier.fillMaxWidth(),
        themeMode = themeMode
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.number,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = categoryColor
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = categoryText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                    Text(
                        text = "近期出现: ${data.recentCount}次",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))


            LinearProgressIndicator(
                progress = { data.temperature / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = categoryColor
            )

            Text(
                text = "冷热度: ${String.format(Locale.getDefault(), "%.1f", data.temperature)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
fun ConsecutiveDataCard(
    backdrop: Backdrop,
    data: AnalysisEngine.ConsecutiveData,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier.fillMaxWidth(),
        themeMode = themeMode
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = data.numbers.joinToString(" - "),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "出现: ${data.count}次",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${String.format(Locale.getDefault(), "%.1f", data.frequency)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SameTailDataCard(
    backdrop: Backdrop,
    data: AnalysisEngine.SameTailData,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier.fillMaxWidth(),
        themeMode = themeMode
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "尾数 ${data.tail}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "出现: ${data.count}次",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.1f", data.frequency)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))


            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                data.numbers.forEach { number ->
                    LiquidGlassCard(
                        backdrop = backdrop,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = number,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SumValueDataCard(
    backdrop: Backdrop,
    data: AnalysisEngine.SumValueData,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier.fillMaxWidth(),
        themeMode = themeMode
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "和值 ${data.sumValue}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "奇偶比: ${data.oddEvenRatio}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${data.count}次",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${String.format(Locale.getDefault(), "%.1f", data.frequency)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SpanDataCard(
    backdrop: Backdrop,
    data: AnalysisEngine.SpanData,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier.fillMaxWidth(),
        themeMode = themeMode
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "跨度 ${data.span}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${data.count}次",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${String.format(Locale.getDefault(), "%.1f", data.frequency)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ACValueDataCard(
    backdrop: Backdrop,
    data: AnalysisEngine.ACValueData,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier.fillMaxWidth(),
        themeMode = themeMode
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AC值 ${data.acValue}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${data.count}次",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${String.format(Locale.getDefault(), "%.1f", data.frequency)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
fun RatioDataCard(
    backdrop: Backdrop,
    data: Any,  
    title: String,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    val (ratio, count, frequency) = when (data) {
        is AnalysisEngine.OddEvenRatioData -> Triple(data.ratio, data.count, data.frequency)
        is AnalysisEngine.SizeRatioData -> Triple(data.ratio, data.count, data.frequency)
        is AnalysisEngine.PrimeCompositeRatioData -> Triple(data.ratio, data.count, data.frequency)
        else -> Triple("0:0", 0, 0f)
    }

    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier.fillMaxWidth(),
        themeMode = themeMode
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$title $ratio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${count}次",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${String.format(Locale.getDefault(), "%.1f", frequency)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
fun ZoneDataCard(
    backdrop: Backdrop,
    data: AnalysisEngine.ZoneData,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier.fillMaxWidth(),
        themeMode = themeMode
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "第${data.zone}区 (${data.range})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${data.count}次",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))


            LinearProgressIndicator(
                progress = { (data.frequency / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = "${String.format(Locale.getDefault(), "%.1f", data.frequency)}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

