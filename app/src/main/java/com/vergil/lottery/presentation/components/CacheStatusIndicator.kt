package com.vergil.lottery.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.vergil.lottery.core.constants.LotteryType
import com.vergil.lottery.core.constants.ThemeMode
import com.vergil.lottery.data.cache.CacheStats
import com.vergil.lottery.di.AppModule
import kotlinx.coroutines.delay


@Composable
fun CacheStatusIndicator(
    backdrop: Backdrop,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    var cacheStats by remember { mutableStateOf<Map<LotteryType, CacheStats>>(emptyMap()) }
    var showIndicator by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        try {
            val stats = AppModule.cachedRepository.getCacheStats()
            cacheStats = stats
            showIndicator = true


            delay(3000)
            showIndicator = false
        } catch (e: Exception) {

        }
    }

    AnimatedVisibility(
        visible = showIndicator,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        CacheStatusContent(
            backdrop = backdrop,
            themeMode = themeMode,
            cacheStats = cacheStats
        )
    }
}

@Composable
private fun CacheStatusContent(
    backdrop: Backdrop,
    themeMode: ThemeMode,
    cacheStats: Map<LotteryType, CacheStats>,
    modifier: Modifier = Modifier
) {
    val isLightTheme = when (themeMode) {
        ThemeMode.LIGHT -> true
        ThemeMode.DARK -> false
        ThemeMode.SYSTEM -> !androidx.compose.foundation.isSystemInDarkTheme()
    }

    val backgroundColor = if (isLightTheme) {
        Color(0xFF4CAF50).copy(alpha = 0.9f)
    } else {
        Color(0xFF66BB6A).copy(alpha = 0.9f)
    }

    val textColor = Color.White


    val totalCached = cacheStats.values.count { it.hasData }
    val totalValid = cacheStats.values.count { it.isValid }
    val avgAge = cacheStats.values.map { it.ageInHours }.average().toInt()

    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        themeMode = themeMode
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (totalValid > 0) Icons.Default.Cached else Icons.Default.CloudDownload,
                    contentDescription = "缓存状态",
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (totalValid > 0) "缓存数据" else "网络数据",
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }


            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (totalCached > 0) {
                    Text(
                        text = "${totalCached}个彩种",
                        color = textColor.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )

                    if (avgAge > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${avgAge}h前",
                            color = textColor.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
