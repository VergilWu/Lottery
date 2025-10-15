package com.vergil.lottery.presentation.screens.prediction

import androidx.compose.runtime.Immutable
import com.vergil.lottery.core.constants.LotteryType
import com.vergil.lottery.core.mvi.MviEffect
import com.vergil.lottery.core.mvi.MviIntent
import com.vergil.lottery.core.mvi.MviState


object PredictionContract {


    sealed interface Intent : MviIntent {

        data class SelectLotteryType(val type: LotteryType) : Intent


        data object GeneratePrediction : Intent


        data object RefreshData : Intent


        data class SelectMode(val mode: PredictionMode) : Intent


        data class SelectAlgorithms(val algorithms: Set<PredictionAlgorithm>) : Intent


        data class ToggleAlgorithm(val algorithm: PredictionAlgorithm) : Intent


        data class SetPredictionCount(val count: Int) : Intent


        data object SelectAllAlgorithms : Intent


        data object DeselectAllAlgorithms : Intent


        data object ShowAlgorithmExplanation : Intent
    }


    @Immutable
    data class State(
        val selectedType: LotteryType = LotteryType.SSQ,
        val isLoading: Boolean = false,
        val isGenerating: Boolean = false,
        val currentAlgorithm: String? = null,  
        val algorithmProgress: Float = 0f,  
        val predictions: List<PredictionResult> = emptyList(),
        val selectedMode: PredictionMode = PredictionMode.COMPREHENSIVE,  
        val selectedAlgorithms: Set<PredictionAlgorithm> = PredictionMode.COMPREHENSIVE.algorithms,
        val predictionCount: Int = 5, 
        val error: String? = null
    ) : MviState


    sealed interface Effect : MviEffect {
        data class ShowToast(val message: String) : Effect
        data object ShowGenerateSuccess : Effect
        data object ScrollToResults : Effect  
        data object ShowAlgorithmExplanation : Effect  
    }


    enum class AlgorithmCategory(val displayName: String, val description: String) {
        STATISTICS("统计学算法", "基于历史数据的统计分析"),
        PATTERN("模式识别", "发现号码出现的模式规律"),
        PROBABILITY("概率模型", "基于概率论的预测模型"),
        FEATURE("特征工程", "分析号码组合的特征"),
        OPTIMIZATION("分类优化", "优化号码的分类分布"),
        COMPREHENSIVE("综合优化", "多维度综合优化")
    }


    enum class PredictionAlgorithm(
        val displayName: String,
        val description: String,
        val category: AlgorithmCategory
    ) {

        FREQUENCY("频率权重法", "基于历史出现频率推荐", AlgorithmCategory.STATISTICS),
        OMISSION("遗漏补偿法", "推荐长期未出现的号码", AlgorithmCategory.STATISTICS),
        TREND("趋势预测法", "基于近期趋势分析", AlgorithmCategory.STATISTICS),


        ASSOCIATION("关联规则", "挖掘号码组合规律", AlgorithmCategory.PATTERN),
        CONSECUTIVE("连号分析", "检测连续号码出现规律", AlgorithmCategory.PATTERN),
        SAME_TAIL("同尾号分析", "分析相同尾数号码", AlgorithmCategory.PATTERN),


        MARKOV("马尔可夫链", "基于状态转移概率预测", AlgorithmCategory.PROBABILITY),
        BAYES("贝叶斯模型", "结合先验与条件概率", AlgorithmCategory.PROBABILITY),
        LSTM("LSTM神经网络", "深度学习时间序列预测", AlgorithmCategory.PROBABILITY),
        GENETIC("遗传算法", "模拟生物进化优化选择", AlgorithmCategory.PROBABILITY),


        SUM_VALUE("和值分析", "号码总和分布规律", AlgorithmCategory.FEATURE),
        SPAN("跨度分析", "最大最小号码差值", AlgorithmCategory.FEATURE),
        AC_VALUE("AC值分析", "号码复杂度指标", AlgorithmCategory.FEATURE),


        ODD_EVEN("奇偶比优化", "优化奇偶号码比例", AlgorithmCategory.OPTIMIZATION),
        PRIME_COMPOSITE("质合比优化", "优化质数合数比例", AlgorithmCategory.OPTIMIZATION),
        ZONE("区间分布", "优化号码区间分布", AlgorithmCategory.OPTIMIZATION),


        BALANCE("均衡分布", "综合考虑多维度均衡", AlgorithmCategory.COMPREHENSIVE);

        companion object {
            fun getByCategory(category: AlgorithmCategory): List<PredictionAlgorithm> {
                return entries.filter { it.category == category }
            }
        }
    }


    enum class PredictionMode(val displayName: String, val description: String, val algorithms: Set<PredictionAlgorithm>) {
        CONSERVATIVE(
            "保守型",
            "注重稳定，选择高频号码",
            setOf(
                PredictionAlgorithm.FREQUENCY,
                PredictionAlgorithm.OMISSION,
                PredictionAlgorithm.BALANCE
            )
        ),
        AGGRESSIVE(
            "激进型",
            "追求冷门，选择高遗漏号码",
            setOf(
                PredictionAlgorithm.OMISSION,
                PredictionAlgorithm.TREND,
                PredictionAlgorithm.MARKOV,
                PredictionAlgorithm.AC_VALUE
            )
        ),
        PROBABILITY(
            "概率型",
            "基于概率模型预测",
            setOf(
                PredictionAlgorithm.MARKOV,
                PredictionAlgorithm.BAYES,
                PredictionAlgorithm.FREQUENCY,
                PredictionAlgorithm.TREND
            )
        ),
        FEATURE(
            "特征型",
            "基于号码特征分析",
            setOf(
                PredictionAlgorithm.SUM_VALUE,
                PredictionAlgorithm.SPAN,
                PredictionAlgorithm.AC_VALUE,
                PredictionAlgorithm.ODD_EVEN,
                PredictionAlgorithm.PRIME_COMPOSITE
            )
        ),
        COMPREHENSIVE(
            "全面型",
            "综合所有算法分析",
            setOf(
                PredictionAlgorithm.FREQUENCY,
                PredictionAlgorithm.OMISSION,
                PredictionAlgorithm.TREND,
                PredictionAlgorithm.ASSOCIATION,
                PredictionAlgorithm.MARKOV,
                PredictionAlgorithm.BAYES,
                PredictionAlgorithm.SUM_VALUE,
                PredictionAlgorithm.ODD_EVEN,
                PredictionAlgorithm.BALANCE
            )
        ),
        CUSTOM(
            "自定义",
            "自由选择算法组合",
            emptySet()
        )
    }


    @Immutable
    data class PredictionResult(
        val id: String,
        val lotteryType: LotteryType, 
        val redNumbers: List<String>,
        val blueNumbers: List<String>,
        val totalScore: Float, 
        val algorithmScores: Map<PredictionAlgorithm, Float>, 
        val confidence: Float, 
        val explanation: String 
    )


    @Immutable
    data class NumberScore(
        val number: String,

        val frequencyScore: Float = 0f,      
        val omissionScore: Float = 0f,       
        val trendScore: Float = 0f,          

        val associationScore: Float = 0f,    
        val consecutiveScore: Float = 0f,    
        val sameTailScore: Float = 0f,       

        val markovScore: Float = 0f,         
        val bayesScore: Float = 0f,          
        val lstmScore: Float = 0f,           
        val geneticScore: Float = 0f,        

        val sumValueScore: Float = 0f,       
        val spanScore: Float = 0f,           
        val acValueScore: Float = 0f,        

        val oddEvenScore: Float = 0f,        
        val primeCompositeScore: Float = 0f, 
        val zoneScore: Float = 0f,           

        val totalScore: Float = 0f           
    )
}

