package com.vergil.lottery.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.kyant.backdrop.Backdrop
import com.vergil.lottery.core.constants.ThemeMode
import com.vergil.lottery.presentation.screens.analysis.AnalysisScreen
import com.vergil.lottery.presentation.screens.home.HomeScreen
import com.vergil.lottery.presentation.screens.prediction.PredictionScreen
import com.vergil.lottery.presentation.screens.profile.ProfileScreen


@Composable
fun PreloadedPages(
    backdrop: Backdrop,
    themeMode: ThemeMode,
    selectedTabIndex: Int,
    modifier: Modifier = Modifier
) {

    Box(modifier = modifier.fillMaxSize()) {

        val homePageAlpha by animateFloatAsState(
            targetValue = if (selectedTabIndex == 0) 1f else 0f,
            animationSpec = tween(100),
            label = "home_alpha"
        )

        val analysisPageAlpha by animateFloatAsState(
            targetValue = if (selectedTabIndex == 1) 1f else 0f,
            animationSpec = tween(100),
            label = "analysis_alpha"
        )

        val predictionPageAlpha by animateFloatAsState(
            targetValue = if (selectedTabIndex == 2) 1f else 0f,
            animationSpec = tween(100),
            label = "prediction_alpha"
        )

        val profilePageAlpha by animateFloatAsState(
            targetValue = if (selectedTabIndex == 3) 1f else 0f,
            animationSpec = tween(100),
            label = "profile_alpha"
        )


        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(homePageAlpha)
        ) {
            if (homePageAlpha > 0f) {
                HomeScreen(backdrop = backdrop, themeMode = themeMode)
            }
        }


        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(analysisPageAlpha)
        ) {
            if (analysisPageAlpha > 0f) {
                AnalysisScreen(backdrop = backdrop, themeMode = themeMode)
            }
        }


        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(predictionPageAlpha)
        ) {
            if (predictionPageAlpha > 0f) {
                PredictionScreen(backdrop = backdrop, themeMode = themeMode)
            }
        }


        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(profilePageAlpha)
        ) {
            if (profilePageAlpha > 0f) {
                ProfileScreen(backdrop = backdrop, themeMode = themeMode)
            }
        }
    }
}
