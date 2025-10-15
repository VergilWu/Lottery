package com.vergil.lottery.presentation.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.capsule.ContinuousRoundedRectangle
import com.vergil.lottery.core.constants.ThemeMode


@Composable
fun LiquidGlassCard(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    height: Dp? = null,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    cardOpacity: Float = 0.8f, 
    content: @Composable ColumnScope.() -> Unit = {}
) {

    val isLightTheme = when (themeMode) {
        ThemeMode.LIGHT -> true   
        ThemeMode.DARK -> false   
        ThemeMode.SYSTEM -> !isSystemInDarkTheme()  
    }

    Box(
        modifier = modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { ContinuousRoundedRectangle(32f.dp) },
                effects = {
                    vibrancy()
                    lens(16f.dp.toPx(), 32f.dp.toPx())

                    if (!isLightTheme) {
                        colorControls(
                            brightness = 0f,
                            saturation = 1.5f
                        )
                        blur(8f.dp.toPx())
                    }
                },
                onDrawSurface = {

                    if (!isLightTheme) {
                        drawRect(Color(0xFF121212).copy(alpha = 0.4f * cardOpacity))
                    }

                    drawRect(Color.White.copy(alpha = cardOpacity * 0.1f))
                }
            )
            .let { if (height != null) it.height(height) else it }
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            content()
        }
    }
}


@Composable
fun ShadowText(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    modifier: Modifier = Modifier
) {
    BasicText(
        text = text,
        style = style.copy(
            color = MaterialTheme.colorScheme.onSurface,
            shadow = Shadow(
                color = Color.Black.copy(alpha = 0.3f),
                offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                blurRadius = 2f
            )
        ),
        modifier = modifier
    )
}