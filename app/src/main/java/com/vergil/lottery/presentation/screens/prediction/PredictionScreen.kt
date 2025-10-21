package com.vergil.lottery.presentation.screens.prediction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.vergil.lottery.presentation.components.LiquidGlassCard
import com.vergil.lottery.presentation.components.LiquidGlassTopAppBar
import com.vergil.lottery.presentation.components.LiquidButton
import com.vergil.lottery.presentation.components.LiquidSlider
import com.vergil.lottery.presentation.components.ShadowText
import com.vergil.lottery.core.constants.ThemeMode
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.capsule.ContinuousRoundedRectangle
import com.vergil.lottery.presentation.BackdropDemoScaffold
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vergil.lottery.core.constants.LotteryType
import com.vergil.lottery.di.AppModule
import com.vergil.lottery.presentation.theme.LotteryTheme
import kotlinx.coroutines.flow.collectLatest
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionScreen(
    backdrop: Backdrop,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier,
    viewModel: PredictionViewModel = viewModel {
        AppModule.createPredictionViewModel()
    }
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    var showAlgorithmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is PredictionContract.Effect.ShowToast -> {

                }
                is PredictionContract.Effect.ShowGenerateSuccess -> {

                }
                is PredictionContract.Effect.ScrollToResults -> {

                    listState.animateScrollToItem(4)
                }
                is PredictionContract.Effect.ShowAlgorithmExplanation -> {
                    showAlgorithmDialog = true
                }
            }
        }
    }


    if (state.isGenerating) {
        LiquidGlassProgressDialog(
            algorithmName = state.currentAlgorithm ?: "正在准备...",
            progress = state.algorithmProgress,
            themeMode = themeMode
        )
    }


    if (showAlgorithmDialog) {
        AlgorithmExplanationDialog(
            backdrop = backdrop,
            themeMode = themeMode,
            onDismiss = { showAlgorithmDialog = false }
        )
    }

    PredictionContent(
        backdrop = backdrop,
        themeMode = themeMode,
        modifier = modifier.fillMaxSize(),
        state = state,
        listState = listState,
        onLotteryTypeSelected = { type ->
            viewModel.handleIntent(PredictionContract.Intent.SelectLotteryType(type))
        },
        onModeSelected = { mode ->
            viewModel.handleIntent(PredictionContract.Intent.SelectMode(mode))
        },
        onAlgorithmToggle = { algorithm ->
            viewModel.handleIntent(PredictionContract.Intent.ToggleAlgorithm(algorithm))
        },
        onSelectAllAlgorithms = {
            viewModel.handleIntent(PredictionContract.Intent.SelectAllAlgorithms)
        },
        onDeselectAllAlgorithms = {
            viewModel.handleIntent(PredictionContract.Intent.DeselectAllAlgorithms)
        },
        onGenerate = {
            viewModel.handleIntent(PredictionContract.Intent.GeneratePrediction)
        },
        onShowAlgorithmExplanation = {
            viewModel.handleIntent(PredictionContract.Intent.ShowAlgorithmExplanation)
        },
        onGenerateComplex = {
            viewModel.handleIntent(PredictionContract.Intent.GenerateComplexPrediction)
        },
        onSetComplexPredictionCount = { count ->
            viewModel.handleIntent(PredictionContract.Intent.SetComplexPredictionCount(count))
        }
    )
}

@Composable
private fun PredictionContent(
    backdrop: Backdrop,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier,
    state: PredictionContract.State,
    listState: LazyListState,
    onLotteryTypeSelected: (LotteryType) -> Unit,
    onModeSelected: (PredictionContract.PredictionMode) -> Unit,
    onAlgorithmToggle: (PredictionContract.PredictionAlgorithm) -> Unit,
    onSelectAllAlgorithms: () -> Unit,
    onDeselectAllAlgorithms: () -> Unit,
    onGenerate: () -> Unit,
    onShowAlgorithmExplanation: () -> Unit,
    onGenerateComplex: () -> Unit,
    onSetComplexPredictionCount: (Int) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
            start = 16.dp,
            end = 16.dp,
            bottom = 72.dp + 48.dp + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item(key = "header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShadowText(
                    text = "智能预测",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                LiquidButton(
                    onClick = onShowAlgorithmExplanation,
                    backdrop = backdrop,
                    tint = Color(0xFF0088FF)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Help, contentDescription = "算法说明", tint = Color.White)
                }
            }
        }
        item {

            LotteryTypeSelector(
                backdrop = backdrop,
                selectedType = state.selectedType,
                onTypeSelected = onLotteryTypeSelected,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {

            PredictionModeSelector(
                backdrop = backdrop,
                selectedMode = state.selectedMode,
                onModeSelected = onModeSelected,
                themeMode = themeMode,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {

            AlgorithmCategorySelector(
                backdrop = backdrop,
                selectedAlgorithms = state.selectedAlgorithms,
                onAlgorithmToggle = onAlgorithmToggle,
                onSelectAllAlgorithms = onSelectAllAlgorithms,
                onDeselectAllAlgorithms = onDeselectAllAlgorithms,
                isCustomMode = state.selectedMode == PredictionContract.PredictionMode.CUSTOM,
                themeMode = themeMode,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GenerateButton(
                    backdrop = backdrop,
                    isLoading = state.isGenerating,
                    enabled = !state.isLoading && state.selectedAlgorithms.isNotEmpty(),
                    onClick = onGenerate,
                    modifier = Modifier.fillMaxWidth()
                )

                ComplexPredictionCountSlider(
                    backdrop = backdrop,
                    currentCount = state.complexPredictionCount,
                    onCountChanged = onSetComplexPredictionCount,
                    modifier = Modifier.fillMaxWidth()
                )

                ComplexPredictionButton(
                    backdrop = backdrop,
                    isLoading = state.isGenerating,
                    enabled = !state.isLoading && state.selectedAlgorithms.isNotEmpty(),
                    onClick = onGenerateComplex,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }


        when {
            state.isLoading -> {
                item {
                    LoadingIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
            state.error != null -> {
                item {
                    ErrorMessage(
                        message = state.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            state.predictions.isEmpty() && state.complexPredictions.isEmpty() -> {
                item {
                    EmptyState(modifier = Modifier.fillMaxWidth())
                }
            }
            else -> {
                if (state.predictions.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ShadowText(
                                text = "普通预测结果（共 ${state.predictions.size} 注）",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            LiquidButton(
                                onClick = { 
                                    copyPredictionsToClipboard(context, state.predictions)
                                },
                                backdrop = backdrop,
                                tint = Color(0xFF34C759) 
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "复制结果",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                ShadowText(
                                    text = "复制",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    items(state.predictions.size) { index ->
                        PredictionResultCard(backdrop = backdrop, prediction = state.predictions[index], themeMode = themeMode)
                    }
                }

                if (state.complexPredictions.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ShadowText(
                                text = "复式票预测结果",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            LiquidButton(
                                onClick = { 
                                    copyComplexPredictionsToClipboard(context, state.complexPredictions)
                                },
                                backdrop = backdrop,
                                tint = Color(0xFFFF6B35) 
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "复制复式票结果",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                ShadowText(
                                    text = "复制",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    items(state.complexPredictions.size) { index ->
                        ComplexPredictionResultCard(
                            backdrop = backdrop, 
                            prediction = state.complexPredictions[index], 
                            themeMode = themeMode
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PredictionModeSelector(
    backdrop: Backdrop,
    selectedMode: PredictionContract.PredictionMode,
    onModeSelected: (PredictionContract.PredictionMode) -> Unit,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier,
        themeMode = themeMode
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                ShadowText(
                    text = "预设模式",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                if (selectedMode != PredictionContract.PredictionMode.CUSTOM) {
                    ShadowText(
                        text = selectedMode.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

                Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PredictionContract.PredictionMode.entries.forEach { mode ->
                    val isSelected = mode == selectedMode
                    LiquidButton(
                        onClick = { onModeSelected(mode) },
                        backdrop = backdrop,
                        modifier = Modifier.height(40.dp),
                        tint = if (isSelected) Color(0xFF0088FF) else Color.Unspecified,
                        surfaceColor = if (isSelected) Color.Unspecified else Color.White.copy(0.3f)
                    ) {
                        ShadowText(
                            text = mode.displayName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isSelected) Color.White else Color.Black,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AlgorithmCategorySelector(
    backdrop: Backdrop,
    selectedAlgorithms: Set<PredictionContract.PredictionAlgorithm>,
    onAlgorithmToggle: (PredictionContract.PredictionAlgorithm) -> Unit,
    onSelectAllAlgorithms: () -> Unit,
    onDeselectAllAlgorithms: () -> Unit,
    isCustomMode: Boolean,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    var expandedCategory by remember { mutableStateOf<PredictionContract.AlgorithmCategory?>(null) }

    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier,
        themeMode = themeMode
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                ShadowText(
                    text = "算法选择",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                if (isCustomMode) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        LiquidButton(
                            onClick = onSelectAllAlgorithms,
                            backdrop = backdrop,
                            modifier = Modifier.height(32.dp),
                            tint = Color(0xFF4CAF50), 
                            surfaceColor = Color.White.copy(0.3f)
                        ) {
                            ShadowText(
                                text = "全选",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }


                        LiquidButton(
                            onClick = onDeselectAllAlgorithms,
                            backdrop = backdrop,
                            modifier = Modifier.height(32.dp),
                            tint = Color(0xFFFF5722), 
                            surfaceColor = Color.White.copy(0.3f)
                        ) {
                            ShadowText(
                                text = "全不选",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                } else {
                    ShadowText(
                        text = "预设模式（仅查看）",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))


            PredictionContract.AlgorithmCategory.entries.forEach { category ->
                val algorithms = PredictionContract.PredictionAlgorithm.getByCategory(category)
                val selectedCount = algorithms.count { it in selectedAlgorithms }
                val isExpanded = expandedCategory == category

                Column {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedCategory = if (isExpanded) null else category }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            ShadowText(
                                text = category.displayName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
                            )
                            ShadowText(
                                text = category.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ShadowText(
                                text = "$selectedCount/${algorithms.size}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            ShadowText(
                                text = if (isExpanded) "▲" else "▼",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }


                    AnimatedVisibility(visible = isExpanded) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
                        ) {
                            algorithms.forEach { algorithm ->
                                val isSelected = algorithm in selectedAlgorithms
                                LiquidButton(
                                    onClick = { if (isCustomMode) onAlgorithmToggle(algorithm) },
                                    backdrop = backdrop,
                                    modifier = Modifier.height(36.dp),
                                    tint = if (isSelected) Color(0xFF0088FF) else Color.Unspecified,
                                    surfaceColor = if (isSelected) Color.Unspecified else Color.White.copy(0.3f),
                                    isInteractive = isCustomMode
                                ) {
                                    ShadowText(
                                        text = algorithm.displayName,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isSelected) Color.White else Color.Black
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GenerateButton(
    backdrop: Backdrop,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidButton(
        onClick = onClick,
        backdrop = backdrop,
        modifier = modifier.height(56.dp),
        tint = if (enabled && !isLoading) Color(0xFF0088FF) else Color.Gray,
        isInteractive = enabled && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            ShadowText("生成中...", style = MaterialTheme.typography.bodyMedium)
        } else {
            ShadowText(
                text = "生成智能预测",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun ComplexPredictionCountSlider(
    backdrop: Backdrop,
    currentCount: Int,
    onCountChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier,
        themeMode = ThemeMode.LIGHT
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShadowText(
                    text = "复式票目标注数",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                ShadowText(
                    text = "${currentCount}注单式票",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LiquidSlider(
                value = { currentCount.toFloat() },
                onValueChange = { value -> 
                    val intValue = value.toInt().coerceIn(1, 100)
                    onCountChanged(intValue)
                },
                valueRange = 1f..100f,
                visibilityThreshold = 0.1f,
                backdrop = backdrop,
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { _: androidx.compose.ui.geometry.Offset -> 
                            },
                            onDragEnd = { 
                            },
                            onDrag = { change: androidx.compose.ui.input.pointer.PointerInputChange, _: androidx.compose.ui.geometry.Offset ->
                            }
                        )
                    }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "拖动滑块选择复式票目标注数，系统将自动计算需要的号码数量",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun ComplexPredictionButton(
    backdrop: Backdrop,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidButton(
        onClick = onClick,
        backdrop = backdrop,
        modifier = modifier.height(56.dp),
        tint = if (enabled && !isLoading) Color(0xFFFF6B35) else Color.Gray,
        isInteractive = enabled && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            ShadowText("复式票生成中...", style = MaterialTheme.typography.bodyMedium)
        } else {
            ShadowText(
                text = "生成复式票预测",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun ComplexPredictionResultCard(
    backdrop: Backdrop,
    prediction: PredictionContract.ComplexPredictionResult,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        themeMode = themeMode
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "复式票号码选择",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                prediction.redNumbers.forEach { number ->
                    LotteryBall(
                        number = number,
                        isBlue = false,
                        modifier = Modifier.size(36.dp)
                    )
                }


                if (prediction.blueNumbers.isNotEmpty()) {
                    BallDivider(size = 32.dp)
                }


                prediction.blueNumbers.forEach { number ->
                    LotteryBall(
                        number = number,
                        isBlue = true,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "复式票评分",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ScoreIndicator(
                    score = prediction.totalScore,
                    modifier = Modifier.width(120.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "实际组合: ${prediction.combinationCount}注",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "投注金额: ${prediction.combinationCount * 2}元",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))


            if (prediction.hotNumbers.isNotEmpty() || prediction.coldNumbers.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (prediction.hotNumbers.isNotEmpty()) {
                        Text(
                            text = "热号: ${prediction.hotNumbers.joinToString(",")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF5722)
                        )
                    }

                    if (prediction.coldNumbers.isNotEmpty()) {
                        Text(
                            text = "冷号: ${prediction.coldNumbers.joinToString(",")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2196F3)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }


            Text(
                text = prediction.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )


            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(
                        text = "算法分项评分",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    prediction.algorithmScores.forEach { (algorithm, score) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = algorithm.displayName,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "%.1f".format(score),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "复式票说明: 选择更多号码，可组合出多注单式票，提高中奖概率",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PredictionResultCard(
    backdrop: Backdrop,
    prediction: PredictionContract.PredictionResult,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        themeMode = themeMode
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                prediction.redNumbers.forEach { number ->
                    LotteryBall(
                        number = number,
                        isBlue = false,
                        modifier = Modifier.size(36.dp)
                    )
                }


                if (prediction.blueNumbers.isNotEmpty()) {
                    BallDivider(size = 32.dp)
                }


                prediction.blueNumbers.forEach { number ->
                    LotteryBall(
                        number = number,
                        isBlue = true,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "综合评分",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ScoreIndicator(
                    score = prediction.totalScore,
                    modifier = Modifier.width(120.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))


            Text(
                text = prediction.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )


            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(
                        text = "算法分项评分",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    prediction.algorithmScores.forEach { (algorithm, score) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = algorithm.displayName,
                                style = MaterialTheme.typography.bodySmall
                            )
        Text(
                                text = "%.1f".format(score),
            style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
        )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LotteryBall(
    number: String,
    isBlue: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                brush = if (isBlue) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2196F3),
                            Color(0xFF1976D2)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF44336),
                            Color(0xFFD32F2F)
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BallDivider(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ScoreIndicator(
    score: Float,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "score_animation"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LinearProgressIndicator(
            progress = { animatedScore },
            modifier = Modifier.weight(1f).height(8.dp),
            color = when {
                score >= 80 -> Color(0xFF4CAF50)
                score >= 60 -> Color(0xFFFFC107)
                else -> Color(0xFFFF9800)
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Text(
            text = "%.0f".format(score),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = when {
                score >= 80 -> Color(0xFF4CAF50)
                score >= 60 -> Color(0xFFFFC107)
                else -> Color(0xFFFF9800)
            }
        )
    }
}

@Composable
private fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "点击「生成智能预测」",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "基于历史数据和AI算法为您推荐号码",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun LotteryTypeSelector(
    backdrop: Backdrop,
    selectedType: LotteryType,
    onTypeSelected: (LotteryType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(LotteryType.entries) { type ->
            LotteryTypeChip(
                backdrop = backdrop,
                type = type,
                isSelected = type == selectedType,
                onClick = { onTypeSelected(type) }
            )
        }
    }
}

@Composable
private fun LotteryTypeChip(
    backdrop: Backdrop,
    type: LotteryType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidButton(
        onClick = onClick,
        backdrop = backdrop,
        modifier = modifier.height(40.dp),
        tint = if (isSelected) Color(0xFF0088FF) else Color.Unspecified,
        surfaceColor = if (isSelected) Color.Unspecified else Color.White.copy(0.3f)
    ) {
        ShadowText(
            text = type.displayName,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (isSelected) Color.White else Color.Black,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        )
    }
}


@Composable
private fun LiquidGlassProgressDialog(
    algorithmName: String,
    progress: Float,
    themeMode: ThemeMode,
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


    val validProgress = progress.takeIf { it.isFinite() && it >= 0f && it <= 1f } ?: 0f


    val animatedProgress by animateFloatAsState(
        targetValue = validProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "progress_animation"
    )

    Dialog(
        onDismissRequest = {  },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
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
                    .padding(40f.dp)
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { ContinuousRoundedRectangle(48f.dp) },
                        effects = {
                            colorControls(
                                brightness = if (isLightTheme) 0.2f else 0f,
                                saturation = 1.5f
                            )
                            blur(if (isLightTheme) 16f.dp.toPx() else 8f.dp.toPx())
                            lens(24f.dp.toPx(), 48f.dp.toPx(), depthEffect = true)
                        },
                        highlight = { Highlight.Plain },
                        onDrawSurface = { drawRect(containerColor) }
                    )
                    .fillMaxWidth()
            ) {

                ShadowText(
                    text = "智能预测生成中",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = contentColor,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(28f.dp, 24f.dp, 28f.dp, 12f.dp)
                )


                Column(
                    modifier = Modifier
                        .then(
                            if (isLightTheme) {
                                Modifier
                            } else {
                                Modifier.graphicsLayer(blendMode = BlendMode.Plus)
                            }
                        )
                        .padding(24f.dp, 12f.dp, 24f.dp, 12f.dp)
                ) {
                    ShadowText(
                        algorithmName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = contentColor.copy(0.8f),
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))


                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = accentColor,
                        trackColor = accentColor.copy(alpha = 0.2f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))


                    ShadowText(
                        text = if (animatedProgress < 1f) {
                            "正在分析历史数据... ${(animatedProgress * 100).toInt()}%"
                        } else {
                            "✓ 分析完成"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = contentColor.copy(0.68f)
                        )
                    )
                }


                Row(
                    Modifier
                        .padding(24f.dp, 12f.dp, 24f.dp, 24f.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16f.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    LiquidButton(
                        onClick = {  },
                        backdrop = backdrop,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        tint = Color.Unspecified,
                        surfaceColor = containerColor.copy(0.2f),
                        isInteractive = false
                    ) {
                        ShadowText(
                            "取消",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = contentColor.copy(0.5f)
                            )
                        )
                    }


                    LiquidButton(
                        onClick = {  },
                        backdrop = backdrop,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        tint = accentColor.copy(alpha = 0.7f),
                        isInteractive = false
                    ) {
                        ShadowText(
                            "生成中...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }
        }
    }
}


private fun copyPredictionsToClipboard(
    context: Context,
    predictions: List<PredictionContract.PredictionResult>
) {
    if (predictions.isEmpty()) return

    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val lotteryType = predictions.first().lotteryType

    val resultText = buildString {
        appendLine("🎯 ${lotteryType.displayName} 智能预测结果")
        appendLine("📅 生成时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}")
        appendLine("📊 共 ${predictions.size} 注推荐号码")
        appendLine()

        predictions.forEachIndexed { index, prediction ->
            appendLine("第 ${index + 1} 注: ")


            prediction.redNumbers.forEach { number ->
                append("$number ")
            }


            if (prediction.blueNumbers.isNotEmpty()) {
                append("| ")
                prediction.blueNumbers.forEach { number ->
                    append("$number ")
                }
            }


        }

        appendLine()
        appendLine("💡 温馨提示: 彩票有风险，投注需谨慎！")
    }

    val clip = android.content.ClipData.newPlainText("预测结果", resultText)
    clipboardManager.setPrimaryClip(clip)
}

private fun copyComplexPredictionsToClipboard(
    context: Context,
    predictions: List<PredictionContract.ComplexPredictionResult>
) {
    if (predictions.isEmpty()) return

    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val lotteryType = predictions.first().lotteryType

    val resultText = buildString {
        appendLine("🎯 ${lotteryType.displayName} 复式票预测结果")
        appendLine("📅 生成时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}")
        appendLine("📊 复式票推荐（可组合出多注单式票）")
        appendLine()

        predictions.forEachIndexed { index, prediction ->
            append("复式票 ${index + 1}: ")


            prediction.redNumbers.forEach { number ->
                append("$number ")
            }


            if (prediction.blueNumbers.isNotEmpty()) {
                append("| ")
                prediction.blueNumbers.forEach { number ->
                    append("$number ")
                }
            }

            appendLine(" (实际组合: ${prediction.combinationCount}注, 投注金额: ${prediction.combinationCount * 2}元)")
        }

        appendLine()
        appendLine("💡 复式票说明: 复式票选择更多号码，可组合出多注单式票，提高中奖概率，但投注金额也会相应增加")
        appendLine("💡 温馨提示: 彩票有风险，投注需谨慎！")
    }

    val clip = android.content.ClipData.newPlainText("复式票预测结果", resultText)
    clipboardManager.setPrimaryClip(clip)
}


@Composable
private fun AlgorithmExplanationDialog(
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
                    text = "算法说明",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = contentColor,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(24f.dp, 20f.dp, 24f.dp, 16f.dp)
                )


                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 24f.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PredictionContract.AlgorithmCategory.entries.forEach { category ->
                        item {
                            AlgorithmCategoryExplanation(
                                category = category,
                                algorithms = PredictionContract.PredictionAlgorithm.getByCategory(category),
                                contentColor = contentColor
                            )
                        }
                    }
                }


                Row(
                    Modifier
                        .padding(24f.dp, 16f.dp, 24f.dp, 20f.dp)
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


@Composable
private fun AlgorithmCategoryExplanation(
    category: PredictionContract.AlgorithmCategory,
    algorithms: List<PredictionContract.PredictionAlgorithm>,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {

        ShadowText(
            text = category.displayName,
            style = MaterialTheme.typography.titleMedium.copy(
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(4.dp))


        ShadowText(
            text = category.description,
            style = MaterialTheme.typography.bodySmall.copy(
                color = contentColor.copy(alpha = 0.7f)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))


        algorithms.forEach { algorithm ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 2.dp, bottom = 2.dp),
                verticalAlignment = Alignment.Top
            ) {

                ShadowText(
                    text = "• ${algorithm.displayName}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = contentColor,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.weight(1f)
                )
            }


            ShadowText(
                text = algorithm.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = contentColor.copy(alpha = 0.6f)
                ),
                modifier = Modifier.padding(start = 32.dp, top = 2.dp, bottom = 4.dp)
            )
        }
    }
}




private fun createMockPredictionResults(): List<PredictionContract.PredictionResult> {
    return listOf(
        PredictionContract.PredictionResult(
            id = "1",
            lotteryType = LotteryType.SSQ,
            redNumbers = listOf("03", "07", "12", "18", "25", "31"),
            blueNumbers = listOf("08"),
            totalScore = 85.5f,
            algorithmScores = mapOf(
                PredictionContract.PredictionAlgorithm.FREQUENCY to 88.0f,
                PredictionContract.PredictionAlgorithm.OMISSION to 82.0f,
                PredictionContract.PredictionAlgorithm.TREND to 86.0f
            ),
            confidence = 0.85f,
            explanation = "基于频率分析和遗漏补偿，该组合具有较高的中奖概率"
        ),
        PredictionContract.PredictionResult(
            id = "2",
            lotteryType = LotteryType.SSQ,
            redNumbers = listOf("01", "09", "15", "22", "28", "33"),
            blueNumbers = listOf("12"),
            totalScore = 78.2f,
            algorithmScores = mapOf(
                PredictionContract.PredictionAlgorithm.FREQUENCY to 75.0f,
                PredictionContract.PredictionAlgorithm.OMISSION to 81.0f,
                PredictionContract.PredictionAlgorithm.TREND to 78.5f
            ),
            confidence = 0.78f,
            explanation = "马尔可夫链分析显示该组合具有较好的状态转移概率"
        ),
        PredictionContract.PredictionResult(
            id = "3",
            lotteryType = LotteryType.SSQ,
            redNumbers = listOf("05", "11", "17", "24", "29", "32"),
            blueNumbers = listOf("06"),
            totalScore = 82.1f,
            algorithmScores = mapOf(
                PredictionContract.PredictionAlgorithm.FREQUENCY to 80.0f,
                PredictionContract.PredictionAlgorithm.OMISSION to 84.0f,
                PredictionContract.PredictionAlgorithm.TREND to 82.3f
            ),
            confidence = 0.82f,
            explanation = "综合多种算法分析，该组合在奇偶比和区间分布上表现均衡"
        )
    )
}

private fun createMockPredictionState(): PredictionContract.State {
    return PredictionContract.State(
        selectedType = LotteryType.SSQ,
        isLoading = false,
        isGenerating = false,
        currentAlgorithm = null,
        algorithmProgress = 0f,
        predictions = createMockPredictionResults(),
        selectedMode = PredictionContract.PredictionMode.COMPREHENSIVE,
        selectedAlgorithms = PredictionContract.PredictionMode.COMPREHENSIVE.algorithms,
        predictionCount = 5,
        error = null
    )
}

@Preview(name = "预测页 - 默认状态", showBackground = true)
@Composable
private fun PredictionScreenDefaultPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        val mockState = createMockPredictionState()
        val listState = rememberLazyListState()
        PredictionContent(
            backdrop = mockBackdrop,
            themeMode = ThemeMode.LIGHT,
            modifier = Modifier.fillMaxSize(),
            state = mockState,
            listState = listState,
            onLotteryTypeSelected = {},
            onModeSelected = {},
            onAlgorithmToggle = {},
            onSelectAllAlgorithms = {},
            onDeselectAllAlgorithms = {},
            onGenerate = {},
            onShowAlgorithmExplanation = {},
            onGenerateComplex = {},
            onSetComplexPredictionCount = {}
        )
    }
}

@Preview(name = "预测页 - 深色模式", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PredictionScreenDarkPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        val mockState = createMockPredictionState()
        val listState = rememberLazyListState()
        PredictionContent(
            backdrop = mockBackdrop,
            themeMode = ThemeMode.DARK,
            modifier = Modifier.fillMaxSize(),
            state = mockState,
            listState = listState,
            onLotteryTypeSelected = {},
            onModeSelected = {},
            onAlgorithmToggle = {},
            onSelectAllAlgorithms = {},
            onDeselectAllAlgorithms = {},
            onGenerate = {},
            onShowAlgorithmExplanation = {},
            onGenerateComplex = {},
            onSetComplexPredictionCount = {}
        )
    }
}

@Preview(name = "预测页 - 生成中", showBackground = true)
@Composable
private fun PredictionScreenGeneratingPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        val generatingState = PredictionContract.State(
            selectedType = LotteryType.SSQ,
            isLoading = false,
            isGenerating = true,
            currentAlgorithm = "频率权重法",
            algorithmProgress = 0.6f,
            predictions = emptyList(),
            selectedMode = PredictionContract.PredictionMode.COMPREHENSIVE,
            selectedAlgorithms = PredictionContract.PredictionMode.COMPREHENSIVE.algorithms,
            predictionCount = 5,
            error = null
        )
        val listState = rememberLazyListState()
        PredictionContent(
            backdrop = mockBackdrop,
            themeMode = ThemeMode.LIGHT,
            modifier = Modifier.fillMaxSize(),
            state = generatingState,
            listState = listState,
            onLotteryTypeSelected = {},
            onModeSelected = {},
            onAlgorithmToggle = {},
            onSelectAllAlgorithms = {},
            onDeselectAllAlgorithms = {},
            onGenerate = {},
            onShowAlgorithmExplanation = {},
            onGenerateComplex = {},
            onSetComplexPredictionCount = {}
        )
    }
}

@Preview(name = "预测页 - 错误状态", showBackground = true)
@Composable
private fun PredictionScreenErrorPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        val errorState = PredictionContract.State(
            selectedType = LotteryType.SSQ,
            isLoading = false,
            isGenerating = false,
            currentAlgorithm = null,
            algorithmProgress = 0f,
            predictions = emptyList(),
            selectedMode = PredictionContract.PredictionMode.COMPREHENSIVE,
            selectedAlgorithms = PredictionContract.PredictionMode.COMPREHENSIVE.algorithms,
            predictionCount = 5,
            error = "历史数据不足，无法生成预测"
        )
        val listState = rememberLazyListState()
        PredictionContent(
            backdrop = mockBackdrop,
            themeMode = ThemeMode.LIGHT,
            modifier = Modifier.fillMaxSize(),
            state = errorState,
            listState = listState,
            onLotteryTypeSelected = {},
            onModeSelected = {},
            onAlgorithmToggle = {},
            onSelectAllAlgorithms = {},
            onDeselectAllAlgorithms = {},
            onGenerate = {},
            onShowAlgorithmExplanation = {},
            onGenerateComplex = {},
            onSetComplexPredictionCount = {}
        )
    }
}

@Preview(name = "预测结果卡片", showBackground = true)
@Composable
private fun PredictionResultCardPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        val mockResult = createMockPredictionResults().first()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "预测结果: ${mockResult.redNumbers.joinToString(", ")} + ${mockResult.blueNumbers.joinToString(", ")}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
