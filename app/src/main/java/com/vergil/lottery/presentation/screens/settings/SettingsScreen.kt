package com.vergil.lottery.presentation.screens.settings

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler
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
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.vergil.lottery.core.constants.ThemeMode
import com.vergil.lottery.presentation.components.LiquidButton
import com.vergil.lottery.presentation.components.LiquidGlassCard
import com.vergil.lottery.presentation.components.LiquidToggle
import com.vergil.lottery.presentation.components.ShadowText
import com.vergil.lottery.presentation.components.rememberImagePickerLauncher
import com.vergil.lottery.presentation.screens.imageeditor.ImageEditorScreen
import com.vergil.lottery.presentation.theme.LotteryTheme
import kotlinx.coroutines.flow.collectLatest


@Composable
fun SettingsScreen(
    backdrop: Backdrop,
    themeMode: ThemeMode,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val state by settingsViewModel.state.collectAsState()
    var showImageEditor by remember { mutableStateOf(false) }
    var selectedImagePath by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current


    BackHandler {
        onBackClick()
    }


    val imagePickerLauncher = rememberImagePickerLauncher(
        context = context,
        onImageSelected = { path ->
            selectedImagePath = path
            showImageEditor = true
        },
        onError = { error ->

        }
    )


    LaunchedEffect(Unit) {
        settingsViewModel.effect.collectLatest { effect ->
            when (effect) {
                is SettingsContract.Effect.ShowToast -> {

                }
                is SettingsContract.Effect.OpenImagePicker -> {

                    imagePickerLauncher()
                }
            }
        }
    }


    if (showImageEditor && selectedImagePath != null) {
        ImageEditorScreen(
            backdrop = backdrop,
            themeMode = themeMode,
            imagePath = selectedImagePath!!,
            onBackClick = { 
                showImageEditor = false
                selectedImagePath = null
            },
            onSaveImage = { editedImagePath ->
                settingsViewModel.setCustomBackgroundPath(editedImagePath)
                showImageEditor = false
                selectedImagePath = null
            }
        )
    } else {

        SettingsContent(
            backdrop = backdrop,
            modifier = modifier,
            state = state,
            currentThemeMode = themeMode,
            onBackClick = onBackClick,
            onBackgroundSettingClick = { 
                settingsViewModel.sendIntent(SettingsContract.Intent.SelectCustomBackground)
            },
            onToggleCustomBackground = { enabled -> 
                settingsViewModel.sendIntent(SettingsContract.Intent.SetUseCustomBackground(enabled))
            },
            onResetToDefault = { 
                settingsViewModel.sendIntent(SettingsContract.Intent.ResetToDefaultBackground)
            },
            onSelectImage = { 
                settingsViewModel.sendIntent(SettingsContract.Intent.SelectCustomBackground)
            }
        )
    }
}

@Composable
private fun SettingsContent(
    backdrop: Backdrop,
    state: SettingsContract.State,
    currentThemeMode: ThemeMode,
    onBackClick: () -> Unit,
    onBackgroundSettingClick: () -> Unit,
    onToggleCustomBackground: (Boolean) -> Unit,
    onResetToDefault: () -> Unit,
    onSelectImage: () -> Unit,
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
            modifier = Modifier.fillMaxWidth(),
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
                text = "应用设置",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))


        LiquidGlassCard(
            backdrop = backdrop,
            modifier = Modifier.fillMaxWidth(),
            themeMode = currentThemeMode
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    ShadowText(
                        text = "自定义背景",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))


                ShadowText(
                    text = "设置应用的自定义背景图片",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShadowText(
                        text = "启用自定义背景",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    LiquidToggle(
                        selected = { state.useCustomBackground },
                        onSelect = onToggleCustomBackground,
                        backdrop = backdrop
                    )
                }


                if (state.useCustomBackground) {
                    Spacer(modifier = Modifier.height(16.dp))


                    LiquidButton(
                        onClick = onSelectImage,
                        backdrop = backdrop,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        ShadowText(
                            "选择图片",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))


                    LiquidButton(
                        onClick = onResetToDefault,
                        backdrop = backdrop,
                        modifier = Modifier.fillMaxWidth(),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        surfaceColor = MaterialTheme.colorScheme.surface.copy(0.2f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        ShadowText(
                            "恢复默认",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.size(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            ShadowText(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                ShadowText(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}




private fun createMockSettingsState(): SettingsContract.State {
    return SettingsContract.State(
        customBackgroundPath = null,
        useCustomBackground = false,
        cardOpacity = 0.8f,
        isLoading = false,
        error = null
    )
}

@Preview(name = "设置页面 - 默认状态", showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        val mockState = createMockSettingsState()
        SettingsContent(
            backdrop = mockBackdrop,
            state = mockState,
            currentThemeMode = ThemeMode.LIGHT,
            onBackClick = {},
            onBackgroundSettingClick = {},
            onToggleCustomBackground = {},
            onResetToDefault = {},
            onSelectImage = {}
        )
    }
}

@Preview(name = "设置页面 - 深色模式", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenDarkPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        val mockState = createMockSettingsState()
        SettingsContent(
            backdrop = mockBackdrop,
            state = mockState,
            currentThemeMode = ThemeMode.DARK,
            onBackClick = {},
            onBackgroundSettingClick = {},
            onToggleCustomBackground = {},
            onResetToDefault = {},
            onSelectImage = {}
        )
    }
}

@Preview(name = "设置页面 - 启用自定义背景", showBackground = true)
@Composable
private fun SettingsScreenCustomBackgroundPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        val customBackgroundState = SettingsContract.State(
            customBackgroundPath = "/storage/emulated/0/Pictures/custom_bg.jpg",
            useCustomBackground = true,
            cardOpacity = 0.8f,
            isLoading = false,
            error = null
        )
        SettingsContent(
            backdrop = mockBackdrop,
            state = customBackgroundState,
            currentThemeMode = ThemeMode.LIGHT,
            onBackClick = {},
            onBackgroundSettingClick = {},
            onToggleCustomBackground = {},
            onResetToDefault = {},
            onSelectImage = {}
        )
    }
}

@Preview(name = "设置页面 - 加载中", showBackground = true)
@Composable
private fun SettingsScreenLoadingPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        val loadingState = SettingsContract.State(
            customBackgroundPath = null,
            useCustomBackground = false,
            cardOpacity = 0.8f,
            isLoading = true,
            error = null
        )
        SettingsContent(
            backdrop = mockBackdrop,
            state = loadingState,
            currentThemeMode = ThemeMode.LIGHT,
            onBackClick = {},
            onBackgroundSettingClick = {},
            onToggleCustomBackground = {},
            onResetToDefault = {},
            onSelectImage = {}
        )
    }
}

@Preview(name = "设置页面 - 错误状态", showBackground = true)
@Composable
private fun SettingsScreenErrorPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        val errorState = SettingsContract.State(
            customBackgroundPath = null,
            useCustomBackground = false,
            cardOpacity = 0.8f,
            isLoading = false,
            error = "设置保存失败，请重试"
        )
        SettingsContent(
            backdrop = mockBackdrop,
            state = errorState,
            currentThemeMode = ThemeMode.LIGHT,
            onBackClick = {},
            onBackgroundSettingClick = {},
            onToggleCustomBackground = {},
            onResetToDefault = {},
            onSelectImage = {}
        )
    }
}

@Preview(name = "设置项组件", showBackground = true)
@Composable
private fun SettingItemPreview() {
    LotteryTheme {
        SettingItem(
            icon = androidx.compose.material.icons.Icons.Default.Image,
            title = "自定义背景",
            subtitle = "设置应用的自定义背景图片",
            onClick = {}
        )
    }
}