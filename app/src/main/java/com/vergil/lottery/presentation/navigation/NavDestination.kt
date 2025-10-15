package com.vergil.lottery.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.ui.graphics.vector.ImageVector
import com.vergil.lottery.R


sealed class NavDestination(
    val route: String,
    val titleResId: Int,
    val icon: ImageVector
) {
    data object Home : NavDestination(
        route = "home",
        titleResId = R.string.nav_home,
        icon = Icons.Default.Home
    )

    data object Analysis : NavDestination(
        route = "analysis",
        titleResId = R.string.nav_analysis,
        icon = Icons.Default.Analytics
    )

    data object Prediction : NavDestination(
        route = "prediction",
        titleResId = R.string.nav_prediction,
        icon = Icons.Default.Psychology
    )

    data object Profile : NavDestination(
        route = "profile",
        titleResId = R.string.nav_profile,
        icon = Icons.Default.Person
    )

    companion object {
        val bottomNavItems = listOf(Home, Analysis, Prediction, Profile)
    }
}

