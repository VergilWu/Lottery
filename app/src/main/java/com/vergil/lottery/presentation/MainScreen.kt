package com.vergil.lottery.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vergil.lottery.presentation.components.LiquidBottomTabs
import com.vergil.lottery.presentation.components.LiquidBottomTab
import com.vergil.lottery.presentation.screens.home.HomeScreen
import com.vergil.lottery.presentation.screens.analysis.AnalysisScreen
import com.vergil.lottery.presentation.screens.prediction.PredictionScreen
import com.vergil.lottery.presentation.screens.profile.ProfileScreen
import com.vergil.lottery.presentation.screens.settings.SettingsScreen
import com.vergil.lottery.core.background.rememberBackgroundManager
import com.vergil.lottery.data.local.preferences.PreferencesManager
import com.vergil.lottery.core.constants.ThemeMode
import com.vergil.lottery.R
import com.vergil.lottery.presentation.theme.ThemeViewModel
import com.vergil.lottery.di.AppModule
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import kotlinx.coroutines.flow.combine


@Composable
fun MainScreen() {
    val themeViewModel: ThemeViewModel = remember { AppModule.themeViewModel }
    val themeMode by themeViewModel.themeMode.collectAsState()
    val context = LocalContext.current
    val preferencesManager = remember { AppModule.preferencesManager }
    val backgroundManager = rememberBackgroundManager()


    var currentBackgroundResId by remember { mutableStateOf(R.drawable.wallpaper_light) }
    var currentCustomBackgroundPath by remember { mutableStateOf<String?>(null) }


    val cardOpacity by preferencesManager.cardOpacity.collectAsState(initial = 0.8f)


    val isSystemDarkTheme = isSystemInDarkTheme()


    LaunchedEffect(Unit) {
        combine(
            preferencesManager.useCustomBackground,
            preferencesManager.customBackgroundPath,
            themeViewModel.themeMode
        ) { useCustom, customPath, theme ->
            val isDarkTheme = when (theme) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemDarkTheme
            }

            if (useCustom && customPath != null) {

                currentCustomBackgroundPath = customPath
                R.drawable.wallpaper_light 
            } else {

                currentCustomBackgroundPath = null
                R.drawable.wallpaper_light 
            }
        }.collect { resId ->
            currentBackgroundResId = resId
        }
    }

    BackdropDemoScaffold(
        initialPainterResId = currentBackgroundResId,
        customBackgroundPath = currentCustomBackgroundPath
    ) { backdrop ->
        Box(modifier = Modifier.fillMaxSize()) {
            var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
            var showSettingsScreen by remember { mutableStateOf(false) }


            AnimatedContent(
                targetState = if (showSettingsScreen) "settings" else selectedTabIndex,
                transitionSpec = {

                    fadeIn(
                        animationSpec = tween(250, easing = androidx.compose.animation.core.EaseInOutCubic)
                    ) togetherWith fadeOut(
                        animationSpec = tween(200, easing = androidx.compose.animation.core.EaseInOutCubic)
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) { currentScreen ->

                when (currentScreen) {
                    "settings" -> SettingsScreen(
                        backdrop = backdrop, 
                        themeMode = themeMode,
                        onBackClick = { 
                            showSettingsScreen = false
                            selectedTabIndex = 3  
                        }
                    )
                    else -> {
                        val tabIndex = currentScreen as Int
                        when (tabIndex) {
                            0 -> HomeScreen(backdrop = backdrop, themeMode = themeMode)
                            1 -> AnalysisScreen(backdrop = backdrop, themeMode = themeMode)
                            2 -> PredictionScreen(backdrop = backdrop, themeMode = themeMode)
                            3 -> ProfileScreen(
                                backdrop = backdrop, 
                                themeMode = themeMode,
                                cardOpacity = cardOpacity, 
                                onNavigateToSettings = { showSettingsScreen = true }
                            )
                        }
                    }
                }
            }




            if (!showSettingsScreen) {
                LiquidBottomTabs(
                    selectedTabIndex = { selectedTabIndex },
                    onTabSelected = { 
                        selectedTabIndex = it
                    },
                    backdrop = backdrop,
                    themeMode = themeMode,
                    tabsCount = 4,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 36f.dp, vertical = 16f.dp)
                ) {

                LiquidBottomTab({ selectedTabIndex = 0 }) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "首页",
                        modifier = Modifier.size(24.dp),
                        tint = if (selectedTabIndex == 0) Color(0xFF0088FF) else Color.Black
                    )
                    BasicText(
                        "首页",
                        style = TextStyle(
                            if (selectedTabIndex == 0) Color(0xFF0088FF) else Color.Black, 
                            12f.sp
                        )
                    )
                }


                LiquidBottomTab({ selectedTabIndex = 1 }) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "分析",
                        modifier = Modifier.size(24.dp),
                        tint = if (selectedTabIndex == 1) Color(0xFF0088FF) else Color.Black
                    )
                    BasicText(
                        "分析",
                        style = TextStyle(
                            if (selectedTabIndex == 1) Color(0xFF0088FF) else Color.Black, 
                            12f.sp
                        )
                    )
                }


                LiquidBottomTab({ selectedTabIndex = 2 }) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "预测",
                        modifier = Modifier.size(24.dp),
                        tint = if (selectedTabIndex == 2) Color(0xFF0088FF) else Color.Black
                    )
                    BasicText(
                        "预测",
                        style = TextStyle(
                            if (selectedTabIndex == 2) Color(0xFF0088FF) else Color.Black, 
                            12f.sp
                        )
                    )
                }


                LiquidBottomTab({ selectedTabIndex = 3 }) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "我的",
                        modifier = Modifier.size(24.dp),
                        tint = if (selectedTabIndex == 3) Color(0xFF0088FF) else Color.Black
                    )
                    BasicText(
                        "我的",
                        style = TextStyle(
                            if (selectedTabIndex == 3) Color(0xFF0088FF) else Color.Black, 
                            12f.sp
                        )
                    )
                }
            }
            }
        }
    }
}