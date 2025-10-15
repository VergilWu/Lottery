package com.vergil.lottery.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vergil.lottery.R
import com.vergil.lottery.core.constants.LotteryType
import com.vergil.lottery.domain.model.DrawResult
import com.vergil.lottery.domain.model.WinnerDetail
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
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
fun HomeScreen(
    backdrop: Backdrop,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()


    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is HomeContract.Effect.ShowToast -> {

                }
                is HomeContract.Effect.NavigateToHistory -> {

                }
                is HomeContract.Effect.ShowVerificationSuccess -> {

                }
            }
        }
    }

    HomeContent(
        backdrop = backdrop,
        drawResults = state.allDraws,
        isLoading = state.isLoading,
        error = state.error,
        onRefresh = { viewModel.sendIntent(HomeContract.Intent.RefreshAllDraws) },
        themeMode = themeMode,
        modifier = modifier
    )
}


@Composable
private fun HomeContent(
    backdrop: Backdrop,
    drawResults: Map<LotteryType, DrawResult>,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
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
                    text = stringResource(R.string.screen_home_title),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                LiquidButton(
                    onClick = onRefresh,
                    backdrop = backdrop,
                    tint = Color(0xFF0088FF)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新", tint = Color.White)
                }
            }
        }


        if (isLoading && drawResults.isEmpty()) {
            item(key = "loading") {
                LoadingCard(backdrop = backdrop, themeMode = themeMode)
            }
        }


        if (error != null && drawResults.isEmpty()) {
            item(key = "error") {
                ErrorCard(backdrop = backdrop, message = error, themeMode = themeMode)
            }
        }


        items(
            items = LotteryType.entries.filter { drawResults[it] != null },
            key = { it.code }
        ) { lotteryType ->
            val drawResult = drawResults[lotteryType]!!
            DrawResultCard(
                backdrop = backdrop,
                drawResult = drawResult,
                lotteryType = lotteryType,
                themeMode = themeMode
            )
        }
    }
}

@Composable
private fun DrawResultCard(
    backdrop: Backdrop,
    drawResult: DrawResult,
    lotteryType: LotteryType,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {

    val isSystemDark = isSystemInDarkTheme()
    val isLightTheme = remember(themeMode, isSystemDark) { 
        themeMode == ThemeMode.LIGHT || (themeMode == ThemeMode.SYSTEM && !isSystemDark) 
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
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
            modifier = Modifier.fillMaxWidth()
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShadowText(
                    text = drawResult.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                ShadowText(
                    text = "第 ${drawResult.issue} 期",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

        Spacer(modifier = Modifier.height(8.dp))


        ShadowText(
            text = drawResult.drawDate,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))


        DrawNumbersFlowRow(drawResult = drawResult)


        if (!drawResult.prizePool.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            ShadowText(
                text = "奖池: ${drawResult.prizePool}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DrawNumbersFlowRow(
    drawResult: DrawResult,
    modifier: Modifier = Modifier
) {

    val isKL8 = drawResult.code == "kl8"
    val totalNumbers = drawResult.red.size + drawResult.blue.size


    val ballSize = when {
        totalNumbers > 15 -> 28.dp  
        totalNumbers > 10 -> 28.dp  
        else -> 28.dp              
    }

    val spacing = when {
        totalNumbers > 15 -> 6.dp
        else -> 8.dp
    }

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(if (isKL8) 12.dp else 0.dp),  
        maxItemsInEachRow = if (isKL8) 10 else 100  
    ) {

        drawResult.red.forEachIndexed { index, number ->
            LotteryBall(
                number = number,
                color = Color(0xFFE53935),
                size = ballSize
            )
            if (index < drawResult.red.size - 1 || drawResult.blue.isNotEmpty()) {
                Spacer(modifier = Modifier.width(spacing))
            }
        }

        if (drawResult.blue.isNotEmpty()) {

            BallDivider(
                modifier = Modifier.padding(horizontal = spacing)
            )


            drawResult.blue.forEachIndexed { index, number ->
                LotteryBall(
                    number = number,
                    color = Color(0xFF1E88E5),
                    size = ballSize
                )
                if (index < drawResult.blue.size - 1) {
                    Spacer(modifier = Modifier.width(spacing))
                }
            }
        }
    }
}


@Composable
private fun BallDivider(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
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
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LotteryBall(
    number: String,
    color: Color,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            color = Color.White,
            style = when {
                size < 40.dp -> MaterialTheme.typography.bodyMedium
                size < 45.dp -> MaterialTheme.typography.bodyLarge
                else -> MaterialTheme.typography.titleMedium
            },
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LoadingCard(
    backdrop: Backdrop,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier.fillMaxWidth(),
        themeMode = themeMode
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun ErrorCard(
    backdrop: Backdrop,
    message: String,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier.fillMaxWidth(),
        themeMode = themeMode
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            ShadowText(
                text = message,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}




private fun createMockDrawResults(): Map<LotteryType, DrawResult> {
    return mapOf(
        LotteryType.SSQ to DrawResult(
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
        LotteryType.CJDLT to DrawResult(
            type = "体彩",
            name = "超级大乐透",
            code = "cjdlt",
            issue = "24001",
            red = listOf("05", "12", "18", "24", "33"),
            blue = listOf("03", "09"),
            drawDate = "2024-01-01",
            timeRule = "每周一、三、六开奖",
            saleMoney = "2.8亿元",
            prizePool = "6.5亿元",
            winnerDetail = null
        ),
        LotteryType.FC3D to DrawResult(
            type = "福彩",
            name = "福彩3D",
            code = "fc3d",
            issue = "2024001",
            red = listOf("3", "5", "8"),
            blue = emptyList(),
            drawDate = "2024-01-01",
            timeRule = "每日开奖",
            saleMoney = "4500万元",
            prizePool = null,
            winnerDetail = null
        ),
        LotteryType.KL8 to DrawResult(
            type = "福彩",
            name = "快乐8",
            code = "kl8",
            issue = "2024001",
            red = listOf("01", "05", "12", "18", "23", "28", "35", "42", "47", "52", "58", "63", "68", "72", "75", "78", "80"),
            blue = emptyList(),
            drawDate = "2024-01-01",
            timeRule = "每日开奖",
            saleMoney = "1200万元",
            prizePool = null,
            winnerDetail = null
        )
    )
}

@Preview(name = "首页 - 加载中", showBackground = false)
@Composable
private fun HomeScreenLoadingPreview() {
    LotteryTheme {
        val mockBackdrop = rememberLayerBackdrop()
        BackdropDemoScaffold(
            initialPainterResId = R.drawable.wallpaper_light
        ) { backdrop ->
            HomeContent(
                backdrop = backdrop,
                drawResults = emptyMap(),
                isLoading = true,
                error = null,
                onRefresh = {},
                themeMode = ThemeMode.LIGHT
            )
        }
    }
}

@Preview(name = "首页 - 有数据", showBackground = false)
@Composable
private fun HomeScreenSuccessPreview() {
    LotteryTheme {
        BackdropDemoScaffold(
            initialPainterResId = R.drawable.wallpaper_light
        ) { backdrop ->
            HomeContent(
                backdrop = backdrop,
                drawResults = createMockDrawResults(),
                isLoading = false,
                error = null,
                onRefresh = {},
                themeMode = ThemeMode.LIGHT
            )
        }
    }
}

@Preview(name = "首页 - 深色模式", showBackground = false, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenDarkPreview() {
    LotteryTheme {
        BackdropDemoScaffold(
            initialPainterResId = R.drawable.wallpaper_light
        ) { backdrop ->
            HomeContent(
                backdrop = backdrop,
                drawResults = createMockDrawResults(),
                isLoading = false,
                error = null,
                onRefresh = {},
                themeMode = ThemeMode.DARK
            )
        }
    }
}

@Preview(name = "首页 - 错误状态", showBackground = false)
@Composable
private fun HomeScreenErrorPreview() {
    LotteryTheme {
        BackdropDemoScaffold(
            initialPainterResId = R.drawable.wallpaper_light
        ) { backdrop ->
            HomeContent(
                backdrop = backdrop,
                drawResults = emptyMap(),
                isLoading = false,
                error = "网络连接失败，请检查网络设置",
                onRefresh = {},
                themeMode = ThemeMode.LIGHT
            )
        }
    }
}

@Preview(name = "开奖结果卡片 - 双色球", showBackground = false)
@Composable
private fun DrawResultCardPreview() {
    LotteryTheme {
        BackdropDemoScaffold(
            initialPainterResId = R.drawable.wallpaper_light
        ) { backdrop ->
            val mockDrawResult = createMockDrawResults()[LotteryType.SSQ]!!
            DrawResultCard(
                backdrop = backdrop,
                drawResult = mockDrawResult,
                lotteryType = LotteryType.SSQ,
                themeMode = ThemeMode.LIGHT
            )
        }
    }
}

@Preview(name = "开奖结果卡片 - 快乐8", showBackground = false)
@Composable
private fun DrawResultCardKL8Preview() {
    LotteryTheme {
        BackdropDemoScaffold(
            initialPainterResId = R.drawable.wallpaper_light
        ) { backdrop ->
            val mockDrawResult = createMockDrawResults()[LotteryType.KL8]!!
            DrawResultCard(
                backdrop = backdrop,
                drawResult = mockDrawResult,
                lotteryType = LotteryType.KL8,
                themeMode = ThemeMode.LIGHT
            )
        }
    }
}

@Preview(name = "号码球展示", showBackground = true)
@Composable
private fun LotteryBallPreview() {
    LotteryTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LotteryBall(
                number = "03",
                color = Color(0xFFE53935),
                size = 48.dp
            )
            LotteryBall(
                number = "07",
                color = Color(0xFFE53935),
                size = 48.dp
            )
            BallDivider()
            LotteryBall(
                number = "08",
                color = Color(0xFF1E88E5),
                size = 48.dp
            )
        }
    }
}
