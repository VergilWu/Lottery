package com.vergil.lottery.presentation.screens.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.vergil.lottery.R
import com.vergil.lottery.core.constants.LotteryType
import com.vergil.lottery.domain.model.DrawResult
import com.vergil.lottery.presentation.components.LiquidGlassCard
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.capsule.ContinuousRoundedRectangle
import com.vergil.lottery.presentation.components.LiquidButton
import com.vergil.lottery.presentation.components.ShadowText
import com.vergil.lottery.presentation.theme.LotteryTheme
import com.vergil.lottery.core.constants.ThemeMode
import com.vergil.lottery.presentation.BackdropDemoScaffold
import kotlinx.coroutines.flow.collectLatest


@Composable
fun AnalysisScreen(
    backdrop: Backdrop,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier,
    viewModel: AnalysisViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()


    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AnalysisContract.Effect.ShowToast -> {

                }
                is AnalysisContract.Effect.NavigateToDetail -> {

                }
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
            start = 16.dp,
            end = 16.dp,
            bottom = 72.dp + 48.dp + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),

        userScrollEnabled = true,
        reverseLayout = false
    ) {

        item(key = "header") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShadowText(
                text = stringResource(R.string.screen_analysis_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            LiquidButton(
                onClick = { viewModel.sendIntent(AnalysisContract.Intent.RefreshHistory) },
                backdrop = backdrop,
                tint = Color(0xFF0088FF)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "刷新", tint = Color.White)
            }
        }
        }


        item(key = "lottery_selector") {
        LotteryTypeSelector(
            backdrop = backdrop,
            selectedType = state.selectedType,
            onTypeSelected = { viewModel.sendIntent(AnalysisContract.Intent.SelectLotteryType(it)) }
        )
        }


        item(key = "dimension_selector") {
            AnalysisDimensionSelector(
                backdrop = backdrop,
                selectedDimension = state.selectedDimension,
                onDimensionSelected = { viewModel.sendIntent(AnalysisContract.Intent.SelectDimension(it)) }
            )
        }


        if (state.selectedDimension != AnalysisContract.AnalysisDimension.HISTORY) {
            item(key = "ball_type_selector") {
                BallTypeSelector(
                    backdrop = backdrop,
                    selectedBallType = state.selectedBallType,
                    onBallTypeSelected = { viewModel.sendIntent(AnalysisContract.Intent.SelectBallType(it)) }
                )
            }
        }


        when (state.selectedDimension) {
            AnalysisContract.AnalysisDimension.HISTORY -> {

        when {
            state.isLoading && state.historyList.isEmpty() -> {
                        item(key = "skeleton") {
                SkeletonListView(backdrop = backdrop, themeMode = themeMode)
                        }
                    }

                    state.error != null && state.historyList.isEmpty() -> {
                        item(key = "error") {
                            ErrorView(message = state.error!!)
                        }
                    }

                    state.historyList.isNotEmpty() -> {
                        items(
                            items = state.historyList,
                            key = { "${it.code}_${it.issue}" } 
                        ) { history ->
                            OptimizedHistoryItemCard(backdrop = backdrop, drawResult = history, themeMode = themeMode)
                        }

                        if (state.isLoadingMore) {
                            item(key = "loading_more") {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        if (!state.hasMore) {
                            item(key = "no_more") {
                                Text(
                                    text = "没有更多数据了",
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    else -> {
                        item(key = "empty") {
                            EmptyView()
                        }
                    }
                }
            }


            AnalysisContract.AnalysisDimension.OMISSION -> {
                if (state.omissionData.isNotEmpty()) {

                    item(key = "omission_chart") {
                        com.vergil.lottery.presentation.components.charts.ChartCard(
                            title = "遗漏分析",
                            subtitle = "号码未出现的连续期数分布",
                            backdrop = backdrop,
                            themeMode = themeMode
                        ) {
                            com.vergil.lottery.presentation.components.charts.SimpleBarChart(
                                title = "号码遗漏期数 (${if (state.selectedBallType == AnalysisContract.BallType.RED) "红球" else "蓝球"})",
                                data = state.omissionData
                                    .sortedByDescending { it.currentOmission }
                                    .take(20) 
                                    .map { 
                                        com.vergil.lottery.presentation.components.charts.ChartDataPoint(
                                            label = it.number,
                                            value = it.currentOmission
                                        )
                                    },
                                themeMode = themeMode
                            )
                        }
                    }


                    items(items = state.omissionData, key = { it.number }) { data ->
                        OmissionDataCard(backdrop = backdrop, data = data, themeMode = themeMode)
                    }
                }
            }

            AnalysisContract.AnalysisDimension.FREQUENCY -> {
                if (state.frequencyData.isNotEmpty()) {

                    item(key = "frequency_chart") {
                        com.vergil.lottery.presentation.components.charts.ChartCard(
                            title = "频率分析",
                            subtitle = "号码出现次数统计",
                            backdrop = backdrop,
                            themeMode = themeMode
                        ) {
                            com.vergil.lottery.presentation.components.charts.SimpleBarChart(
                                title = "号码出现频率",
                                data = state.frequencyData
                                    .sortedByDescending { it.frequency }
                                    .take(20) 
                                    .map { 
                                        com.vergil.lottery.presentation.components.charts.ChartDataPoint(
                                            label = it.number,
                                            value = it.frequency.toInt()
                                        )
                                    },
                                themeMode = themeMode
                            )
                        }
                    }


                    items(items = state.frequencyData, key = { it.number }) { data ->
                        FrequencyDataCard(backdrop = backdrop, data = data, themeMode = themeMode)
                    }
                }
            }

            AnalysisContract.AnalysisDimension.HOT_COLD -> {
                if (state.hotColdData.isNotEmpty()) {

                    item(key = "hot_cold_chart") {
                        com.vergil.lottery.presentation.components.charts.ChartCard(
                            title = "冷热号分析",
                            subtitle = "基于近期数据的冷热程度分布",
                            backdrop = backdrop,
                            themeMode = themeMode
                        ) {
                            com.vergil.lottery.presentation.components.charts.SimpleBarChart(
                                title = "冷热号分布",
                                data = state.hotColdData
                                    .sortedByDescending { it.temperature }
                                    .take(20) 
                                    .map { 
                                        com.vergil.lottery.presentation.components.charts.ChartDataPoint(
                                            label = it.number,
                                            value = it.temperature.toInt()
                                        )
                                    },
                                themeMode = themeMode
                            )
                        }
                    }


                    items(items = state.hotColdData, key = { it.number }) { data ->
                        HotColdDataCard(backdrop = backdrop, data = data, themeMode = themeMode)
                    }
                }
            }

            AnalysisContract.AnalysisDimension.CONSECUTIVE -> {
                if (state.consecutiveData.isNotEmpty()) {
                    items(items = state.consecutiveData, key = { it.numbers.joinToString() }) { data ->
                        ConsecutiveDataCard(backdrop = backdrop, data = data, themeMode = themeMode)
                    }
                }
            }

            AnalysisContract.AnalysisDimension.SAME_TAIL -> {
                if (state.sameTailData.isNotEmpty()) {
                    items(items = state.sameTailData, key = { it.tail }) { data ->
                        SameTailDataCard(backdrop = backdrop, data = data, themeMode = themeMode)
                    }
                }
            }

            AnalysisContract.AnalysisDimension.SUM_VALUE -> {
                if (state.sumValueData.isNotEmpty()) {

                    item(key = "sum_value_chart") {
                        com.vergil.lottery.presentation.components.charts.ChartCard(
                            title = "和值分析",
                            subtitle = "开奖号码和值分布趋势",
                            backdrop = backdrop,
                            themeMode = themeMode
                        ) {
                            com.vergil.lottery.presentation.components.charts.SimpleBarChart(
                                title = "和值分布",
                                data = state.sumValueData
                                    .sortedBy { it.sumValue }
                                    .take(15) 
                                    .map { 
                                        com.vergil.lottery.presentation.components.charts.ChartDataPoint(
                                            label = it.sumValue.toString(),
                                            value = it.count
                                        )
                                    },
                                themeMode = themeMode
                            )
                        }
                    }


                    items(items = state.sumValueData, key = { it.sumValue }) { data ->
                        SumValueDataCard(backdrop = backdrop, data = data, themeMode = themeMode)
                    }
                }
            }

            AnalysisContract.AnalysisDimension.SPAN -> {
                if (state.spanData.isNotEmpty()) {
                    items(items = state.spanData, key = { it.span }) { data ->
                        SpanDataCard(backdrop = backdrop, data = data, themeMode = themeMode)
                    }
                }
            }

            AnalysisContract.AnalysisDimension.AC_VALUE -> {
                if (state.acValueData.isNotEmpty()) {
                    items(items = state.acValueData, key = { it.acValue }) { data ->
                        ACValueDataCard(backdrop = backdrop, data = data, themeMode = themeMode)
                    }
                }
            }

            AnalysisContract.AnalysisDimension.ODD_EVEN -> {
                if (state.oddEvenData.isNotEmpty()) {

                    item(key = "odd_even_chart") {
                        com.vergil.lottery.presentation.components.charts.ChartCard(
                            title = "奇偶比分析",
                            subtitle = "奇数偶数比例分布",
                            backdrop = backdrop,
                            themeMode = themeMode
                        ) {
                            com.vergil.lottery.presentation.components.charts.SimplePieChart(
                                title = "奇偶比例分布",
                                data = state.oddEvenData
                                    .sortedByDescending { it.count }
                                    .take(8) 
                                    .map { 
                                        com.vergil.lottery.presentation.components.charts.ChartDataPoint(
                                            label = it.ratio,
                                            value = it.count
                                        )
                                    },
                                themeMode = themeMode
                            )
                        }
                    }


                    items(items = state.oddEvenData, key = { it.ratio }) { data ->
                        RatioDataCard(backdrop = backdrop, data = data, title = "奇偶比", themeMode = themeMode)
                    }
                }
            }

            AnalysisContract.AnalysisDimension.SIZE_RATIO -> {
                if (state.sizeRatioData.isNotEmpty()) {
                    items(items = state.sizeRatioData, key = { it.ratio }) { data ->
                        RatioDataCard(backdrop = backdrop, data = data, title = "大小比", themeMode = themeMode)
                    }
                }
            }

            AnalysisContract.AnalysisDimension.PRIME_COMPOSITE -> {
                if (state.primeCompositeData.isNotEmpty()) {
                    items(items = state.primeCompositeData, key = { it.ratio }) { data ->
                        RatioDataCard(backdrop = backdrop, data = data, title = "质合比", themeMode = themeMode)
                    }
                }
            }

            AnalysisContract.AnalysisDimension.ZONE -> {
                if (state.zoneData.isNotEmpty()) {
                    items(items = state.zoneData, key = { it.zone }) { data ->
                        ZoneDataCard(backdrop = backdrop, data = data, themeMode = themeMode)
                    }
                }
            }
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
private fun HistoryList(
    backdrop: Backdrop,
    historyList: List<DrawResult>,
    isRefreshing: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {

    val listState = rememberLazyListState()


    val shouldLoadMore by remember {
        derivedStateOf {

            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            val totalItems = listState.layoutInfo.totalItemsCount


            lastVisibleIndex >= totalItems - 3 && hasMore && !isLoadingMore && !isRefreshing
        }
    }


    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)  
    ) {

        if (isRefreshing && historyList.isEmpty()) {
            item(key = "refreshing_indicator") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }


        items(
            items = historyList,
            key = { drawResult -> "${drawResult.code}_${drawResult.issue}" }  
        ) { drawResult ->
            HistoryItemCard(backdrop = backdrop, drawResult = drawResult, themeMode = themeMode)
        }


        if (isLoadingMore) {
            item(key = "loading_more_indicator") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "加载中...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }


        if (!hasMore && historyList.isNotEmpty()) {
            item(key = "no_more_data") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "已加载全部数据",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    backdrop: Backdrop,
    drawResult: DrawResult,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {

    val issueText = remember(drawResult.issue) { "第 ${drawResult.issue} 期" }
    val dateText = remember(drawResult.drawDate) { drawResult.drawDate }

    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier.fillMaxWidth(),
        themeMode = themeMode
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = issueText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))


            NumberBallsRow(
                redNumbers = drawResult.red,
                blueNumbers = drawResult.blue
            )
        }
    }
}

@Composable
private fun NumberBallsRow(
    redNumbers: List<String>,
    blueNumbers: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {

        redNumbers.forEach { number ->
            SmallLotteryBall(
                number = number,
                color = Color(0xFFE53935)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        if (blueNumbers.isNotEmpty()) {

            SmallBallDivider(
                modifier = Modifier.padding(horizontal = 4.dp)
            )


            blueNumbers.forEach { number ->
                SmallLotteryBall(
                    number = number,
                    color = Color(0xFF1E88E5)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
        }
    }
}


@Composable
private fun SmallBallDivider(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SmallLotteryBall(
    number: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SkeletonListView(backdrop: Backdrop, themeMode: ThemeMode, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        repeat(3) {
            SkeletonCard(backdrop = backdrop, themeMode = themeMode)
        }
    }
}

@Composable
private fun SkeletonCard(backdrop: Backdrop, themeMode: ThemeMode, modifier: Modifier = Modifier) {
    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier.fillMaxWidth(),
        themeMode = themeMode
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                repeat(6) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
        }
    }
}

@Composable
private fun LoadingView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorView(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun EmptyView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "暂无历史数据",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}




private fun createMockHistoryData(): List<DrawResult> {
    return listOf(
        DrawResult(
            type = "福彩",
            name = "双色球",
            code = "ssq",
            issue = "2024001",
            red = listOf("03", "07", "12", "18", "25", "31"),
            blue = listOf("08"),
            drawDate = "2024-01-02",
            timeRule = "每周二、四、日开奖",
            saleMoney = "3.5亿元",
            prizePool = "8.2亿元",
            winnerDetail = null
        ),
        DrawResult(
            type = "福彩",
            name = "双色球",
            code = "ssq",
            issue = "2024002",
            red = listOf("01", "09", "15", "22", "28", "33"),
            blue = listOf("12"),
            drawDate = "2024-01-04",
            timeRule = "每周二、四、日开奖",
            saleMoney = "3.2亿元",
            prizePool = "8.5亿元",
            winnerDetail = null
        ),
        DrawResult(
            type = "福彩",
            name = "双色球",
            code = "ssq",
            issue = "2024003",
            red = listOf("05", "11", "17", "24", "29", "32"),
            blue = listOf("06"),
            drawDate = "2024-01-07",
            timeRule = "每周二、四、日开奖",
            saleMoney = "3.8亿元",
            prizePool = "8.8亿元",
            winnerDetail = null
        )
    )
}

private fun createMockAnalysisState(): AnalysisContract.State {
    return AnalysisContract.State(
        selectedType = LotteryType.SSQ,
        selectedDimension = AnalysisContract.AnalysisDimension.HISTORY,
        selectedBallType = AnalysisContract.BallType.RED,
        historyList = createMockHistoryData(),
        isLoading = false,
        isLoadingMore = false,
        hasMore = true,
        currentPage = 0,
        pageSize = 20,
        error = null
    )
}

@Preview(name = "数据分析 - 历史记录", showBackground = false)
@Composable
private fun AnalysisScreenHistoryPreview() {
    LotteryTheme {
        BackdropDemoScaffold(
            initialPainterResId = R.drawable.wallpaper_light
        ) { backdrop ->
            val mockState = createMockAnalysisState()
            AnalysisContent(
                backdrop = backdrop,
                state = mockState,
                viewModel = null,
                themeMode = ThemeMode.LIGHT
            )
        }
    }
}

@Preview(name = "数据分析 - 深色模式", showBackground = false, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AnalysisScreenDarkPreview() {
    LotteryTheme {
        BackdropDemoScaffold(
            initialPainterResId = R.drawable.wallpaper_light
        ) { backdrop ->
            val mockState = createMockAnalysisState()
            AnalysisContent(
                backdrop = backdrop,
                state = mockState,
                viewModel = null,
                themeMode = ThemeMode.DARK
            )
        }
    }
}

@Preview(name = "数据分析 - 加载中", showBackground = true)
@Composable
private fun AnalysisScreenLoadingPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        val loadingState = AnalysisContract.State(
            selectedType = LotteryType.SSQ,
            selectedDimension = AnalysisContract.AnalysisDimension.HISTORY,
            selectedBallType = AnalysisContract.BallType.RED,
            historyList = emptyList(),
            isLoading = true,
            isLoadingMore = false,
            hasMore = true,
            currentPage = 0,
            pageSize = 20,
            error = null
        )
        AnalysisContent(
            backdrop = mockBackdrop,
            state = loadingState,
            viewModel = null,
            themeMode = ThemeMode.LIGHT
        )
    }
}

@Preview(name = "数据分析 - 错误状态", showBackground = true)
@Composable
private fun AnalysisScreenErrorPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        val errorState = AnalysisContract.State(
            selectedType = LotteryType.SSQ,
            selectedDimension = AnalysisContract.AnalysisDimension.HISTORY,
            selectedBallType = AnalysisContract.BallType.RED,
            historyList = emptyList(),
            isLoading = false,
            isLoadingMore = false,
            hasMore = true,
            currentPage = 0,
            pageSize = 20,
            error = "网络连接失败，请检查网络设置"
        )
        AnalysisContent(
            backdrop = mockBackdrop,
            state = errorState,
            viewModel = null,
            themeMode = ThemeMode.LIGHT
        )
    }
}

@Preview(name = "历史记录卡片", showBackground = true)
@Composable
private fun HistoryItemCardPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        val mockDrawResult = createMockHistoryData().first()
        HistoryItemCard(
            backdrop = mockBackdrop,
            drawResult = mockDrawResult,
            themeMode = ThemeMode.LIGHT
        )
    }
}

@Preview(name = "彩种选择器", showBackground = true)
@Composable
private fun LotteryTypeSelectorPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        LotteryTypeSelector(
            backdrop = mockBackdrop,
            selectedType = LotteryType.SSQ,
            onTypeSelected = {}
        )
    }
}

@Preview(name = "骨架卡片", showBackground = true)
@Composable
private fun SkeletonCardPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        SkeletonCard(
            backdrop = mockBackdrop,
            themeMode = ThemeMode.LIGHT
        )
    }
}


@Composable
private fun AnalysisContent(
    backdrop: Backdrop,
    state: AnalysisContract.State,
    viewModel: AnalysisViewModel?,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    LazyColumn(
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
                    text = "数据分析",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                LiquidButton(
                    onClick = { viewModel?.sendIntent(AnalysisContract.Intent.RefreshHistory) },
                    backdrop = backdrop,
                    tint = Color(0xFF0088FF)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新", tint = Color.White)
                }
            }
        }


        item(key = "lottery_selector") {
            LotteryTypeSelector(
                backdrop = backdrop,
                selectedType = state.selectedType,
                onTypeSelected = { viewModel?.sendIntent(AnalysisContract.Intent.SelectLotteryType(it)) }
            )
        }


        when {
            state.isLoading && state.historyList.isEmpty() -> {
                item(key = "skeleton") {
                    SkeletonListView(backdrop = backdrop, themeMode = themeMode)
                }
            }

            state.error != null && state.historyList.isEmpty() -> {
                item(key = "error") {
                    ErrorView(message = state.error!!)
                }
            }

            state.historyList.isNotEmpty() -> {
                items(
                    items = state.historyList,
                    key = { "${it.code}_${it.issue}" }
                ) { history ->
                    HistoryItemCard(backdrop = backdrop, drawResult = history, themeMode = themeMode)
                }

                if (state.isLoadingMore) {
                    item(key = "loading_more") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                if (!state.hasMore) {
                    item(key = "no_more") {
                        Text(
                            text = "没有更多数据了",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            else -> {
                item(key = "empty") {
                    EmptyView()
                }
            }
        }
    }
}


@Composable
private fun OptimizedHistoryItemCard(
    backdrop: Backdrop,
    drawResult: DrawResult,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    val isLightTheme = themeMode == ThemeMode.LIGHT || (themeMode == ThemeMode.SYSTEM && !isSystemInDarkTheme())

    Box(
        modifier = modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { ContinuousRoundedRectangle(24f.dp) },
                effects = {
                    vibrancy()
                    lens(12f.dp.toPx(), 24f.dp.toPx())
                }
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "第 ${drawResult.issue} 期",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isLightTheme) Color.Black else Color.White
                )
                Text(
                    text = drawResult.drawDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isLightTheme) Color.Black.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.7f)
                )
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                drawResult.red.forEach { number ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53E3E))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = number,
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }


                if (drawResult.blue.isNotEmpty()) {
                    Text(
                        text = "|",
                        color = if (isLightTheme) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }


                drawResult.blue.forEach { number ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3182CE))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = number,
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
