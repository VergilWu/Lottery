package com.vergil.lottery.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.capsule.ContinuousCapsule
import kotlinx.coroutines.flow.collectLatest


@Composable
fun LiquidSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
    trackHeight: Dp = 6.dp,
    thumbSize: Dp = 40.dp,
    label: String? = null,
    valueFormatter: (Float) -> String = { "%.0f%%".format(it * 100) }
) {
    val isLightTheme = !isSystemInDarkTheme()
    val accentColor =
        if (isLightTheme) Color(0xFF0088FF)
        else Color(0xFF0091FF)
    val trackColor =
        if (isLightTheme) Color(0xFF787878).copy(0.2f)
        else Color(0xFF787880).copy(0.36f)

    val trackBackdrop = rememberLayerBackdrop()
    val thumbBackdrop = rememberLayerBackdrop()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (label != null) {
                ShadowText(
                    text = label,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                )
            }
            ShadowText(
                text = valueFormatter(value),
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
        }

        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            val trackWidth = constraints.maxWidth
            val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
            val animationScope = rememberCoroutineScope()


            val progress = if (valueRange.endInclusive == valueRange.start) {
                0f
            } else {
                ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start))
                    .fastCoerceIn(0f, 1f)
            }


            Box(Modifier.layerBackdrop(trackBackdrop)) {
                Box(
                    modifier = Modifier
                        .clip(ContinuousCapsule)
                        .background(trackColor)
                        .pointerInput(enabled) {
                            if (!enabled) return@pointerInput
                            detectTapGestures { position ->
                                val delta = (valueRange.endInclusive - valueRange.start) * (position.x / trackWidth)
                                val targetValue =
                                    (if (isLtr) valueRange.start + delta
                                    else valueRange.endInclusive - delta)
                                        .coerceIn(valueRange)


                                val steppedValue = if (steps > 0) {
                                    val stepSize = (valueRange.endInclusive - valueRange.start) / steps
                                    val stepIndex = ((targetValue - valueRange.start) / stepSize).toInt()
                                    valueRange.start + stepIndex * stepSize
                                } else {
                                    targetValue
                                }

                                onValueChange(steppedValue)
                            }
                        }
                        .height(trackHeight)
                        .fillMaxWidth()
                )


                Box(
                    modifier = Modifier
                        .clip(ContinuousCapsule)
                        .background(accentColor)
                        .height(trackHeight)
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            val width = (constraints.maxWidth * progress).fastRoundToInt()
                            layout(width, placeable.height) {
                                placeable.place(0, 0)
                            }
                        }
                )
            }


            Box(
                modifier = Modifier
                    .layerBackdrop(thumbBackdrop)
                    .drawBackdrop(
                        backdrop = thumbBackdrop,
                        shape = { ContinuousCapsule },
                        effects = {
                            blur(8f.dp.toPx())
                            lens(16f.dp.toPx(), 32f.dp.toPx())
                        },
                        highlight = {
                            Highlight(
                                width = 8f.dp,
                                alpha = 0.3f
                            )
                        },
                        shadow = {
                            Shadow(
                                radius = 4f.dp,
                                color = Color.Black.copy(alpha = 0.05f)
                            )
                        },
                        onDrawSurface = {
                            drawRect(Color.White.copy(alpha = 0.1f))
                        }
                    )
                    .size(thumbSize, 24.dp)
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        val x = (constraints.maxWidth * progress - placeable.width / 2).fastRoundToInt()
                        layout(placeable.width, placeable.height) {
                            placeable.place(x, 0)
                        }
                    }
            )
        }
    }
}