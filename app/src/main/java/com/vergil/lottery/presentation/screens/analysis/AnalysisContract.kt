package com.vergil.lottery.presentation.screens.analysis

import androidx.compose.runtime.Immutable
import com.vergil.lottery.core.constants.LotteryType
import com.vergil.lottery.core.mvi.MviEffect
import com.vergil.lottery.core.mvi.MviIntent
import com.vergil.lottery.core.mvi.MviState
import com.vergil.lottery.domain.analyzer.AnalysisEngine
import com.vergil.lottery.domain.model.DrawResult


object AnalysisContract {


    enum class AnalysisDimension(val displayName: String, val description: String) {
        HISTORY("历史记录", "查看完整的历史开奖记录"),
        OMISSION("遗漏分析", "分析号码未出现的连续期数"),
        FREQUENCY("频率分析", "统计号码出现的次数和频率"),
        HOT_COLD("冷热号", "基于近期数据判断冷热号码"),
        CONSECUTIVE("连号分析", "分析连续号码的出现规律"),
        SAME_TAIL("同尾号", "统计相同尾数号码的分布"),
        SUM_VALUE("和值分析", "分析开奖号码的和值分布"),
        SPAN("跨度分析", "分析最大号与最小号的差值"),
        AC_VALUE("AC值", "分析号码的算术复杂度"),
        ODD_EVEN("奇偶比", "统计奇数偶数的比例"),
        SIZE_RATIO("大小比", "统计大号小号的比例"),
        PRIME_COMPOSITE("质合比", "统计质数合数的比例"),
        ZONE("区间分布", "分析号码在各区间的分布")
    }


    enum class BallType(val displayName: String) {
        RED("红球"),
        BLUE("蓝球")
    }


    sealed interface Intent : MviIntent {
        data class SelectLotteryType(val type: LotteryType) : Intent
        data class SelectDimension(val dimension: AnalysisDimension) : Intent
        data class SelectBallType(val ballType: BallType) : Intent
        data object LoadHistory : Intent
        data object RefreshHistory : Intent
        data object LoadMoreHistory : Intent
        data object AnalyzeData : Intent  
    }


    @Immutable
    data class State(
        val selectedType: LotteryType = LotteryType.SSQ,
        val selectedDimension: AnalysisDimension = AnalysisDimension.HISTORY,
        val selectedBallType: BallType = BallType.RED,
        val historyList: List<DrawResult> = emptyList(),
        val isLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
        val hasMore: Boolean = true,
        val currentPage: Int = 0,
        val pageSize: Int = 20,
        val error: String? = null,

        val omissionData: List<AnalysisEngine.OmissionData> = emptyList(),
        val frequencyData: List<AnalysisEngine.FrequencyData> = emptyList(),
        val hotColdData: List<AnalysisEngine.HotColdData> = emptyList(),
        val consecutiveData: List<AnalysisEngine.ConsecutiveData> = emptyList(),
        val sameTailData: List<AnalysisEngine.SameTailData> = emptyList(),
        val sumValueData: List<AnalysisEngine.SumValueData> = emptyList(),
        val spanData: List<AnalysisEngine.SpanData> = emptyList(),
        val acValueData: List<AnalysisEngine.ACValueData> = emptyList(),
        val oddEvenData: List<AnalysisEngine.OddEvenRatioData> = emptyList(),
        val sizeRatioData: List<AnalysisEngine.SizeRatioData> = emptyList(),
        val primeCompositeData: List<AnalysisEngine.PrimeCompositeRatioData> = emptyList(),
        val zoneData: List<AnalysisEngine.ZoneData> = emptyList()
    ) : MviState


    sealed interface Effect : MviEffect {
        data class ShowToast(val message: String) : Effect
        data class NavigateToDetail(val drawResult: DrawResult) : Effect
    }
}


