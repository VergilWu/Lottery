package com.vergil.lottery.domain.analyzer

import android.content.Context
import com.vergil.lottery.core.constants.LotteryType
import com.vergil.lottery.domain.ml.LSTMPredictor
import com.vergil.lottery.domain.model.DrawResult
import com.vergil.lottery.presentation.screens.prediction.PredictionContract.NumberScore
import com.vergil.lottery.presentation.screens.prediction.PredictionContract.PredictionAlgorithm
import com.vergil.lottery.presentation.screens.prediction.PredictionContract.PredictionResult
import com.vergil.lottery.presentation.screens.prediction.PredictionContract.ComplexPredictionResult
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random


class PredictionEngine(
    private val context: Context? = null  
) {


    private val primeSet = setOf(
        2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 
        31, 37, 41, 43, 47
    )


    private val markovCache = mutableMapOf<String, Map<String, Double>>()


    private val lstmPredictorCache = mutableMapOf<LotteryType, LSTMPredictor?>()


    private fun getLSTMPredictor(lotteryType: LotteryType): LSTMPredictor? {
        return lstmPredictorCache.getOrPut(lotteryType) {
            context?.let { ctx ->
                try {
                    LSTMPredictor(ctx, lotteryType).apply {
                        if (initialize()) {
                            Timber.d("✅ LSTM 预测器初始化成功 (${lotteryType.code})")
                        } else {
                            Timber.w("⚠️ LSTM 预测器初始化失败 (${lotteryType.code})，将使用简化版算法")
                            return@getOrPut null
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "❌ 创建 LSTM 预测器失败 (${lotteryType.code})")
                    null
                }
            }
        }
    }


    fun generatePredictions(
        history: List<DrawResult>,
        lotteryType: LotteryType,
        algorithms: Set<PredictionAlgorithm>,
        count: Int
    ): List<PredictionResult> {
        if (history.size < 20) {
            Timber.w("History data insufficient: ${history.size} draws")
            return emptyList()
        }

        val config = getLotteryConfig(lotteryType)


        val sharedData = precomputeSharedData(history, config)


        val redScores = calculateNumberScores(history, lotteryType, config.redRange, algorithms, isRed = true, sharedData)
        val blueScores = calculateNumberScores(history, lotteryType, config.blueRange, algorithms, isRed = false, sharedData)

        return (1..count).map { index ->
            generateSinglePrediction(
                id = "pred_$index",
                lotteryType = lotteryType,
                redScores = redScores,
                blueScores = blueScores,
                config = config,
                algorithms = algorithms,
                sharedData = sharedData
            )
        }
    }


    fun generateComplexPredictions(
        history: List<DrawResult>,
        lotteryType: LotteryType,
        algorithms: Set<PredictionAlgorithm>,
        targetCombinations: Int
    ): List<ComplexPredictionResult> {
        if (history.size < 20) {
            Timber.w("History data insufficient: ${history.size} draws")
            return emptyList()
        }

        val config = getLotteryConfig(lotteryType)


        val sharedData = precomputeSharedData(history, config)


        val redScores = calculateNumberScores(history, lotteryType, config.redRange, algorithms, isRed = true, sharedData)
        val blueScores = calculateNumberScores(history, lotteryType, config.blueRange, algorithms, isRed = false, sharedData)

        return listOf(
            generateComplexPrediction(
                id = "complex_1",
                lotteryType = lotteryType,
                redScores = redScores,
                blueScores = blueScores,
                config = config,
                algorithms = algorithms,
                sharedData = sharedData,
                targetCombinations = targetCombinations
            )
        )
    }


    private fun precomputeSharedData(history: List<DrawResult>, config: LotteryConfig): SharedData {
        val allRedNumbers = history.flatMap { it.red }
        val allBlueNumbers = history.flatMap { it.blue }

        return SharedData(
            historySize = history.size,
            redFrequencyMap = allRedNumbers.groupingBy { it }.eachCount(),
            blueFrequencyMap = allBlueNumbers.groupingBy { it }.eachCount(),
            recentHistory = history.take(10),
            olderHistory = history.drop(10).take(10)
        )
    }


    private fun calculateNumberScores(
        history: List<DrawResult>,
        lotteryType: LotteryType,
        range: IntRange,
        algorithms: Set<PredictionAlgorithm>,
        isRed: Boolean,
        sharedData: SharedData
    ): Map<String, NumberScore> {
        return range.associate { num ->
            val numStr = num.toString().padStart(2, '0')
            numStr to calculateSingleNumberScore(numStr, history, lotteryType, algorithms, isRed, sharedData)
        }
    }


    private fun calculateSingleNumberScore(
        number: String,
        history: List<DrawResult>,
        lotteryType: LotteryType,
        algorithms: Set<PredictionAlgorithm>,
        isRed: Boolean,
        sharedData: SharedData
    ): NumberScore {
        val scores = mutableMapOf<String, Float>()


        if (PredictionAlgorithm.FREQUENCY in algorithms) {
            scores["frequency"] = calculateFrequencyScore(number, sharedData, isRed)
        }
        if (PredictionAlgorithm.OMISSION in algorithms) {
            scores["omission"] = calculateOmissionScore(number, history, isRed)
        }
        if (PredictionAlgorithm.TREND in algorithms) {
            scores["trend"] = calculateTrendScore(number, sharedData, isRed)
        }


        if (PredictionAlgorithm.ASSOCIATION in algorithms) {
            scores["association"] = calculateAssociationScore(number, history, isRed)
        }
        if (PredictionAlgorithm.CONSECUTIVE in algorithms) {
            scores["consecutive"] = calculateConsecutiveScore(number, history, isRed)
        }
        if (PredictionAlgorithm.SAME_TAIL in algorithms) {
            scores["sameTail"] = calculateSameTailScore(number, history, isRed)
        }


        if (PredictionAlgorithm.MARKOV in algorithms) {
            scores["markov"] = calculateMarkovScore(number, history, isRed)
        }
        if (PredictionAlgorithm.BAYES in algorithms) {
            scores["bayes"] = calculateBayesScore(number, history, isRed, sharedData)
        }
        if (PredictionAlgorithm.LSTM in algorithms) {
            scores["lstm"] = calculateLSTMScore(number, history, isRed, lotteryType, sharedData)
        }
        if (PredictionAlgorithm.GENETIC in algorithms) {
            scores["genetic"] = calculateGeneticScore(number, history, isRed, sharedData)
        }


        scores["sumValue"] = 0.5f
        scores["span"] = 0.5f
        scores["acValue"] = 0.5f
        scores["oddEven"] = if (number.toInt() % 2 == 1) 0.5f else 0.5f
        scores["primeComposite"] = if (isPrime(number.toInt())) 0.6f else 0.4f
        scores["zone"] = 0.5f


        val totalScore = calculateWeightedScore(scores, algorithms)

        return NumberScore(
            number = number,
            frequencyScore = scores["frequency"] ?: 0f,
            omissionScore = scores["omission"] ?: 0f,
            trendScore = scores["trend"] ?: 0f,
            associationScore = scores["association"] ?: 0f,
            consecutiveScore = scores["consecutive"] ?: 0f,
            sameTailScore = scores["sameTail"] ?: 0f,
            markovScore = scores["markov"] ?: 0f,
            bayesScore = scores["bayes"] ?: 0f,
            lstmScore = scores["lstm"] ?: 0f,
            geneticScore = scores["genetic"] ?: 0f,
            sumValueScore = scores["sumValue"] ?: 0f,
            spanScore = scores["span"] ?: 0f,
            acValueScore = scores["acValue"] ?: 0f,
            oddEvenScore = scores["oddEven"] ?: 0f,
            primeCompositeScore = scores["primeComposite"] ?: 0f,
            zoneScore = scores["zone"] ?: 0f,
            totalScore = totalScore
        )
    }




    private fun calculateFrequencyScore(
        number: String,
        sharedData: SharedData,
        isRed: Boolean
    ): Float {
        val frequencyMap = if (isRed) sharedData.redFrequencyMap else sharedData.blueFrequencyMap
        val appearances = frequencyMap[number] ?: 0
        val frequency = appearances.toFloat() / sharedData.historySize
        return min(frequency / 0.2f, 1f)
    }


    private fun calculateOmissionScore(
        number: String,
        history: List<DrawResult>,
        isRed: Boolean
    ): Float {
        var omission = 0
        for (draw in history) {
            val numbers = if (isRed) draw.red else draw.blue
            if (numbers.contains(number)) break
            omission++
        }

        val maxOmission = calculateMaxOmission(number, history, isRed)
        if (maxOmission == 0) return 0.5f

        val ratio = omission.toFloat() / maxOmission

        return (1f / (1f + exp(-5f * (ratio - 0.5f)))).toFloat()
    }

    private fun calculateMaxOmission(number: String, history: List<DrawResult>, isRed: Boolean): Int {
        var maxOmission = 0
        var currentOmission = 0

        for (draw in history.reversed()) {
            val numbers = if (isRed) draw.red else draw.blue
            if (numbers.contains(number)) {
                maxOmission = maxOf(maxOmission, currentOmission)
                currentOmission = 0
            } else {
                currentOmission++
            }
        }
        return maxOf(maxOmission, currentOmission)
    }


    private fun calculateTrendScore(
        number: String,
        sharedData: SharedData,
        isRed: Boolean
    ): Float {
        val recent = sharedData.recentHistory
        val older = sharedData.olderHistory

        if (older.isEmpty()) return 0.5f

        val recentFreq = recent.count { draw ->
            val numbers = if (isRed) draw.red else draw.blue
            numbers.contains(number)
        }.toFloat() / recent.size

        val olderFreq = older.count { draw ->
            val numbers = if (isRed) draw.red else draw.blue
            numbers.contains(number)
        }.toFloat() / older.size


        val trend = (recentFreq - olderFreq + 0.2f) / 0.4f
        return trend.coerceIn(0f, 1f)
    }




    private fun calculateAssociationScore(
        number: String,
        history: List<DrawResult>,
        isRed: Boolean
    ): Float {
        val coOccurrences = mutableMapOf<String, Int>()

        for (draw in history) {
            val numbers = if (isRed) draw.red else draw.blue
            if (numbers.contains(number)) {
                numbers.forEach { other ->
                    if (other != number) {
                        coOccurrences[other] = coOccurrences.getOrDefault(other, 0) + 1
                    }
                }
            }
        }

        val avgCoOccurrence = if (coOccurrences.isNotEmpty()) {
            coOccurrences.values.average().toFloat() / history.size
        } else 0f

        return min(avgCoOccurrence / 0.1f, 1f)
    }


    private fun calculateConsecutiveScore(
        number: String,
        history: List<DrawResult>,
        isRed: Boolean
    ): Float {
        val num = number.toInt()
        val prev = (num - 1).toString().padStart(2, '0')
        val next = (num + 1).toString().padStart(2, '0')

        var consecutiveCount = 0
        for (draw in history) {
            val numbers = if (isRed) draw.red else draw.blue
            if (numbers.contains(number)) {
                if (numbers.contains(prev) || numbers.contains(next)) {
                    consecutiveCount++
                }
            }
        }

        val consecutiveFreq = consecutiveCount.toFloat() / history.size
        return min(consecutiveFreq / 0.15f, 1f)
    }


    private fun calculateSameTailScore(
        number: String,
        history: List<DrawResult>,
        isRed: Boolean
    ): Float {
        val tail = number.last()

        var sameTailCount = 0
        for (draw in history) {
            val numbers = if (isRed) draw.red else draw.blue
            if (numbers.contains(number)) {

                val hasSameTail = numbers.any { it != number && it.last() == tail }
                if (hasSameTail) sameTailCount++
            }
        }

        val sameTailFreq = sameTailCount.toFloat() / history.size
        return min(sameTailFreq / 0.1f, 1f)
    }




    private fun calculateMarkovScore(
        number: String,
        history: List<DrawResult>,
        isRed: Boolean
    ): Float {
        if (history.size < 2) return 0.5f


        val currentState = (if (isRed) history[0].red else history[0].blue).sorted().joinToString(",")


        val cacheKey = "$currentState-$number-$isRed"
        if (markovCache.containsKey(cacheKey)) {
            return markovCache[cacheKey]?.get(number)?.toFloat() ?: 0.5f
        }


        var transitionCount = 0
        var stateOccurrence = 0

        for (i in 0 until history.size - 1) {
            val state = (if (isRed) history[i].red else history[i].blue).sorted().joinToString(",")
            val nextNumbers = if (isRed) history[i + 1].red else history[i + 1].blue

            if (state == currentState) {
                stateOccurrence++
                if (nextNumbers.contains(number)) {
                    transitionCount++
                }
            }
        }

        val probability = if (stateOccurrence > 0) {
            transitionCount.toFloat() / stateOccurrence
        } else 0.5f


        markovCache[cacheKey] = mapOf(number to probability.toDouble())

        return probability.coerceIn(0f, 1f)
    }


    private fun calculateBayesScore(
        number: String,
        history: List<DrawResult>,
        isRed: Boolean,
        sharedData: SharedData
    ): Float {
        val frequencyMap = if (isRed) sharedData.redFrequencyMap else sharedData.blueFrequencyMap
        val appearances = frequencyMap[number] ?: 0


        val prior = appearances.toFloat() / sharedData.historySize


        val recentAppearances = sharedData.recentHistory.count { draw ->
            val numbers = if (isRed) draw.red else draw.blue
            numbers.contains(number)
        }
        val likelihood = if (appearances > 0) {
            recentAppearances.toFloat() / appearances
        } else 0f


        val evidence = sharedData.recentHistory.size.toFloat() / sharedData.historySize


        val posterior = if (evidence > 0) {
            (likelihood * prior / evidence).coerceIn(0f, 1f)
        } else prior

        return posterior
    }


    private fun calculateLSTMScore(
        number: String,
        history: List<DrawResult>,
        isRed: Boolean,
        lotteryType: LotteryType,
        sharedData: SharedData
    ): Float {

        val predictor = getLSTMPredictor(lotteryType)
        if (predictor != null && history.size >= 10) {
            try {

                val drawHistory = history.take(10).map { draw ->
                    LSTMPredictor.DrawHistory(
                        redNumbers = draw.red,
                        blueNumbers = draw.blue
                    )
                }


                val (redProbabilities, blueProbabilities) = predictor.predict(drawHistory) ?: return fallbackLSTMScore(number, history, isRed)


                val probabilities = if (isRed) redProbabilities else blueProbabilities
                return probabilities[number] ?: 0.5f

            } catch (e: Exception) {
                Timber.w(e, "⚠️ LSTM 预测失败，使用简化版算法")
                return fallbackLSTMScore(number, history, isRed)
            }
        }


        return fallbackLSTMScore(number, history, isRed)
    }


    private fun fallbackLSTMScore(
        number: String,
        history: List<DrawResult>,
        isRed: Boolean
    ): Float {
        if (history.size < 10) return 0.5f


        val alpha = 0.3f  


        val sequence = history.reversed().map { draw ->
            val numbers = if (isRed) draw.red else draw.blue
            if (numbers.contains(number)) 1f else 0f
        }


        var ema = sequence[0]
        for (i in 1 until sequence.size) {
            ema = alpha * sequence[i] + (1 - alpha) * ema
        }


        val sma = sequence.map { it.toDouble() }.average().toFloat()
        val trend = if (sma > 0) (ema / sma).coerceIn(0f, 2f) / 2f else ema

        return trend.coerceIn(0f, 1f)
    }


    private fun calculateGeneticScore(
        number: String,
        history: List<DrawResult>,
        isRed: Boolean,
        sharedData: SharedData
    ): Float {

        val frequencyFitness = calculateFrequencyScore(number, sharedData, isRed)


        val omissionFitness = calculateOmissionScore(number, history, isRed)


        val trendFitness = calculateTrendScore(number, sharedData, isRed)


        val associationFitness = calculateAssociationScore(number, history, isRed)



        val fitness = (
            frequencyFitness * 0.3f +
            omissionFitness * 0.25f +
            trendFitness * 0.25f +
            associationFitness * 0.2f
        )


        val selectionPressure = 1.5
        val enhancedFitness = fitness.toDouble().pow(selectionPressure).toFloat()

        return enhancedFitness.coerceIn(0f, 1f)
    }




    private fun calculateWeightedScore(
        scores: Map<String, Float>,
        algorithms: Set<PredictionAlgorithm>
    ): Float {
        if (scores.isEmpty()) return 0f


        val activeScores = scores.filter { it.value > 0f }
        if (activeScores.isEmpty()) return 0f

        return activeScores.values.average().toFloat()
    }


    private fun isPrime(n: Int): Boolean {
        if (n <= 1) return false
        if (n in primeSet) return true
        if (n <= 50) return false 


        if (n % 2 == 0) return false
        val sqrtN = sqrt(n.toDouble()).toInt()
        for (i in 3..sqrtN step 2) {
            if (n % i == 0) return false
        }
        return true
    }


    private fun generateSinglePrediction(
        id: String,
        lotteryType: LotteryType,
        redScores: Map<String, NumberScore>,
        blueScores: Map<String, NumberScore>,
        config: LotteryConfig,
        algorithms: Set<PredictionAlgorithm>,
        sharedData: SharedData
    ): PredictionResult {

        val redNumbers = selectNumbersByWeight(redScores, config.redCount)
        val blueNumbers = selectNumbersByWeight(blueScores, config.blueCount)


        val optimizedRed = applyCombinatorialOptimization(
            redNumbers.map { it.toInt() },
            config.redRange,
            algorithms
        ).map { it.toString().padStart(2, '0') }


        val redAvgScore = optimizedRed.mapNotNull { redScores[it]?.totalScore }
            .takeIf { it.isNotEmpty() }?.average()?.toFloat() ?: 0f
        val blueAvgScore = blueNumbers.mapNotNull { blueScores[it]?.totalScore }
            .takeIf { it.isNotEmpty() }?.average()?.toFloat() ?: 0f


        val combinatorialBonus = calculateCombinatorialBonus(
            optimizedRed.map { it.toInt() },
            config.redRange,
            algorithms
        )


        val totalScore = if (blueNumbers.isEmpty()) {

            (redAvgScore * (1 + combinatorialBonus) * 100).coerceIn(0f, 100f)
        } else {

            ((redAvgScore * 0.7f + blueAvgScore * 0.3f) * (1 + combinatorialBonus) * 100)
                .coerceIn(0f, 100f)
        }


        val algorithmScores = algorithms.associateWith { algo ->
            calculateAlgorithmScore(optimizedRed + blueNumbers, redScores + blueScores, algo)
        }


        val explanation = generateExplanation(optimizedRed, blueNumbers, redScores, blueScores, algorithms)

        return PredictionResult(
            id = id,
            lotteryType = lotteryType,
            redNumbers = if (shouldSortNumbers(lotteryType, isRed = true)) optimizedRed.sorted() else optimizedRed,
            blueNumbers = if (shouldSortNumbers(lotteryType, isRed = false)) blueNumbers.sorted() else blueNumbers,
            totalScore = totalScore,
            algorithmScores = algorithmScores,
            confidence = totalScore / 100f,
            explanation = explanation
        )
    }


    private fun generateComplexPrediction(
        id: String,
        lotteryType: LotteryType,
        redScores: Map<String, NumberScore>,
        blueScores: Map<String, NumberScore>,
        config: LotteryConfig,
        algorithms: Set<PredictionAlgorithm>,
        sharedData: SharedData,
        targetCombinations: Int
    ): ComplexPredictionResult {

        val (optimalRedCount, optimalBlueCount) = calculateOptimalNumbersForTarget(
            config.redCount,
            config.blueCount,
            targetCombinations
        )

        val redNumbers = selectComplexNumbers(redScores, optimalRedCount, config.redRange)
        val blueNumbers = selectComplexNumbers(blueScores, optimalBlueCount, config.blueRange)
        
        if (redNumbers.isEmpty() && blueNumbers.isEmpty()) {
            return ComplexPredictionResult(
                id = id,
                lotteryType = lotteryType,
                redNumbers = emptyList(),
                blueNumbers = emptyList(),
                totalScore = 0f,
                algorithmScores = emptyMap(),
                confidence = 0f,
                explanation = "无法生成复式票预测",
                combinationCount = 0,
                coverageRate = 0f,
                hotNumbers = emptyList(),
                coldNumbers = emptyList()
            )
        }


        val optimizedRed = applyComplexOptimization(
            redNumbers.map { it.toInt() },
            config.redRange,
            algorithms
        ).map { it.toString().padStart(2, '0') }


        val redAvgScore = optimizedRed.mapNotNull { redScores[it]?.totalScore }
            .takeIf { it.isNotEmpty() }?.average()?.toFloat() ?: 0f
        val blueAvgScore = blueNumbers.mapNotNull { blueScores[it]?.totalScore }
            .takeIf { it.isNotEmpty() }?.average()?.toFloat() ?: 0f


        val complexBonus = calculateComplexBonus(
            optimizedRed.map { it.toInt() },
            config.redRange,
            algorithms
        )


        val totalScore = if (blueNumbers.isEmpty()) {
            (redAvgScore * (1 + complexBonus) * 100).coerceIn(0f, 100f)
        } else {
            ((redAvgScore * 0.7f + blueAvgScore * 0.3f) * (1 + complexBonus) * 100)
                .coerceIn(0f, 100f)
        }


        val algorithmScores = algorithms.associateWith { algo ->
            calculateAlgorithmScore(optimizedRed + blueNumbers, redScores + blueScores, algo)
        }


        val combinationCount = calculateCombinationCount(optimizedRed, blueNumbers, config)
        val coverageRate = calculateCoverageRate(optimizedRed, blueNumbers, redScores, blueScores)
        val hotNumbers = identifyHotNumbers(optimizedRed, redScores)
        val coldNumbers = identifyColdNumbers(optimizedRed, redScores)


        val explanation = generateComplexExplanation(
            optimizedRed, 
            blueNumbers, 
            redScores, 
            blueScores, 
            algorithms,
            combinationCount,
            coverageRate
        )

        return ComplexPredictionResult(
            id = id,
            lotteryType = lotteryType,
            redNumbers = if (shouldSortNumbers(lotteryType, isRed = true)) optimizedRed.sorted() else optimizedRed,
            blueNumbers = if (shouldSortNumbers(lotteryType, isRed = false)) blueNumbers.sorted() else blueNumbers,
            totalScore = totalScore,
            algorithmScores = algorithmScores,
            confidence = totalScore / 100f,
            explanation = explanation,
            combinationCount = combinationCount,
            coverageRate = coverageRate,
            hotNumbers = hotNumbers,
            coldNumbers = coldNumbers
        )
    }


    private fun shouldSortNumbers(lotteryType: LotteryType, isRed: Boolean): Boolean {
        return when (lotteryType) {

            LotteryType.FC3D,  
            LotteryType.PL3,    
            LotteryType.PL5,    
            LotteryType.QXC -> false  


            LotteryType.SSQ,    
            LotteryType.CJDLT,  
            LotteryType.QLC,    
            LotteryType.KL8 -> true   
        }
    }


    private fun selectNumbersByWeight(
        scores: Map<String, NumberScore>,
        count: Int
    ): List<String> {
        val selected = mutableSetOf<String>()
        val candidates = scores.toList().sortedByDescending { it.second.totalScore }

        while (selected.size < count && selected.size < candidates.size) {
            val available = candidates.filter { it.first !in selected }
            val totalWeight = available.sumOf { it.second.totalScore.toDouble() }

            if (totalWeight <= 0) {

                selected.add(available.random().first)
                continue
            }

            var random = Random.nextDouble() * totalWeight

            for ((number, score) in available) {
                random -= score.totalScore
                if (random <= 0) {
                    selected.add(number)
                    break
                }
            }
        }


        while (selected.size < count) {
            val remaining = scores.keys - selected
            if (remaining.isEmpty()) break
            selected.add(remaining.random())
        }

        return selected.toList()
    }


    private fun applyCombinatorialOptimization(
        numbers: List<Int>,
        range: IntRange,
        algorithms: Set<PredictionAlgorithm>
    ): List<Int> {
        var optimized = numbers.toList()


        if (PredictionAlgorithm.SUM_VALUE in algorithms) {
            optimized = optimizeSumValue(optimized, range)
        }


        if (PredictionAlgorithm.SPAN in algorithms) {
            optimized = optimizeSpan(optimized, range)
        }


        if (PredictionAlgorithm.AC_VALUE in algorithms) {
            optimized = optimizeAcValue(optimized, range)
        }


        if (PredictionAlgorithm.ODD_EVEN in algorithms) {
            optimized = optimizeOddEven(optimized, range)
        }


        if (PredictionAlgorithm.PRIME_COMPOSITE in algorithms) {
            optimized = optimizePrimeComposite(optimized, range)
        }


        if (PredictionAlgorithm.ZONE in algorithms) {
            optimized = optimizeZoneDistribution(optimized, range)
        }


        if (PredictionAlgorithm.BALANCE in algorithms) {
            optimized = optimizeBalance(optimized, range)
        }

        return optimized
    }




    private fun optimizeSumValue(numbers: List<Int>, range: IntRange): List<Int> {
        val sum = numbers.sum()
        val mid = range.sum() * numbers.size / range.count()
        val targetRange = (mid * 0.8).toInt()..(mid * 1.2).toInt()

        if (sum in targetRange) return numbers


        val mutable = numbers.toMutableList()
        if (sum < targetRange.first) {
            val minIdx = mutable.indexOf(mutable.minOrNull())
            val candidates = range.filter { it !in mutable && it > mutable[minIdx] }
            if (candidates.isNotEmpty()) {
                mutable[minIdx] = candidates.random()
            }
        } else if (sum > targetRange.last) {
            val maxIdx = mutable.indexOf(mutable.maxOrNull())
            val candidates = range.filter { it !in mutable && it < mutable[maxIdx] }
            if (candidates.isNotEmpty()) {
                mutable[maxIdx] = candidates.random()
            }
        }

        return mutable
    }


    private fun optimizeSpan(numbers: List<Int>, range: IntRange): List<Int> {
        val span = (numbers.maxOrNull() ?: 0) - (numbers.minOrNull() ?: 0)
        val targetSpan = 15..25

        if (span in targetSpan) return numbers

        val mutable = numbers.toMutableList()
        if (span < targetSpan.first) {

            val candidates = range.filter { it !in mutable }
            if (candidates.isNotEmpty()) {
                mutable[mutable.size / 2] = candidates.random()
            }
        }

        return mutable
    }


    private fun optimizeAcValue(numbers: List<Int>, range: IntRange): List<Int> {
        val acValue = calculateAcValue(numbers)
        val targetAc = 6..8

        if (acValue in targetAc) return numbers


        val mutable = numbers.toMutableList()
        val candidates = range.filter { it !in mutable }
        if (candidates.isNotEmpty() && acValue < targetAc.first) {
            mutable[Random.nextInt(mutable.size)] = candidates.random()
        }

        return mutable
    }

    private fun calculateAcValue(numbers: List<Int>): Int {
        val differences = mutableSetOf<Int>()
        for (i in numbers.indices) {
            for (j in i + 1 until numbers.size) {
                differences.add(abs(numbers[i] - numbers[j]))
            }
        }
        return differences.size - (numbers.size - 1)
    }


    private fun optimizeOddEven(numbers: List<Int>, range: IntRange): List<Int> {
        val oddCount = numbers.count { it % 2 == 1 }
        val targetOdd = 2..4

        if (oddCount in targetOdd) return numbers

        val mutable = numbers.toMutableList()
        if (oddCount < targetOdd.first) {

            val evenIdx = mutable.indexOfFirst { it % 2 == 0 }
            if (evenIdx >= 0) {
                val oddCandidates = range.filter { it % 2 == 1 && it !in mutable }
                if (oddCandidates.isNotEmpty()) {
                    mutable[evenIdx] = oddCandidates.random()
                }
            }
        } else if (oddCount > targetOdd.last) {

            val oddIdx = mutable.indexOfFirst { it % 2 == 1 }
            if (oddIdx >= 0) {
                val evenCandidates = range.filter { it % 2 == 0 && it !in mutable }
                if (evenCandidates.isNotEmpty()) {
                    mutable[oddIdx] = evenCandidates.random()
                }
            }
        }

        return mutable
    }


    private fun optimizePrimeComposite(numbers: List<Int>, range: IntRange): List<Int> {
        val primeCount = numbers.count { isPrime(it) }
        val targetPrime = 2..4

        if (primeCount in targetPrime) return numbers

        val mutable = numbers.toMutableList()
        if (primeCount < targetPrime.first) {

            val compositeIdx = mutable.indexOfFirst { !isPrime(it) }
            if (compositeIdx >= 0) {
                val primeCandidates = range.filter { isPrime(it) && it !in mutable }
                if (primeCandidates.isNotEmpty()) {
                    mutable[compositeIdx] = primeCandidates.random()
                }
            }
        }

        return mutable
    }


    private fun optimizeZoneDistribution(numbers: List<Int>, range: IntRange): List<Int> {
        val zoneSize = range.count() / 3
        val zone1 = range.first until (range.first + zoneSize)
        val zone2 = (range.first + zoneSize) until (range.first + zoneSize * 2)
        val zone3 = (range.first + zoneSize * 2)..range.last

        val zone1Count = numbers.count { it in zone1 }
        val zone2Count = numbers.count { it in zone2 }
        val zone3Count = numbers.count { it in zone3 }


        if (maxOf(zone1Count, zone2Count, zone3Count) - minOf(zone1Count, zone2Count, zone3Count) <= 1) {
            return numbers
        }


        val mutable = numbers.toMutableList()

        return mutable
    }


    private fun optimizeBalance(numbers: List<Int>, range: IntRange): List<Int> {
        val mid = (range.first + range.last) / 2
        val bigCount = numbers.count { it > mid }
        val oddCount = numbers.count { it % 2 == 1 }

        val targetBig = 2..4
        val targetOdd = 2..4

        if (bigCount in targetBig && oddCount in targetOdd) return numbers

        val mutable = numbers.toMutableList()


        if (bigCount < targetBig.first) {
            val smallIdx = mutable.indexOfFirst { it <= mid }
            if (smallIdx >= 0) {
                val bigCandidates = range.filter { it > mid && it !in mutable }
                if (bigCandidates.isNotEmpty()) {
                    mutable[smallIdx] = bigCandidates.random()
                }
            }
        }

        return mutable
    }


    private fun selectComplexNumbers(
        scores: Map<String, NumberScore>,
        count: Int,
        range: IntRange
    ): List<String> {
        if (count <= 0) return emptyList()
        if (scores.isEmpty()) return emptyList()
        
        val selected = mutableSetOf<String>()
        val candidates = scores.toList().sortedByDescending { it.second.totalScore }


        val topCandidates = candidates.take(count * 3)
        val highScoreNumbers = topCandidates.filter { it.second.totalScore > 0.6f }
        val mediumScoreNumbers = topCandidates.filter { it.second.totalScore in 0.4f..0.6f }
        val lowScoreNumbers = topCandidates.filter { it.second.totalScore < 0.4f }


        val hotNumbers = highScoreNumbers.take(count / 2)
        val balancedNumbers = mediumScoreNumbers.take(count / 3)
        val coldNumbers = lowScoreNumbers.take(count / 6)


        selected.addAll(hotNumbers.map { it.first })
        selected.addAll(balancedNumbers.map { it.first })
        selected.addAll(coldNumbers.map { it.first })


        while (selected.size < count && selected.size < candidates.size) {
            val available = candidates.filter { it.first !in selected }
            if (available.isEmpty()) break
            selected.add(available.random().first)
        }

        return selected.toList()
    }


    private fun selectComplexNumbersForTarget(
        scores: Map<String, NumberScore>,
        baseCount: Int,
        range: IntRange,
        targetCombinations: Int
    ): List<String> {
        val selected = mutableSetOf<String>()
        val candidates = scores.toList().sortedByDescending { it.second.totalScore }

        val redCount = baseCount
        val blueCount = if (baseCount > 0) 1 else 0

        val redNeeded = calculateNumbersNeededForCombinations(redCount, targetCombinations)
        val blueNeeded = calculateNumbersNeededForCombinations(blueCount, targetCombinations)

        val totalNeeded = if (baseCount > 0) redNeeded else blueNeeded

        val topCandidates = candidates.take(totalNeeded * 2)
        val highScoreNumbers = topCandidates.filter { it.second.totalScore > 0.6f }
        val mediumScoreNumbers = topCandidates.filter { it.second.totalScore in 0.4f..0.6f }
        val lowScoreNumbers = topCandidates.filter { it.second.totalScore < 0.4f }

        val hotNumbers = highScoreNumbers.take(totalNeeded / 2)
        val balancedNumbers = mediumScoreNumbers.take(totalNeeded / 3)
        val coldNumbers = lowScoreNumbers.take(totalNeeded / 6)

        selected.addAll(hotNumbers.map { it.first })
        selected.addAll(balancedNumbers.map { it.first })
        selected.addAll(coldNumbers.map { it.first })

        while (selected.size < totalNeeded && selected.size < candidates.size) {
            val available = candidates.filter { it.first !in selected }
            if (available.isEmpty()) break
            selected.add(available.random().first)
        }

        return selected.toList()
    }


    private fun calculateNumbersNeededForCombinations(baseCount: Int, targetCombinations: Int): Int {
        if (baseCount <= 0) return 0

        var numbersNeeded = baseCount
        while (calculateCombinations(numbersNeeded, baseCount) < targetCombinations && numbersNeeded < 20) {
            numbersNeeded++
        }
        return numbersNeeded
    }

    private fun calculateOptimalNumbersForTarget(
        redBaseCount: Int,
        blueBaseCount: Int,
        targetCombinations: Int
    ): Pair<Int, Int> {
        if (targetCombinations <= 0) return Pair(redBaseCount, blueBaseCount)
        if (redBaseCount <= 0 || blueBaseCount <= 0) return Pair(redBaseCount, blueBaseCount)
        
        var bestRed = redBaseCount
        var bestBlue = blueBaseCount
        var bestCombinations = calculateCombinations(redBaseCount, redBaseCount) * calculateCombinations(blueBaseCount, blueBaseCount)
        var bestDifference = kotlin.math.abs(bestCombinations - targetCombinations)
        
        if (bestCombinations >= targetCombinations) {
            return Pair(redBaseCount, blueBaseCount)
        }

        for (redCount in redBaseCount..15) {
            for (blueCount in blueBaseCount..5) {
                val combinations = calculateCombinations(redCount, redBaseCount) * calculateCombinations(blueCount, blueBaseCount)
                val difference = kotlin.math.abs(combinations - targetCombinations)
                
                if (combinations >= targetCombinations) {
                    if (difference < bestDifference || bestCombinations < targetCombinations) {
                        bestRed = redCount
                        bestBlue = blueCount
                        bestCombinations = combinations
                        bestDifference = difference
                    }
                    break
                }
            }
            if (bestCombinations >= targetCombinations) break
        }
        
        return Pair(bestRed, bestBlue)
    }


    private fun applyComplexOptimization(
        numbers: List<Int>,
        range: IntRange,
        algorithms: Set<PredictionAlgorithm>
    ): List<Int> {
        var optimized = numbers.toList()


        if (PredictionAlgorithm.SUM_VALUE in algorithms) {
            optimized = optimizeComplexSumValue(optimized, range)
        }


        if (PredictionAlgorithm.SPAN in algorithms) {
            optimized = optimizeComplexSpan(optimized, range)
        }


        if (PredictionAlgorithm.AC_VALUE in algorithms) {
            optimized = optimizeComplexAcValue(optimized, range)
        }


        if (PredictionAlgorithm.ODD_EVEN in algorithms) {
            optimized = optimizeComplexOddEven(optimized, range)
        }


        if (PredictionAlgorithm.PRIME_COMPOSITE in algorithms) {
            optimized = optimizeComplexPrimeComposite(optimized, range)
        }


        if (PredictionAlgorithm.ZONE in algorithms) {
            optimized = optimizeComplexZoneDistribution(optimized, range)
        }


        if (PredictionAlgorithm.BALANCE in algorithms) {
            optimized = optimizeComplexBalance(optimized, range)
        }

        return optimized
    }


    private fun optimizeComplexSumValue(numbers: List<Int>, range: IntRange): List<Int> {
        val sum = numbers.sum()
        val mid = range.sum() * numbers.size / range.count()
        val targetRange = (mid * 0.7).toInt()..(mid * 1.3).toInt()

        if (sum in targetRange) return numbers


        val mutable = numbers.toMutableList()
        if (sum < targetRange.first) {
            val minIdx = mutable.indexOf(mutable.minOrNull())
            val candidates = range.filter { it !in mutable && it > mutable[minIdx] }
            if (candidates.isNotEmpty()) {
                mutable[minIdx] = candidates.random()
            }
        } else if (sum > targetRange.last) {
            val maxIdx = mutable.indexOf(mutable.maxOrNull())
            val candidates = range.filter { it !in mutable && it < mutable[maxIdx] }
            if (candidates.isNotEmpty()) {
                mutable[maxIdx] = candidates.random()
            }
        }

        return mutable
    }


    private fun optimizeComplexSpan(numbers: List<Int>, range: IntRange): List<Int> {
        val span = (numbers.maxOrNull() ?: 0) - (numbers.minOrNull() ?: 0)
        val targetSpan = 10..30

        if (span in targetSpan) return numbers

        val mutable = numbers.toMutableList()
        if (span < targetSpan.first) {

            val candidates = range.filter { it !in mutable }
            if (candidates.isNotEmpty()) {
                mutable[mutable.size / 2] = candidates.random()
            }
        }

        return mutable
    }


    private fun optimizeComplexAcValue(numbers: List<Int>, range: IntRange): List<Int> {
        val acValue = calculateAcValue(numbers)
        val targetAc = 5..9

        if (acValue in targetAc) return numbers


        val mutable = numbers.toMutableList()
        val candidates = range.filter { it !in mutable }
        if (candidates.isNotEmpty() && acValue < targetAc.first) {
            mutable[Random.nextInt(mutable.size)] = candidates.random()
        }

        return mutable
    }


    private fun optimizeComplexOddEven(numbers: List<Int>, range: IntRange): List<Int> {
        val oddCount = numbers.count { it % 2 == 1 }
        val targetOdd = 1..5

        if (oddCount in targetOdd) return numbers

        val mutable = numbers.toMutableList()
        if (oddCount < targetOdd.first) {

            val evenIdx = mutable.indexOfFirst { it % 2 == 0 }
            if (evenIdx >= 0) {
                val oddCandidates = range.filter { it % 2 == 1 && it !in mutable }
                if (oddCandidates.isNotEmpty()) {
                    mutable[evenIdx] = oddCandidates.random()
                }
            }
        } else if (oddCount > targetOdd.last) {

            val oddIdx = mutable.indexOfFirst { it % 2 == 1 }
            if (oddIdx >= 0) {
                val evenCandidates = range.filter { it % 2 == 0 && it !in mutable }
                if (evenCandidates.isNotEmpty()) {
                    mutable[oddIdx] = evenCandidates.random()
                }
            }
        }

        return mutable
    }


    private fun optimizeComplexPrimeComposite(numbers: List<Int>, range: IntRange): List<Int> {
        val primeCount = numbers.count { isPrime(it) }
        val targetPrime = 1..5

        if (primeCount in targetPrime) return numbers

        val mutable = numbers.toMutableList()
        if (primeCount < targetPrime.first) {

            val compositeIdx = mutable.indexOfFirst { !isPrime(it) }
            if (compositeIdx >= 0) {
                val primeCandidates = range.filter { isPrime(it) && it !in mutable }
                if (primeCandidates.isNotEmpty()) {
                    mutable[compositeIdx] = primeCandidates.random()
                }
            }
        }

        return mutable
    }


    private fun optimizeComplexZoneDistribution(numbers: List<Int>, range: IntRange): List<Int> {
        val zoneSize = range.count() / 3
        val zone1 = range.first until (range.first + zoneSize)
        val zone2 = (range.first + zoneSize) until (range.first + zoneSize * 2)
        val zone3 = (range.first + zoneSize * 2)..range.last

        val zone1Count = numbers.count { it in zone1 }
        val zone2Count = numbers.count { it in zone2 }
        val zone3Count = numbers.count { it in zone3 }


        if (maxOf(zone1Count, zone2Count, zone3Count) - minOf(zone1Count, zone2Count, zone3Count) <= 2) {
            return numbers
        }


        val mutable = numbers.toMutableList()

        return mutable
    }


    private fun optimizeComplexBalance(numbers: List<Int>, range: IntRange): List<Int> {
        val mid = (range.first + range.last) / 2
        val bigCount = numbers.count { it > mid }
        val oddCount = numbers.count { it % 2 == 1 }

        val targetBig = 1..5
        val targetOdd = 1..5

        if (bigCount in targetBig && oddCount in targetOdd) return numbers

        val mutable = numbers.toMutableList()


        if (bigCount < targetBig.first) {
            val smallIdx = mutable.indexOfFirst { it <= mid }
            if (smallIdx >= 0) {
                val bigCandidates = range.filter { it > mid && it !in mutable }
                if (bigCandidates.isNotEmpty()) {
                    mutable[smallIdx] = bigCandidates.random()
                }
            }
        }

        return mutable
    }


    private fun calculateComplexBonus(
        numbers: List<Int>,
        range: IntRange,
        algorithms: Set<PredictionAlgorithm>
    ): Float {
        var bonus = 0f


        if (PredictionAlgorithm.SUM_VALUE in algorithms) {
            val sum = numbers.sum()
            val mid = range.sum() * numbers.size / range.count()
            val targetRange = (mid * 0.7).toInt()..(mid * 1.3).toInt()
            if (sum in targetRange) bonus += 0.08f
        }


        if (PredictionAlgorithm.AC_VALUE in algorithms) {
            val acValue = calculateAcValue(numbers)
            if (acValue in 5..9) bonus += 0.08f
        }


        if (PredictionAlgorithm.ODD_EVEN in algorithms) {
            val oddCount = numbers.count { it % 2 == 1 }
            if (oddCount in 1..5) bonus += 0.05f
        }


        if (PredictionAlgorithm.ZONE in algorithms) {
            val zoneSize = range.count() / 3
            val zone1 = range.first until (range.first + zoneSize)
            val zone2 = (range.first + zoneSize) until (range.first + zoneSize * 2)
            val zone3 = (range.first + zoneSize * 2)..range.last

            val zone1Count = numbers.count { it in zone1 }
            val zone2Count = numbers.count { it in zone2 }
            val zone3Count = numbers.count { it in zone3 }

            if (maxOf(zone1Count, zone2Count, zone3Count) - minOf(zone1Count, zone2Count, zone3Count) <= 2) {
                bonus += 0.05f
            }
        }

        return bonus
    }


    private fun calculateCombinationCount(
        redNumbers: List<String>,
        blueNumbers: List<String>,
        config: LotteryConfig
    ): Int {
        val redCount = redNumbers.size
        val blueCount = blueNumbers.size

        return when {
            redCount > config.redCount && blueCount > config.blueCount -> {
                val redCombinations = calculateCombinations(redCount, config.redCount)
                val blueCombinations = calculateCombinations(blueCount, config.blueCount)
                redCombinations * blueCombinations
            }
            redCount > config.redCount -> calculateCombinations(redCount, config.redCount)
            blueCount > config.blueCount -> calculateCombinations(blueCount, config.blueCount)
            else -> 1
        }
    }


    private fun calculateCombinations(n: Int, r: Int): Int {
        if (r > n || r < 0) return 0
        if (r == 0 || r == n) return 1

        var result = 1
        for (i in 0 until r) {
            result = result * (n - i) / (i + 1)
        }
        return result
    }


    private fun calculateCoverageRate(
        redNumbers: List<String>,
        blueNumbers: List<String>,
        redScores: Map<String, NumberScore>,
        blueScores: Map<String, NumberScore>
    ): Float {
        val totalRedScore = redNumbers.sumOf { (redScores[it]?.totalScore ?: 0f).toDouble() }
        val totalBlueScore = blueNumbers.sumOf { (blueScores[it]?.totalScore ?: 0f).toDouble() }
        val maxPossibleScore = (redNumbers.size + blueNumbers.size) * 100f

        return ((totalRedScore + totalBlueScore) / maxPossibleScore).toFloat().coerceIn(0f, 1f)
    }


    private fun identifyHotNumbers(
        redNumbers: List<String>,
        redScores: Map<String, NumberScore>
    ): List<String> {
        return redNumbers.filter { number ->
            val score = redScores[number]?.totalScore ?: 0f
            score > 0.7f
        }
    }


    private fun identifyColdNumbers(
        redNumbers: List<String>,
        redScores: Map<String, NumberScore>
    ): List<String> {
        return redNumbers.filter { number ->
            val score = redScores[number]?.totalScore ?: 0f
            score < 0.3f
        }
    }


    private fun generateComplexExplanation(
        redNumbers: List<String>,
        blueNumbers: List<String>,
        redScores: Map<String, NumberScore>,
        blueScores: Map<String, NumberScore>,
        algorithms: Set<PredictionAlgorithm>,
        combinationCount: Int,
        coverageRate: Float
    ): String {
        val parts = mutableListOf<String>()


        val hotNumbers = redNumbers.filter { 
            (redScores[it]?.totalScore ?: 0f) > 0.7f 
        }
        if (hotNumbers.isNotEmpty()) {
            parts.add("热号: ${hotNumbers.joinToString(",")}")
        }


        val coldNumbers = redNumbers.filter {
            (redScores[it]?.totalScore ?: 0f) < 0.3f
        }
        if (coldNumbers.isNotEmpty()) {
            parts.add("冷号: ${coldNumbers.joinToString(",")}")
        }


        val nums = redNumbers.map { it.toInt() }
        parts.add("和值=${nums.sum()}")
        parts.add("跨度=${(nums.maxOrNull()?:0)-(nums.minOrNull()?:0)}")


        parts.add("组合数=${combinationCount}")
        parts.add("覆盖率=${(coverageRate * 100).toInt()}%")

        if (parts.isEmpty()) {
            parts.add("复式票综合推荐")
        }

        return parts.joinToString(" | ")
    }


    private fun calculateCombinatorialBonus(
        numbers: List<Int>,
        range: IntRange,
        algorithms: Set<PredictionAlgorithm>
    ): Float {
        var bonus = 0f


        if (PredictionAlgorithm.SUM_VALUE in algorithms) {
            val sum = numbers.sum()
            val mid = range.sum() * numbers.size / range.count()
            val targetRange = (mid * 0.8).toInt()..(mid * 1.2).toInt()
            if (sum in targetRange) bonus += 0.05f
        }


        if (PredictionAlgorithm.AC_VALUE in algorithms) {
            val acValue = calculateAcValue(numbers)
            if (acValue in 6..8) bonus += 0.05f
        }


        if (PredictionAlgorithm.ODD_EVEN in algorithms) {
            val oddCount = numbers.count { it % 2 == 1 }
            if (oddCount in 2..4) bonus += 0.03f
        }

        return bonus
    }

    private fun calculateAlgorithmScore(
        numbers: List<String>,
        allScores: Map<String, NumberScore>,
        algorithm: PredictionAlgorithm
    ): Float {
        return numbers.mapNotNull { num ->
            val score = allScores[num] ?: return@mapNotNull null
            when (algorithm) {
                PredictionAlgorithm.FREQUENCY -> score.frequencyScore
                PredictionAlgorithm.OMISSION -> score.omissionScore
                PredictionAlgorithm.TREND -> score.trendScore
                PredictionAlgorithm.ASSOCIATION -> score.associationScore
                PredictionAlgorithm.CONSECUTIVE -> score.consecutiveScore
                PredictionAlgorithm.SAME_TAIL -> score.sameTailScore
                PredictionAlgorithm.MARKOV -> score.markovScore
                PredictionAlgorithm.BAYES -> score.bayesScore
                PredictionAlgorithm.LSTM -> score.lstmScore
                PredictionAlgorithm.GENETIC -> score.geneticScore
                PredictionAlgorithm.SUM_VALUE -> score.sumValueScore
                PredictionAlgorithm.SPAN -> score.spanScore
                PredictionAlgorithm.AC_VALUE -> score.acValueScore
                PredictionAlgorithm.ODD_EVEN -> score.oddEvenScore
                PredictionAlgorithm.PRIME_COMPOSITE -> score.primeCompositeScore
                PredictionAlgorithm.ZONE -> score.zoneScore
                PredictionAlgorithm.BALANCE -> score.totalScore
            }
        }.map { it.toDouble() }.average().toFloat() * 100
    }

    private fun generateExplanation(
        redNumbers: List<String>,
        blueNumbers: List<String>,
        redScores: Map<String, NumberScore>,
        blueScores: Map<String, NumberScore>,
        algorithms: Set<PredictionAlgorithm>
    ): String {
        val parts = mutableListOf<String>()


        val highFreqRed = redNumbers.filter { 
            (redScores[it]?.frequencyScore ?: 0f) > 0.7f 
        }
        if (highFreqRed.isNotEmpty()) {
            parts.add("高频: ${highFreqRed.joinToString(",")}")
        }


        val highOmissionRed = redNumbers.filter {
            (redScores[it]?.omissionScore ?: 0f) > 0.7f
        }
        if (highOmissionRed.isNotEmpty()) {
            parts.add("遗漏回补: ${highOmissionRed.joinToString(",")}")
        }


        val trendingRed = redNumbers.filter {
            (redScores[it]?.trendScore ?: 0f) > 0.7f
        }
        if (trendingRed.isNotEmpty()) {
            parts.add("趋势↑: ${trendingRed.joinToString(",")}")
        }


        val nums = redNumbers.map { it.toInt() }
        parts.add("和值=${nums.sum()}")
        parts.add("跨度=${(nums.maxOrNull()?:0)-(nums.minOrNull()?:0)}")

        if (parts.isEmpty()) {
            parts.add("综合算法推荐")
        }

        return parts.joinToString(" | ")
    }

    private fun getLotteryConfig(type: LotteryType): LotteryConfig {
        return when (type) {
            LotteryType.SSQ -> LotteryConfig(
                redRange = 1..33,
                blueRange = 1..16,
                redCount = 6,
                blueCount = 1
            )
            LotteryType.CJDLT -> LotteryConfig(
                redRange = 1..35,
                blueRange = 1..12,
                redCount = 5,
                blueCount = 2
            )
            LotteryType.QLC -> LotteryConfig(
                redRange = 1..30,
                blueRange = 1..30,  
                redCount = 7,
                blueCount = 1  
            )
            LotteryType.FC3D -> LotteryConfig(
                redRange = 0..9,
                blueRange = 0..9,
                redCount = 3,  
                blueCount = 0
            )
            LotteryType.QXC -> LotteryConfig(
                redRange = 0..9,
                blueRange = 0..14,  
                redCount = 6,  
                blueCount = 1  
            )
            LotteryType.PL3 -> LotteryConfig(
                redRange = 0..9,
                blueRange = 0..9,
                redCount = 3,  
                blueCount = 0
            )
            LotteryType.PL5 -> LotteryConfig(
                redRange = 0..9,
                blueRange = 0..9,
                redCount = 5,  
                blueCount = 0
            )
            LotteryType.KL8 -> LotteryConfig(
                redRange = 1..80,
                blueRange = 1..1,
                redCount = 20,  
                blueCount = 0
            )
        }
    }



    private data class LotteryConfig(
        val redRange: IntRange,
        val blueRange: IntRange,
        val redCount: Int,
        val blueCount: Int
    )


    private data class SharedData(
        val historySize: Int,
        val redFrequencyMap: Map<String, Int>,
        val blueFrequencyMap: Map<String, Int>,
        val recentHistory: List<DrawResult>,
        val olderHistory: List<DrawResult>
    )
}

