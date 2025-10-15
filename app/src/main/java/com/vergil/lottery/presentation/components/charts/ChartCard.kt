package com.vergil.lottery.presentation.components.charts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.capsule.ContinuousRoundedRectangle
import com.vergil.lottery.core.constants.ThemeMode
import com.vergil.lottery.presentation.components.ShadowText


@Composable
fun ChartCard(
    title: String,
    subtitle: String? = null,
    backdrop: Backdrop,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isLightTheme = themeMode == ThemeMode.LIGHT || (themeMode == ThemeMode.SYSTEM && !androidx.compose.foundation.isSystemInDarkTheme())

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBackdrop(
                backdrop = backdrop,
                shape = { ContinuousRoundedRectangle(24f.dp) },
                effects = {
                    vibrancy()
                    lens(12f.dp.toPx(), 24f.dp.toPx())
                }
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            ShadowText(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isLightTheme) Color.Black else Color.White
                )
            )


            if (!subtitle.isNullOrEmpty()) {
                ShadowText(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isLightTheme) Color.Black.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.7f)
                    )
                )
            }


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    }
}
