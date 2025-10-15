package com.vergil.lottery.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.vergil.lottery.core.constants.ThemeMode


@Composable
fun BackdropLiquidGlassCard(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    glassAlpha: Float = 0.5f,
    tintColor: Color? = null,
    blurRadius: Dp = 16.dp,
    refractionHeight: Dp = 24.dp,
    refractionAmount: Dp = 48.dp,
    depthEffect: Boolean = true,
    chromaticAberration: Boolean = false,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
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
                shape = { shape },
                effects = {

                    vibrancy()


                    blur(blurRadius.toPx())


                    lens(
                        refractionHeight = refractionHeight.toPx(),
                        refractionAmount = refractionAmount.toPx(),
                        depthEffect = depthEffect,
                        chromaticAberration = chromaticAberration
                    )


                    if (!isLightTheme) {
                        colorControls(
                            brightness = 0f,
                            saturation = 1.5f
                        )
                    }
                },
                onDrawSurface = {
                    if (tintColor != null) {

                        drawRect(tintColor, blendMode = androidx.compose.ui.graphics.BlendMode.Hue)
                        drawRect(tintColor.copy(alpha = glassAlpha / 2f))
                    } else {

                        drawRect(Color.White.copy(alpha = glassAlpha))

                        if (!isLightTheme) {
                            drawRect(Color(0xFF121212).copy(alpha = 0.4f))
                        }
                    }
                }
            )
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
    ) {
        content()
    }
}


@Composable
fun SmallBackdropLiquidGlassCard(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    BackdropLiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        glassAlpha = 0.45f,
        blurRadius = 12.dp,
        refractionHeight = 16.dp,
        refractionAmount = 32.dp,
        depthEffect = true,
        onClick = onClick,
        content = content
    )
}


@Composable
fun MediumBackdropLiquidGlassCard(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    BackdropLiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        glassAlpha = 0.5f,
        blurRadius = 16.dp,
        refractionHeight = 20.dp,
        refractionAmount = 40.dp,
        depthEffect = true,
        onClick = onClick,
        content = content
    )
}


@Composable
fun LargeBackdropLiquidGlassCard(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    BackdropLiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        glassAlpha = 0.55f,
        blurRadius = 20.dp,
        refractionHeight = 24.dp,
        refractionAmount = 48.dp,
        depthEffect = true,
        chromaticAberration = true, 
        onClick = onClick,
        content = content
    )
}


@Composable
fun XLargeBackdropLiquidGlassCard(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    BackdropLiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        glassAlpha = 0.6f,
        blurRadius = 24.dp,
        refractionHeight = 28.dp,
        refractionAmount = 56.dp,
        depthEffect = true,
        chromaticAberration = true,
        onClick = onClick,
        content = content
    )
}


@Composable
fun CircleBackdropLiquidGlassButton(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    BackdropLiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier,
        shape = CircleShape,
        glassAlpha = 0.5f,
        blurRadius = 16.dp,
        refractionHeight = 20.dp,
        refractionAmount = 32.dp,
        depthEffect = true,
        chromaticAberration = true,
        onClick = onClick,
        content = content
    )
}

