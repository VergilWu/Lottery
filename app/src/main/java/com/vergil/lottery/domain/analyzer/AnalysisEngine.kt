package com.vergil.lottery.domain.analyzer

import com.vergil.lottery.core.constants.LotteryType
import com.vergil.lottery.domain.model.DrawResult
import kotlin.math.abs


class AnalysisEngine {


    data class OmissionData(
        val number: String,           
        val currentOmission: Int,     
        val maxOmission: Int,         
        val avgOmission: Float        
    )


    data class FrequencyData(
        val number: String,           
        val count: Int,               
        val frequency: Float,         
        val lastAppearIssue: String?  
    )


    data class HotColdData(
        val number: String,           
        val temperature: Float,       
        val category: HotColdCategory, 
        val recentCount: Int          
    )

    enum class HotColdCategory {
        HOT,      
        WARM,     
        COLD      
    }


    data class ConsecutiveData(
        val numbers: List<String>,    
        val count: Int,               
        val frequency: Float          
    )


    data class SameTailData(
        val tail: Int,                
        val numbers: List<String>,    
        val count: Int,               
        val frequency: Float          
    )


    data class SumValueData(
        val sumValue: Int,            
        val count: Int,               
        val frequency: Float,         
        val oddEvenRatio: String      
    )


    data class SpanData(
        val span: Int,                
        val count: Int,               
        val frequency: Float          
    )


    data class ACValueData(
        val acValue: Int,             
        val count: Int,               
        val frequency: Float          
    )


    data class OddEvenRatioData(
        val ratio: String,            
        val count: Int,               
        val frequency: Float          
    )


    data class SizeRatioData(
        val ratio: String,            
        val count: Int,               
        val frequency: Float          
    )


    data class PrimeCompositeRatioData(
        val ratio: String,            
        val count: Int,               
        val frequency: Float          
    )


    data class ZoneData(
        val zone: Int,                
        val range: String,            
        val count: Int,               
        val frequency: Float          
    )




    fun analyzeOmission(
        history: List<DrawResult>,
        lotteryType: LotteryType,
        isRed: Boolean = true
    ): List<OmissionData> {
        val config = lotteryType.getConfig()
        val range = if (isRed) config.redRange else config.blueRange

        return range.map { num ->
            val numStr = num.toString().padStart(2, '0')
            val omissions = mutableListOf<Int>()
            var currentOmission = 0


            for (draw in history) {
                val numbers = if (isRed) draw.red else draw.blue
                if (numbers.contains(numStr)) {
                    omissions.add(currentOmission)
                    currentOmission = 0
                } else {
                    currentOmission++
                }
            }

            OmissionData(
                number = numStr,
                currentOmission = currentOmission,
                maxOmission = omissions.maxOrNull() ?: currentOmission,
                avgOmission = if (omissions.isNotEmpty()) omissions.average().toFloat() else currentOmission.toFloat()
            )
        }.sortedByDescending { it.currentOmission }
    }


    fun analyzeFrequency(
        history: List<DrawResult>,
        lotteryType: LotteryType,
        isRed: Boolean = true
    ): List<FrequencyData> {
        val config = lotteryType.getConfig()
        val range = if (isRed) config.redRange else config.blueRange
        val countMap = mutableMapOf<String, Int>()
        val lastAppearMap = mutableMapOf<String, String>()

        history.forEach { draw ->
            val numbers = if (isRed) draw.red else draw.blue
            numbers.forEach { num ->
                countMap[num] = (countMap[num] ?: 0) + 1
                if (lastAppearMap[num] == null) {
                    lastAppearMap[num] = draw.issue
                }
            }
        }

        return range.map { num ->
            val numStr = num.toString().padStart(2, '0')
            val count = countMap[numStr] ?: 0
            FrequencyData(
                number = numStr,
                count = count,
                frequency = if (history.isNotEmpty()) (count.toFloat() / history.size) * 100f else 0f,
                lastAppearIssue = lastAppearMap[numStr]
            )
        }.sortedByDescending { it.count }
    }


    fun analyzeHotCold(
        history: List<DrawResult>,
        lotteryType: LotteryType,
        isRed: Boolean = true,
        recentPeriods: Int = 20
    ): List<HotColdData> {
        val recentHistory = history.take(recentPeriods.coerceAtMost(history.size))
        val config = lotteryType.getConfig()
        val range = if (isRed) config.redRange else config.blueRange

        val countMap = mutableMapOf<String, Int>()
        recentHistory.forEach { draw ->
            val numbers = if (isRed) draw.red else draw.blue
            numbers.forEach { num ->
                countMap[num] = (countMap[num] ?: 0) + 1
            }
        }

        val maxCount = countMap.values.maxOrNull() ?: 1

        return range.map { num ->
            val numStr = num.toString().padStart(2, '0')
            val count = countMap[numStr] ?: 0
            val temperature = (count.toFloat() / maxCount) * 100f

            val category = when {
                temperature >= 60f -> HotColdCategory.HOT
                temperature >= 30f -> HotColdCategory.WARM
                else -> HotColdCategory.COLD
            }

            HotColdData(
                number = numStr,
                temperature = temperature,
                category = category,
                recentCount = count
            )
        }.sortedByDescending { it.temperature }
    }


    fun analyzeConsecutive(
        history: List<DrawResult>,
        isRed: Boolean = true
    ): List<ConsecutiveData> {
        val consecutiveMap = mutableMapOf<String, Int>()

        history.forEach { draw ->
            val numbers = if (isRed) draw.red else draw.blue
            val sortedNums = numbers.map { it.toInt() }.sorted()


            for (i in sortedNums.indices) {
                for (j in i + 1 until sortedNums.size) {
                    if (sortedNums[j] - sortedNums[i] == j - i) {

                        val consecutive = sortedNums.subList(i, j + 1)
                            .joinToString(",") { it.toString().padStart(2, '0') }
                        consecutiveMap[consecutive] = (consecutiveMap[consecutive] ?: 0) + 1
                    }
                }
            }
        }

        return consecutiveMap.entries
            .filter { it.value > 1 }  
            .map { (nums, count) ->
                ConsecutiveData(
                    numbers = nums.split(","),
                    count = count,
                    frequency = (count.toFloat() / history.size) * 100f
                )
            }
            .sortedByDescending { it.count }
            .take(20)  
    }


    fun analyzeSameTail(
        history: List<DrawResult>,
        lotteryType: LotteryType,
        isRed: Boolean = true
    ): List<SameTailData> {
        val tailMap = mutableMapOf<Int, MutableList<String>>()
        val tailCountMap = mutableMapOf<Int, Int>()


        for (tail in 0..9) {
            tailMap[tail] = mutableListOf()
        }

        val config = lotteryType.getConfig()
        val range = if (isRed) config.redRange else config.blueRange


        range.forEach { num ->
            val numStr = num.toString().padStart(2, '0')
            val tail = num % 10
            tailMap[tail]?.add(numStr)
        }


        history.forEach { draw ->
            val numbers = if (isRed) draw.red else draw.blue
            numbers.forEach { num ->
                val tail = num.toInt() % 10
                tailCountMap[tail] = (tailCountMap[tail] ?: 0) + 1
            }
        }

        return tailMap.entries.map { (tail, numbers) ->
            val count = tailCountMap[tail] ?: 0
            SameTailData(
                tail = tail,
                numbers = numbers,
                count = count,
                frequency = if (history.isNotEmpty()) (count.toFloat() / (history.size * (if (isRed) 6 else 1))) * 100f else 0f
            )
        }.sortedByDescending { it.count }
    }


    fun analyzeSumValue(
        history: List<DrawResult>,
        isRed: Boolean = true
    ): List<SumValueData> {
        val sumMap = mutableMapOf<Int, Int>()
        val oddEvenMap = mutableMapOf<Int, Pair<Int, Int>>()  

        history.forEach { draw ->
            val numbers = if (isRed) draw.red else draw.blue
            val sum = numbers.sumOf { it.toInt() }
            sumMap[sum] = (sumMap[sum] ?: 0) + 1


            val oddCount = numbers.count { it.toInt() % 2 == 1 }
            val evenCount = numbers.size - oddCount
            oddEvenMap[sum] = Pair(oddCount, evenCount)
        }

        return sumMap.entries.map { (sum, count) ->
            val (oddCount, evenCount) = oddEvenMap[sum] ?: Pair(0, 0)
            SumValueData(
                sumValue = sum,
                count = count,
                frequency = (count.toFloat() / history.size) * 100f,
                oddEvenRatio = "$oddCount:$evenCount"
            )
        }.sortedByDescending { it.count }
    }


    fun analyzeSpan(
        history: List<DrawResult>,
        isRed: Boolean = true
    ): List<SpanData> {
        val spanMap = mutableMapOf<Int, Int>()

        history.forEach { draw ->
            val numbers = if (isRed) draw.red else draw.blue
            if (numbers.isNotEmpty()) {
                val nums = numbers.map { it.toInt() }
                val span = nums.maxOrNull()!! - nums.minOrNull()!!
                spanMap[span] = (spanMap[span] ?: 0) + 1
            }
        }

        return spanMap.entries.map { (span, count) ->
            SpanData(
                span = span,
                count = count,
                frequency = (count.toFloat() / history.size) * 100f
            )
        }.sortedBy { it.span }
    }


    fun analyzeACValue(
        history: List<DrawResult>,
        isRed: Boolean = true
    ): List<ACValueData> {
        val acMap = mutableMapOf<Int, Int>()

        history.forEach { draw ->
            val numbers = if (isRed) draw.red else draw.blue
            if (numbers.size > 1) {
                val nums = numbers.map { it.toInt() }.sorted()
                val differences = mutableSetOf<Int>()


                for (i in nums.indices) {
                    for (j in i + 1 until nums.size) {
                        differences.add(abs(nums[j] - nums[i]))
                    }
                }

                val acValue = differences.size - (nums.size - 1)
                acMap[acValue] = (acMap[acValue] ?: 0) + 1
            }
        }

        return acMap.entries.map { (ac, count) ->
            ACValueData(
                acValue = ac,
                count = count,
                frequency = (count.toFloat() / history.size) * 100f
            )
        }.sortedBy { it.acValue }
    }


    fun analyzeOddEvenRatio(
        history: List<DrawResult>,
        isRed: Boolean = true
    ): List<OddEvenRatioData> {
        val ratioMap = mutableMapOf<String, Int>()

        history.forEach { draw ->
            val numbers = if (isRed) draw.red else draw.blue
            val oddCount = numbers.count { it.toInt() % 2 == 1 }
            val evenCount = numbers.size - oddCount
            val ratio = "$oddCount:$evenCount"
            ratioMap[ratio] = (ratioMap[ratio] ?: 0) + 1
        }

        return ratioMap.entries.map { (ratio, count) ->
            OddEvenRatioData(
                ratio = ratio,
                count = count,
                frequency = (count.toFloat() / history.size) * 100f
            )
        }.sortedByDescending { it.count }
    }


    fun analyzeSizeRatio(
        history: List<DrawResult>,
        lotteryType: LotteryType,
        isRed: Boolean = true
    ): List<SizeRatioData> {
        val config = lotteryType.getConfig()
        val range = if (isRed) config.redRange else config.blueRange
        val median = (range.first + range.last) / 2
        val ratioMap = mutableMapOf<String, Int>()

        history.forEach { draw ->
            val numbers = if (isRed) draw.red else draw.blue
            val bigCount = numbers.count { it.toInt() > median }
            val smallCount = numbers.size - bigCount
            val ratio = "$bigCount:$smallCount"
            ratioMap[ratio] = (ratioMap[ratio] ?: 0) + 1
        }

        return ratioMap.entries.map { (ratio, count) ->
            SizeRatioData(
                ratio = ratio,
                count = count,
                frequency = (count.toFloat() / history.size) * 100f
            )
        }.sortedByDescending { it.count }
    }


    fun analyzePrimeCompositeRatio(
        history: List<DrawResult>,
        isRed: Boolean = true
    ): List<PrimeCompositeRatioData> {
        val ratioMap = mutableMapOf<String, Int>()

        history.forEach { draw ->
            val numbers = if (isRed) draw.red else draw.blue
            val primeCount = numbers.count { isPrime(it.toInt()) }
            val compositeCount = numbers.size - primeCount
            val ratio = "$primeCount:$compositeCount"
            ratioMap[ratio] = (ratioMap[ratio] ?: 0) + 1
        }

        return ratioMap.entries.map { (ratio, count) ->
            PrimeCompositeRatioData(
                ratio = ratio,
                count = count,
                frequency = (count.toFloat() / history.size) * 100f
            )
        }.sortedByDescending { it.count }
    }


    fun analyzeZone(
        history: List<DrawResult>,
        lotteryType: LotteryType,
        isRed: Boolean = true,
        zoneCount: Int = 3  
    ): List<ZoneData> {
        val config = lotteryType.getConfig()
        val range = if (isRed) config.redRange else config.blueRange
        val zoneSize = (range.last - range.first + 1) / zoneCount
        val zoneCountMap = mutableMapOf<Int, Int>()

        history.forEach { draw ->
            val numbers = if (isRed) draw.red else draw.blue
            numbers.forEach { num ->
                val numInt = num.toInt()
                val zone = ((numInt - range.first) / zoneSize).coerceAtMost(zoneCount - 1)
                zoneCountMap[zone] = (zoneCountMap[zone] ?: 0) + 1
            }
        }

        return (0 until zoneCount).map { zone ->
            val zoneStart = range.first + zone * zoneSize
            val zoneEnd = if (zone == zoneCount - 1) range.last else zoneStart + zoneSize - 1
            val count = zoneCountMap[zone] ?: 0

            ZoneData(
                zone = zone + 1,
                range = "${zoneStart.toString().padStart(2, '0')}-${zoneEnd.toString().padStart(2, '0')}",
                count = count,
                frequency = if (history.isNotEmpty()) (count.toFloat() / (history.size * (if (isRed) 6 else 1))) * 100f else 0f
            )
        }
    }




    private fun isPrime(n: Int): Boolean {
        if (n <= 1) return false
        if (n == 2) return true
        if (n % 2 == 0) return false

        val sqrt = kotlin.math.sqrt(n.toDouble()).toInt()
        for (i in 3..sqrt step 2) {
            if (n % i == 0) return false
        }
        return true
    }


    private fun LotteryType.getConfig(): LotteryConfig {
        return when (this) {
            LotteryType.SSQ -> LotteryConfig(redRange = 1..33, blueRange = 1..16)
            LotteryType.CJDLT -> LotteryConfig(redRange = 1..35, blueRange = 1..12)
            LotteryType.QLC -> LotteryConfig(redRange = 1..30, blueRange = 1..30)
            LotteryType.FC3D -> LotteryConfig(redRange = 0..9, blueRange = 0..9)
            LotteryType.QXC -> LotteryConfig(redRange = 0..9, blueRange = 0..14)
            LotteryType.PL3 -> LotteryConfig(redRange = 0..9, blueRange = 0..9)
            LotteryType.PL5 -> LotteryConfig(redRange = 0..9, blueRange = 0..9)
            LotteryType.KL8 -> LotteryConfig(redRange = 1..80, blueRange = 1..1)
        }
    }

    private data class LotteryConfig(
        val redRange: IntRange,
        val blueRange: IntRange
    )
}

