package com.vergil.lottery.presentation.screens.imageeditor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.capsule.ContinuousRoundedRectangle
import com.vergil.lottery.presentation.BackdropDemoScaffold
import com.vergil.lottery.core.constants.ThemeMode
import com.vergil.lottery.di.AppModule
import com.vergil.lottery.presentation.components.LiquidButton
import com.vergil.lottery.presentation.components.LiquidGlassCard
import com.vergil.lottery.presentation.components.ShadowText
import com.vergil.lottery.presentation.theme.LotteryTheme
import kotlinx.coroutines.flow.collectLatest


@Composable
fun ImageEditorScreen(
    backdrop: Backdrop,
    themeMode: ThemeMode,
    imagePath: String,
    onBackClick: () -> Unit,
    onSaveImage: (String) -> Unit,
    modifier: Modifier = Modifier,
    imageEditorViewModel: ImageEditorViewModel = viewModel()
) {
    val state by imageEditorViewModel.state.collectAsState()


    BackHandler {
        onBackClick()
    }


    LaunchedEffect(imagePath) {
        imageEditorViewModel.loadImage(imagePath)
    }


    LaunchedEffect(Unit) {
        imageEditorViewModel.effect.collectLatest { effect ->
            when (effect) {
                is ImageEditorContract.Effect.ShowToast -> {

                }
                is ImageEditorContract.Effect.SaveImage -> {
                    onSaveImage(effect.imagePath)
                }
            }
        }
    }

    ImageEditorContent(
        backdrop = backdrop,
        modifier = modifier,
        state = state,
        currentThemeMode = themeMode,
        onBackClick = onBackClick,
        onRotateLeft = { imageEditorViewModel.sendIntent(ImageEditorContract.Intent.RotateLeft) },
        onRotateRight = { imageEditorViewModel.sendIntent(ImageEditorContract.Intent.RotateRight) },
        onZoomIn = { imageEditorViewModel.sendIntent(ImageEditorContract.Intent.ZoomIn) },
        onZoomOut = { imageEditorViewModel.sendIntent(ImageEditorContract.Intent.ZoomOut) },
        onReset = { imageEditorViewModel.sendIntent(ImageEditorContract.Intent.Reset) },
        onSave = { imageEditorViewModel.sendIntent(ImageEditorContract.Intent.SaveImage) },
    )
}

@Composable
private fun ImageEditorContent(
    backdrop: Backdrop,
    state: ImageEditorContract.State,
    currentThemeMode: ThemeMode,
    onBackClick: () -> Unit,
    onRotateLeft: () -> Unit,
    onRotateRight: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "返回",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clickable { onBackClick() }
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            ShadowText(
                text = "图片编辑",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))


        LiquidGlassCard(
            backdrop = backdrop,
            modifier = Modifier.fillMaxWidth(),
            themeMode = currentThemeMode
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (state.imageBitmap != null) {

                    val imageBitmap = remember(state.imageBitmap) {
                        state.imageBitmap!!.asImageBitmap()
                    }

                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "编辑中的图片",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                rotationZ = state.rotation
                                scaleX = state.scale
                                scaleY = state.scale
                            },
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = "加载图片失败",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        LiquidGlassCard(
            backdrop = backdrop,
            modifier = Modifier.fillMaxWidth(),
            themeMode = currentThemeMode
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    LiquidButton(
                        onClick = onRotateLeft,
                        backdrop = backdrop,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Default.RotateLeft,
                            contentDescription = "左旋转",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        ShadowText(
                            "左旋转",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Spacer(modifier = Modifier.size(8.dp))


                    LiquidButton(
                        onClick = onRotateRight,
                        backdrop = backdrop,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Default.RotateRight,
                            contentDescription = "右旋转",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        ShadowText(
                            "右旋转",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                HorizontalDivider()


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    LiquidButton(
                        onClick = onZoomIn,
                        backdrop = backdrop,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "放大",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        ShadowText(
                            "放大",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Spacer(modifier = Modifier.size(8.dp))


                    LiquidButton(
                        onClick = onZoomOut,
                        backdrop = backdrop,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomOut,
                            contentDescription = "缩小",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        ShadowText(
                            "缩小",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            LiquidButton(
                onClick = onReset,
                backdrop = backdrop,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                tint = Color.Unspecified,
                surfaceColor = MaterialTheme.colorScheme.surface.copy(0.2f)
            ) {
                ShadowText(
                    "重置",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                )
            }


            LiquidButton(
                onClick = onSave,
                backdrop = backdrop,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                tint = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "完成",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                ShadowText(
                    "完成",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}




private fun createMockImageEditorState(): ImageEditorContract.State {
    return ImageEditorContract.State(
        imageBitmap = null,
        rotation = 0f,
        scale = 1f,
        isLoading = false,
        error = null
    )
}

@Preview(name = "图片编辑 - 默认状态", showBackground = true)
@Composable
private fun ImageEditorScreenPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        val mockState = createMockImageEditorState()
        ImageEditorContent(
            backdrop = mockBackdrop,
            state = mockState,
            currentThemeMode = ThemeMode.LIGHT,
            onBackClick = {},
            onRotateLeft = {},
            onRotateRight = {},
            onZoomIn = {},
            onZoomOut = {},
            onReset = {},
            onSave = {}
        )
    }
}

@Preview(name = "图片编辑 - 深色模式", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ImageEditorScreenDarkPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        val mockState = createMockImageEditorState()
        ImageEditorContent(
            backdrop = mockBackdrop,
            state = mockState,
            currentThemeMode = ThemeMode.DARK,
            onBackClick = {},
            onRotateLeft = {},
            onRotateRight = {},
            onZoomIn = {},
            onZoomOut = {},
            onReset = {},
            onSave = {}
        )
    }
}

@Preview(name = "图片编辑 - 加载中", showBackground = true)
@Composable
private fun ImageEditorScreenLoadingPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        val loadingState = ImageEditorContract.State(
            imageBitmap = null,
            rotation = 0f,
            scale = 1f,
            isLoading = true,
            error = null
        )
        ImageEditorContent(
            backdrop = mockBackdrop,
            state = loadingState,
            currentThemeMode = ThemeMode.LIGHT,
            onBackClick = {},
            onRotateLeft = {},
            onRotateRight = {},
            onZoomIn = {},
            onZoomOut = {},
            onReset = {},
            onSave = {}
        )
    }
}

@Preview(name = "图片编辑 - 错误状态", showBackground = true)
@Composable
private fun ImageEditorScreenErrorPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        val errorState = ImageEditorContract.State(
            imageBitmap = null,
            rotation = 0f,
            scale = 1f,
            isLoading = false,
            error = "图片加载失败"
        )
        ImageEditorContent(
            backdrop = mockBackdrop,
            state = errorState,
            currentThemeMode = ThemeMode.LIGHT,
            onBackClick = {},
            onRotateLeft = {},
            onRotateRight = {},
            onZoomIn = {},
            onZoomOut = {},
            onReset = {},
            onSave = {}
        )
    }
}

@Preview(name = "图片编辑 - 已编辑状态", showBackground = true)
@Composable
private fun ImageEditorScreenEditedPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        val editedState = ImageEditorContract.State(
            imageBitmap = null, 
            rotation = 90f,
            scale = 1.5f,
            isLoading = false,
            error = null
        )
        ImageEditorContent(
            backdrop = mockBackdrop,
            state = editedState,
            currentThemeMode = ThemeMode.LIGHT,
            onBackClick = {},
            onRotateLeft = {},
            onRotateRight = {},
            onZoomIn = {},
            onZoomOut = {},
            onReset = {},
            onSave = {}
        )
    }
}
