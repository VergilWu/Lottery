package com.vergil.lottery.presentation.components.charts

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vergil.lottery.core.constants.ThemeMode
import com.vergil.lottery.presentation.components.ShadowText


@Composable
fun SimpleBarChart(
    title: String,
    data: List<ChartDataPoint>,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    val isLightTheme = themeMode == ThemeMode.LIGHT || (themeMode == ThemeMode.SYSTEM && !androidx.compose.foundation.isSystemInDarkTheme())
    val textColor = if (isLightTheme) Color.Black else Color.White

    if (data.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ShadowText(
                text = "暂无数据",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = textColor.copy(alpha = 0.6f)
                )
            )
        }
        return
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        ShadowText(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        )


        val maxValue = data.maxOfOrNull { it.value } ?: 1
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
        ) {
            items(data.size) { index ->
                val point = data[index]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.width(60.dp) 
                ) {

                    val barHeight = (point.value.toFloat() / maxValue * 120).dp.coerceAtLeast(8.dp)
                    Box(
                        modifier = Modifier
                            .width(20.dp) 
                            .height(barHeight)
                            .background(
                                color = when {
                                    point.value >= maxValue * 0.8f -> Color(0xFF4CAF50)
                                    point.value >= maxValue * 0.5f -> Color(0xFFFF9800)
                                    else -> Color(0xFF2196F3)
                                },
                                shape = RoundedCornerShape(4.dp)
                            )
                    )


                    ShadowText(
                        text = point.value.toString(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                    )


                    ShadowText(
                        text = point.label,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = textColor.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        }
    }
}


@Composable
fun SimplePieChart(
    title: String,
    data: List<ChartDataPoint>,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    val isLightTheme = themeMode == ThemeMode.LIGHT || (themeMode == ThemeMode.SYSTEM && !androidx.compose.foundation.isSystemInDarkTheme())
    val textColor = if (isLightTheme) Color.Black else Color.White

    if (data.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ShadowText(
                text = "暂无数据",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = textColor.copy(alpha = 0.6f)
                )
            )
        }
        return
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        ShadowText(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        )


        val totalValue = data.sumOf { it.value }
        val colors = listOf(
            Color(0xFF4CAF50),
            Color(0xFF2196F3),
            Color(0xFFFF9800),
            Color(0xFFF44336),
            Color(0xFF9C27B0),
            Color(0xFF00BCD4),
            Color(0xFFFFC107),
            Color(0xFFE91E63)
        )

        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.height(200.dp), 
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(data.size) { index ->
                val point = data[index]
                val percentage = (point.value.toFloat() / totalValue * 100).toInt()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(
                                    color = colors[index % colors.size],
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ShadowText(
                            text = point.label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = textColor
                            )
                        )
                    }

                    ShadowText(
                        text = "${point.value}次 (${percentage}%)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textColor.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        }
    }
}

data class ChartDataPoint(
    val label: String,    
    val value: Int        
)
