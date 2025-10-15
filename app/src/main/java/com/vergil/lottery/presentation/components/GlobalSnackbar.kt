package com.vergil.lottery.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.vergil.lottery.core.ui.ToastManager
import com.vergil.lottery.core.ui.handleMessage
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach


@Composable
fun GlobalSnackbarHost(
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        ToastManager.messages
            .onEach { message ->
                snackbarHostState.handleMessage(message)
            }
            .collect()
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier,
        snackbar = { snackbarData ->

            LiquidGlassSnackbar(backdrop = backdrop, snackbarData = snackbarData)
        }
    )
}


@Composable
private fun LiquidGlassSnackbar(
    backdrop: Backdrop,
    snackbarData: androidx.compose.material3.SnackbarData,
    modifier: Modifier = Modifier
) {
    BackdropLiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier.padding(16.dp)
    ) {
        Snackbar(
            snackbarData = snackbarData,
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
        )
    }
}

