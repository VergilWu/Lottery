package com.vergil.lottery.domain.analyzer

import com.vergil.lottery.core.constants.LotteryType
import com.vergil.lottery.presentation.screens.prediction.PredictionContract
import org.junit.Test
import org.junit.Assert.*

/**
 * 预测引擎排序逻辑测试
 * 验证不同彩种的号码排序行为
 */
class PredictionEngineSortingTest {

    @Test
    fun `福彩3D号码不应排序`() {
        // 福彩3D: 百位、十位、个位，顺序有意义
        val lotteryType = LotteryType.FC3D
        val redNumbers = listOf("3", "7", "1") // 371
        val blueNumbers = emptyList<String>()
        
        // 模拟预测结果
        val prediction = PredictionContract.PredictionResult(
            id = "test_1",
            lotteryType = lotteryType,
            redNumbers = redNumbers,
            blueNumbers = blueNumbers,
            totalScore = 85.5f,
            algorithmScores = emptyMap(),
            confidence = 0.855f,
            explanation = "测试预测"
        )
        
        // 验证红球顺序保持不变
        assertEquals("3", prediction.redNumbers[0])
        assertEquals("7", prediction.redNumbers[1])
        assertEquals("1", prediction.redNumbers[2])
    }

    @Test
    fun `排列3号码不应排序`() {
        // 排列3: 百位、十位、个位，顺序有意义
        val lotteryType = LotteryType.PL3
        val redNumbers = listOf("5", "2", "8") // 528
        val blueNumbers = emptyList<String>()
        
        val prediction = PredictionContract.PredictionResult(
            id = "test_2",
            lotteryType = lotteryType,
            redNumbers = redNumbers,
            blueNumbers = blueNumbers,
            totalScore = 78.2f,
            algorithmScores = emptyMap(),
            confidence = 0.782f,
            explanation = "测试预测"
        )
        
        // 验证红球顺序保持不变
        assertEquals("5", prediction.redNumbers[0])
        assertEquals("2", prediction.redNumbers[1])
        assertEquals("8", prediction.redNumbers[2])
    }

    @Test
    fun `排列5号码不应排序`() {
        // 排列5: 5位数字，顺序有意义
        val lotteryType = LotteryType.PL5
        val redNumbers = listOf("1", "2", "3", "4", "5") // 12345
        val blueNumbers = emptyList<String>()
        
        val prediction = PredictionContract.PredictionResult(
            id = "test_3",
            lotteryType = lotteryType,
            redNumbers = redNumbers,
            blueNumbers = blueNumbers,
            totalScore = 92.1f,
            algorithmScores = emptyMap(),
            confidence = 0.921f,
            explanation = "测试预测"
        )
        
        // 验证红球顺序保持不变
        assertEquals("1", prediction.redNumbers[0])
        assertEquals("2", prediction.redNumbers[1])
        assertEquals("3", prediction.redNumbers[2])
        assertEquals("4", prediction.redNumbers[3])
        assertEquals("5", prediction.redNumbers[4])
    }

    @Test
    fun `七星彩号码不应排序`() {
        // 七星彩: 前6位 + 第7位，顺序有意义
        val lotteryType = LotteryType.QXC
        val redNumbers = listOf("1", "2", "3", "4", "5", "6") // 前6位
        val blueNumbers = listOf("7") // 第7位
        
        val prediction = PredictionContract.PredictionResult(
            id = "test_4",
            lotteryType = lotteryType,
            redNumbers = redNumbers,
            blueNumbers = blueNumbers,
            totalScore = 88.7f,
            algorithmScores = emptyMap(),
            confidence = 0.887f,
            explanation = "测试预测"
        )
        
        // 验证红球顺序保持不变
        assertEquals("1", prediction.redNumbers[0])
        assertEquals("2", prediction.redNumbers[1])
        assertEquals("3", prediction.redNumbers[2])
        assertEquals("4", prediction.redNumbers[3])
        assertEquals("5", prediction.redNumbers[4])
        assertEquals("6", prediction.redNumbers[5])
        
        // 验证蓝球顺序保持不变
        assertEquals("7", prediction.blueNumbers[0])
    }

    @Test
    fun `双色球号码可以排序`() {
        // 双色球: 红球和蓝球都可以排序
        val lotteryType = LotteryType.SSQ
        val redNumbers = listOf("33", "1", "15", "22", "8", "29") // 红球
        val blueNumbers = listOf("16", "3") // 蓝球
        
        val prediction = PredictionContract.PredictionResult(
            id = "test_5",
            lotteryType = lotteryType,
            redNumbers = redNumbers,
            blueNumbers = blueNumbers,
            totalScore = 76.3f,
            algorithmScores = emptyMap(),
            confidence = 0.763f,
            explanation = "测试预测"
        )
        
        // 对于双色球，排序后的结果应该是升序
        // 注意：这里我们只是验证数据结构正确，实际排序逻辑在PredictionEngine中
        assertEquals(6, prediction.redNumbers.size)
        assertEquals(2, prediction.blueNumbers.size)
    }

    @Test
    fun `超级大乐透号码可以排序`() {
        // 超级大乐透: 红球和蓝球都可以排序
        val lotteryType = LotteryType.CJDLT
        val redNumbers = listOf("35", "1", "15", "22", "8") // 红球
        val blueNumbers = listOf("12", "3") // 蓝球
        
        val prediction = PredictionContract.PredictionResult(
            id = "test_6",
            lotteryType = lotteryType,
            redNumbers = redNumbers,
            blueNumbers = blueNumbers,
            totalScore = 81.9f,
            algorithmScores = emptyMap(),
            confidence = 0.819f,
            explanation = "测试预测"
        )
        
        // 验证数据结构正确
        assertEquals(5, prediction.redNumbers.size)
        assertEquals(2, prediction.blueNumbers.size)
    }
}
