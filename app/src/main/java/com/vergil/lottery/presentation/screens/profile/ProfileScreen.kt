package com.vergil.lottery.presentation.screens.profile

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
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
import com.vergil.lottery.core.constants.LotteryType
import com.vergil.lottery.presentation.BackdropDemoScaffold
import com.vergil.lottery.core.constants.ThemeMode
import com.vergil.lottery.di.AppModule
import com.vergil.lottery.presentation.components.LiquidButton
import com.vergil.lottery.presentation.components.LiquidGlassCard
import com.vergil.lottery.presentation.components.LiquidToggle
import com.vergil.lottery.presentation.components.ShadowText
import com.vergil.lottery.presentation.theme.LotteryTheme
import kotlinx.coroutines.flow.collectLatest


@Composable
fun ProfileScreen(
    backdrop: Backdrop,
    themeMode: ThemeMode,
    cardOpacity: Float = 0.8f, 
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel = viewModel(),
    themeViewModel: com.vergil.lottery.presentation.theme.ThemeViewModel = remember { AppModule.themeViewModel },
    onNavigateToSettings: () -> Unit = {}
) {
    val state by profileViewModel.state.collectAsState()
    val themeMode by themeViewModel.themeMode.collectAsState()
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        profileViewModel.effect.collectLatest { effect ->
            when (effect) {
                is ProfileContract.Effect.ShowToast -> {

                }
                is ProfileContract.Effect.NavigateToThemeSetting -> {

                }
                is ProfileContract.Effect.NavigateToDefaultLotterySetting -> {

                }
                is ProfileContract.Effect.NavigateToAbout -> {
                    showAboutDialog = true
                }
                is ProfileContract.Effect.NavigateToSettings -> {
                    onNavigateToSettings()
                }
            }
        }
    }

    ProfileContent(
        backdrop = backdrop,
        modifier = modifier,
        state = state,
        currentThemeMode = themeMode,
        cardOpacity = cardOpacity, 
        onThemeModeChanged = { themeViewModel.setThemeMode(it) },
        onThemeSettingClick = { profileViewModel.sendIntent(ProfileContract.Intent.NavigateToThemeSetting) },
        onDefaultLotteryClick = { profileViewModel.sendIntent(ProfileContract.Intent.NavigateToDefaultLotterySetting) },
        onAboutClick = { profileViewModel.sendIntent(ProfileContract.Intent.NavigateToAbout) },
        onSettingsClick = { profileViewModel.sendIntent(ProfileContract.Intent.NavigateToSettings) },
        onClearCache = { showClearCacheDialog = true },
        onToggleFavorite = { profileViewModel.sendIntent(ProfileContract.Intent.ToggleFavoriteMode) },
    )


    if (showClearCacheDialog) {
        ClearCacheConfirmDialog(
            backdrop = backdrop,
            themeMode = themeMode,
            cacheSize = state.cacheSize,
            isLoading = state.isLoading,
            onConfirm = {
                showClearCacheDialog = false
                profileViewModel.sendIntent(ProfileContract.Intent.ClearCache)
            },
            onDismiss = { showClearCacheDialog = false }
        )
    }


    if (showAboutDialog) {
        AboutAppDialog(
            backdrop = backdrop,
            themeMode = themeMode,
            onDismiss = { showAboutDialog = false }
        )
    }


}

@Composable
private fun ProfileContent(
    backdrop: Backdrop,
    state: ProfileContract.State,
    currentThemeMode: ThemeMode,
    cardOpacity: Float, 
    onThemeModeChanged: (ThemeMode) -> Unit,
    onThemeSettingClick: () -> Unit,
    onDefaultLotteryClick: () -> Unit,
    onAboutClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onClearCache: () -> Unit,
    onToggleFavorite: () -> Unit,
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
                bottom = 72.dp + 48.dp + 16.dp
            )
    ) {

        UserProfileCard(
            backdrop = backdrop,
            userName = state.userName,
            themeMode = currentThemeMode,
            cardOpacity = cardOpacity
        )

        Spacer(modifier = Modifier.height(24.dp))


        StatsCard(
            backdrop = backdrop,
            favoriteCount = state.favoriteCount,
            viewedCount = state.viewedDrawsCount,
            themeMode = currentThemeMode,
            cardOpacity = cardOpacity
        )

        Spacer(modifier = Modifier.height(16.dp))


        LiquidGlassCard(
            backdrop = backdrop,
            modifier = Modifier.fillMaxWidth(),
            themeMode = currentThemeMode,
            cardOpacity = cardOpacity
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                ThemeModeSelector(
                    currentMode = currentThemeMode,
                    onModeChanged = onThemeModeChanged,
                    backdrop = backdrop
                )

                HorizontalDivider()

                SettingItem(
                    icon = Icons.Default.SportsSoccer,
                    title = "默认彩种",
                    subtitle = state.defaultLotteryType.displayName,
                    onClick = onDefaultLotteryClick
                )

            }
        }

        Spacer(modifier = Modifier.height(16.dp))


    LiquidGlassCard(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth(),
        themeMode = currentThemeMode,
        cardOpacity = cardOpacity
    ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                SettingItem(
                    icon = Icons.Default.CleaningServices,
                    title = "清除缓存",
                    subtitle = state.cacheSize,
                    onClick = onClearCache
                )

                HorizontalDivider()


                SettingItem(
                    icon = Icons.Default.Settings,
                    title = "应用设置",
                    onClick = onSettingsClick
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


    LiquidGlassCard(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth(),
        themeMode = currentThemeMode,
        cardOpacity = cardOpacity
    ) {
            SettingItem(
                icon = Icons.Default.Info,
                title = "关于应用",
                subtitle = "版本 1.0.0",
                onClick = onAboutClick
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "调试模式 · 管理员账号: admin",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun UserProfileCard(
    backdrop: Backdrop,
    userName: String,
    themeMode: ThemeMode,
    cardOpacity: Float, 
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier.fillMaxWidth(),
        themeMode = themeMode,
        cardOpacity = cardOpacity
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.size(16.dp))


            Column {
                ShadowText(
                    text = userName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                ShadowText(
                    text = "admin@lottery.com",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                )
            }
        }
    }
}

@Composable
private fun StatsCard(
    backdrop: Backdrop,
    favoriteCount: Int,
    viewedCount: Int,
    themeMode: ThemeMode,
    cardOpacity: Float, 
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier.fillMaxWidth(),
        themeMode = themeMode,
        cardOpacity = cardOpacity
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.Default.Favorite,
                label = "收藏",
                count = favoriteCount
            )
            StatItem(
                icon = Icons.Default.SportsSoccer,
                label = "查看",
                count = viewedCount
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        ShadowText(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(4.dp))
        ShadowText(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}



@Composable
private fun ThemeModeSelector(
    currentMode: ThemeMode,
    onModeChanged: (ThemeMode) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        ShadowText(
            text = "主题模式",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
        )

        Spacer(modifier = Modifier.height(12.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(ThemeMode.LIGHT, ThemeMode.DARK).forEach { mode ->
                val isSelected = currentMode == mode

                LiquidButton(
                    onClick = { onModeChanged(mode) },
                    backdrop = backdrop,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    tint = if (isSelected) Color(0xFF0088FF) else Color.Unspecified,
                    surfaceColor = if (isSelected) Color.Unspecified else Color.White.copy(0.3f)
                ) {
                    ShadowText(
                        text = when (mode) {
                            ThemeMode.LIGHT -> "浅色"
                            ThemeMode.DARK -> "深色"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isSelected) Color.White else Color.Black,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShadowText(
                text = "跟随系统主题",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )

            LiquidToggle(
                selected = { currentMode == ThemeMode.SYSTEM },
                onSelect = { isSelected ->
                    onModeChanged(if (isSelected) ThemeMode.SYSTEM else ThemeMode.LIGHT)
                },
                backdrop = backdrop
            )
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
            .clickable(onClick = onClick)
            .padding(16.dp),
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
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun ClearCacheConfirmDialog(
    backdrop: Backdrop,
    themeMode: ThemeMode,
    cacheSize: String,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLightTheme = when (themeMode) {
        ThemeMode.LIGHT -> true
        ThemeMode.DARK -> false
        ThemeMode.SYSTEM -> !isSystemInDarkTheme()
    }
    val contentColor = if (isLightTheme) Color.Black else Color.White
    val accentColor = if (isLightTheme) Color(0xFF0088FF) else Color(0xFF0091FF)
    val containerColor = if (isLightTheme) Color(0xFFFAFAFA).copy(0.6f) else Color(0xFF121212).copy(0.4f)
    val dimColor = if (isLightTheme) Color(0xFF29293A).copy(0.23f) else Color(0xFF121212).copy(0.56f)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        BackdropDemoScaffold(
            Modifier.drawWithContent {
                drawContent()
                drawRect(dimColor)
            },
            initialPainterResId = com.vergil.lottery.R.drawable.wallpaper_light
        ) { backdrop ->
            Column(
                Modifier
                    .padding(24f.dp)
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { ContinuousRoundedRectangle(32f.dp) },
                        effects = {
                            colorControls(
                                brightness = if (isLightTheme) 0.2f else 0f,
                                saturation = 1.5f
                            )
                            blur(if (isLightTheme) 16f.dp.toPx() else 8f.dp.toPx())
                            lens(24f.dp.toPx(), 32f.dp.toPx(), depthEffect = true)
                        },
                        highlight = { Highlight.Plain },
                        onDrawSurface = { drawRect(containerColor) }
                    )
                    .fillMaxWidth()
            ) {

                ShadowText(
                    text = "清除缓存",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = contentColor,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(24f.dp, 20f.dp, 24f.dp, 16f.dp)
                )


                Column(
                    modifier = Modifier.padding(horizontal = 24f.dp)
                ) {
                    ShadowText(
                        text = "确定要清除所有缓存数据吗？",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = contentColor
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ShadowText(
                        text = "当前缓存大小：$cacheSize",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = contentColor.copy(alpha = 0.7f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ShadowText(
                        text = "清除缓存后，应用将重新下载数据，可能会消耗更多流量。",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = contentColor.copy(alpha = 0.6f)
                        )
                    )
                }


                Row(
                    Modifier
                        .padding(24f.dp, 20f.dp, 24f.dp, 20f.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12f.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    LiquidButton(
                        onClick = onDismiss,
                        backdrop = backdrop,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        tint = Color.Unspecified,
                        surfaceColor = containerColor.copy(0.2f)
                    ) {
                        ShadowText(
                            "取消",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = contentColor.copy(alpha = 0.8f)
                            )
                        )
                    }


                    LiquidButton(
                        onClick = onConfirm,
                        backdrop = backdrop,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        tint = if (isLoading) Color.Gray else accentColor,
                        isInteractive = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                        }
                        ShadowText(
                            if (isLoading) "清除中..." else "确认清除",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun AboutAppDialog(
    backdrop: Backdrop,
    themeMode: ThemeMode,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLightTheme = when (themeMode) {
        ThemeMode.LIGHT -> true
        ThemeMode.DARK -> false
        ThemeMode.SYSTEM -> !isSystemInDarkTheme()
    }
    val contentColor = if (isLightTheme) Color.Black else Color.White
    val accentColor = if (isLightTheme) Color(0xFF0088FF) else Color(0xFF0091FF)
    val containerColor = if (isLightTheme) Color(0xFFFAFAFA).copy(0.6f) else Color(0xFF121212).copy(0.4f)
    val dimColor = if (isLightTheme) Color(0xFF29293A).copy(0.23f) else Color(0xFF121212).copy(0.56f)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        BackdropDemoScaffold(
            Modifier.drawWithContent {
                drawContent()
                drawRect(dimColor)
            },
            initialPainterResId = com.vergil.lottery.R.drawable.wallpaper_light
        ) { backdrop ->
            Column(
                Modifier
                    .padding(24f.dp)
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { ContinuousRoundedRectangle(32f.dp) },
                        effects = {
                            colorControls(
                                brightness = if (isLightTheme) 0.2f else 0f,
                                saturation = 1.5f
                            )
                            blur(if (isLightTheme) 16f.dp.toPx() else 8f.dp.toPx())
                            lens(24f.dp.toPx(), 32f.dp.toPx(), depthEffect = true)
                        },
                        highlight = { Highlight.Plain },
                        onDrawSurface = { drawRect(containerColor) }
                    )
                    .fillMaxWidth()
            ) {

                ShadowText(
                    text = "关于应用",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = contentColor,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(24f.dp, 20f.dp, 24f.dp, 16f.dp)
                )


                Column(
                    modifier = Modifier.padding(horizontal = 24f.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                        Column {
                            ShadowText(
                                text = "彩票工具软件",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = contentColor,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            ShadowText(
                                text = "版本 1.0.0",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = contentColor.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))


                    ShadowText(
                        text = "一款基于现代化 Android 技术栈开发的彩票工具软件，提供开奖查询、数据分析、智能预测等功能。",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = contentColor.copy(alpha = 0.8f)
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))


                    ShadowText(
                        text = "开发者信息",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = contentColor,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))


                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = contentColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        ShadowText(
                            text = "Vergil",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = contentColor,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))


                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = contentColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        ShadowText(
                            text = "vergilcat@gmail.com",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = contentColor.copy(alpha = 0.8f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = contentColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        ShadowText(
                            text = "https://github.com/VergilWu/Lottery",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = contentColor.copy(alpha = 0.8f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))


                    ShadowText(
                        text = "技术栈",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = contentColor,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ShadowText(
                        text = "• Jetpack Compose\n• Material3 设计系统\n• MVI 架构模式\n• Liquid Glass UI\n• Kotlin 协程\n• TensorFlow Lite",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = contentColor.copy(alpha = 0.7f)
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))


                    ShadowText(
                        text = "免责声明",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = contentColor,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ShadowText(
                        text = "本应用仅供学习和研究使用，不构成任何投注建议。预测结果仅供参考，不保证准确性。用户应理性对待彩票，量力而行。开发者不承担因使用本应用而产生的任何损失。",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = contentColor.copy(alpha = 0.6f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ShadowText(
                        text = "重要声明：本应用不提供任何形式的投注服务，不参与任何赌博活动。所有数据来源于公开渠道，预测算法仅用于技术研究。用户使用本应用即表示同意承担所有风险，开发者不承担任何法律责任。",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = contentColor.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }


                Row(
                    Modifier
                        .padding(24f.dp, 20f.dp, 24f.dp, 20f.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12f.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    LiquidButton(
                        onClick = onDismiss,
                        backdrop = backdrop,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        tint = accentColor
                    ) {
                        ShadowText(
                            "关闭",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}






private fun createMockProfileState(): ProfileContract.State {
    return ProfileContract.State(
        userName = "彩票达人",
        userAvatar = null,
        defaultLotteryType = LotteryType.SSQ,
        favoriteCount = 15,
        viewedDrawsCount = 128,
        cacheSize = "25.6 MB",
        isLoading = false,
        error = null
    )
}

@Preview(name = "个人中心 - 默认状态", showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        val mockState = createMockProfileState()
        ProfileContent(
            backdrop = mockBackdrop,
            state = mockState,
            currentThemeMode = ThemeMode.LIGHT,
            cardOpacity = 0.8f,
            onThemeModeChanged = {},
            onThemeSettingClick = {},
            onDefaultLotteryClick = {},
            onAboutClick = {},
            onSettingsClick = {},
            onClearCache = {},
            onToggleFavorite = {}
        )
    }
}

@Preview(name = "个人中心 - 深色模式", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileScreenDarkPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        val mockState = createMockProfileState()
        ProfileContent(
            backdrop = mockBackdrop,
            state = mockState,
            currentThemeMode = ThemeMode.DARK,
            cardOpacity = 0.8f,
            onThemeModeChanged = {},
            onThemeSettingClick = {},
            onDefaultLotteryClick = {},
            onAboutClick = {},
            onSettingsClick = {},
            onClearCache = {},
            onToggleFavorite = {}
        )
    }
}

@Preview(name = "个人中心 - 加载中", showBackground = true)
@Composable
private fun ProfileScreenLoadingPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        val loadingState = ProfileContract.State(
            userName = "彩票达人",
            userAvatar = null,
            defaultLotteryType = LotteryType.SSQ,
            favoriteCount = 0,
            viewedDrawsCount = 0,
            cacheSize = "0 MB",
            isLoading = true,
            error = null
        )
        ProfileContent(
            backdrop = mockBackdrop,
            state = loadingState,
            currentThemeMode = ThemeMode.LIGHT,
            cardOpacity = 0.8f,
            onThemeModeChanged = {},
            onThemeSettingClick = {},
            onDefaultLotteryClick = {},
            onAboutClick = {},
            onSettingsClick = {},
            onClearCache = {},
            onToggleFavorite = {}
        )
    }
}

@Preview(name = "个人中心 - 错误状态", showBackground = true)
@Composable
private fun ProfileScreenErrorPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        val errorState = ProfileContract.State(
            userName = "彩票达人",
            userAvatar = null,
            defaultLotteryType = LotteryType.SSQ,
            favoriteCount = 0,
            viewedDrawsCount = 0,
            cacheSize = "0 MB",
            isLoading = false,
            error = "加载用户信息失败"
        )
        ProfileContent(
            backdrop = mockBackdrop,
            state = errorState,
            currentThemeMode = ThemeMode.LIGHT,
            cardOpacity = 0.8f,
            onThemeModeChanged = {},
            onThemeSettingClick = {},
            onDefaultLotteryClick = {},
            onAboutClick = {},
            onSettingsClick = {},
            onClearCache = {},
            onToggleFavorite = {}
        )
    }
}

@Preview(name = "用户信息卡片", showBackground = true)
@Composable
private fun UserInfoCardPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        UserProfileCard(
            backdrop = mockBackdrop,
            userName = "彩票达人",
            themeMode = ThemeMode.LIGHT,
            cardOpacity = 0.8f
        )
    }
}

@Preview(name = "统计信息卡片", showBackground = true)
@Composable
private fun StatisticsCardPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        StatsCard(
            backdrop = mockBackdrop,
            favoriteCount = 15,
            viewedCount = 128,
            themeMode = ThemeMode.LIGHT,
            cardOpacity = 0.8f
        )
    }
}

@Preview(name = "设置选项卡片", showBackground = true)
@Composable
private fun SettingsCardPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        LiquidGlassCard(
            backdrop = mockBackdrop,
            modifier = Modifier.fillMaxWidth(),
            themeMode = ThemeMode.LIGHT,
            cardOpacity = 0.8f
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                SettingItem(
                    icon = Icons.Default.SportsSoccer,
                    title = "默认彩种",
                    subtitle = "双色球",
                    onClick = {}
                )
                HorizontalDivider()
                SettingItem(
                    icon = Icons.Default.CleaningServices,
                    title = "清除缓存",
                    subtitle = "25.6 MB",
                    onClick = {}
                )
            }
        }
    }
}
