package com.vergil.lottery.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.layout
import com.kyant.backdrop.Backdrop
import com.vergil.lottery.core.constants.ThemeMode
import com.vergil.lottery.presentation.screens.analysis.AnalysisScreen
import com.vergil.lottery.presentation.screens.home.HomeScreen
import com.vergil.lottery.presentation.screens.prediction.PredictionScreen
import com.vergil.lottery.presentation.screens.profile.ProfileScreen
import kotlinx.coroutines.delay


@Composable
fun PagePreloader(
    backdrop: Backdrop,
    themeMode: ThemeMode,
    selectedTabIndex: Int,
    modifier: Modifier = Modifier
) {

    var isHomePreloaded by remember { mutableStateOf(false) }
    var isAnalysisPreloaded by remember { mutableStateOf(false) }
    var isPredictionPreloaded by remember { mutableStateOf(false) }
    var isProfilePreloaded by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {

        isHomePreloaded = true
        isAnalysisPreloaded = true
        isPredictionPreloaded = true
        isProfilePreloaded = true
    }


    val homeAlpha by animateFloatAsState(
        targetValue = if (selectedTabIndex == 0) 1f else 0f,
        animationSpec = tween(400, easing = EaseInOutCubic), 
        label = "home_alpha"
    )

    val homeScale by animateFloatAsState(
        targetValue = if (selectedTabIndex == 0) 1f else 0.92f, 
        animationSpec = tween(400, easing = EaseInOutCubic),
        label = "home_scale"
    )

    val analysisAlpha by animateFloatAsState(
        targetValue = if (selectedTabIndex == 1) 1f else 0f,
        animationSpec = tween(400, easing = EaseInOutCubic),
        label = "analysis_alpha"
    )

    val analysisScale by animateFloatAsState(
        targetValue = if (selectedTabIndex == 1) 1f else 0.92f,
        animationSpec = tween(400, easing = EaseInOutCubic),
        label = "analysis_scale"
    )

    val predictionAlpha by animateFloatAsState(
        targetValue = if (selectedTabIndex == 2) 1f else 0f,
        animationSpec = tween(400, easing = EaseInOutCubic),
        label = "prediction_alpha"
    )

    val predictionScale by animateFloatAsState(
        targetValue = if (selectedTabIndex == 2) 1f else 0.92f,
        animationSpec = tween(400, easing = EaseInOutCubic),
        label = "prediction_scale"
    )

    val profileAlpha by animateFloatAsState(
        targetValue = if (selectedTabIndex == 3) 1f else 0f,
        animationSpec = tween(400, easing = EaseInOutCubic),
        label = "profile_alpha"
    )

    val profileScale by animateFloatAsState(
        targetValue = if (selectedTabIndex == 3) 1f else 0.92f,
        animationSpec = tween(400, easing = EaseInOutCubic),
        label = "profile_scale"
    )


    Box(modifier = modifier.fillMaxSize()) {

        if (selectedTabIndex == 0 || homeAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(homeAlpha)
                    .scale(homeScale)
            ) {
                HomeScreen(backdrop = backdrop, themeMode = themeMode)
            }
        }


        if (selectedTabIndex == 1 || analysisAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(analysisAlpha)
                    .scale(analysisScale)
            ) {
                AnalysisScreen(backdrop = backdrop, themeMode = themeMode)
            }
        }


        if (selectedTabIndex == 2 || predictionAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(predictionAlpha)
                    .scale(predictionScale)
            ) {
                PredictionScreen(backdrop = backdrop, themeMode = themeMode)
            }
        }


        if (selectedTabIndex == 3 || profileAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(profileAlpha)
                    .scale(profileScale)
            ) {
                ProfileScreen(backdrop = backdrop, themeMode = themeMode)
            }
        }
    }
}
