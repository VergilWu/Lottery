package com.vergil.lottery.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kyant.backdrop.Backdrop
import com.vergil.lottery.presentation.screens.analysis.AnalysisScreen
import com.vergil.lottery.presentation.screens.home.HomeScreen
import com.vergil.lottery.presentation.screens.prediction.PredictionScreen
import com.vergil.lottery.presentation.screens.profile.ProfileScreen
import com.vergil.lottery.core.constants.ThemeMode


@Composable
fun NavGraph(
    navController: NavHostController,
    backdrop: Backdrop,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavDestination.Home.route,
        modifier = modifier
    ) {
        composable(NavDestination.Home.route) {
            HomeScreen(backdrop = backdrop, themeMode = themeMode)
        }

        composable(NavDestination.Analysis.route) {
            AnalysisScreen(backdrop = backdrop, themeMode = themeMode)
        }

        composable(NavDestination.Prediction.route) {
            PredictionScreen(backdrop = backdrop, themeMode = themeMode)
        }

        composable(NavDestination.Profile.route) {
            ProfileScreen(backdrop = backdrop, themeMode = themeMode)
        }
    }
}
