package com.vergil.lottery.domain.ml

import android.content.Context
import com.vergil.lottery.core.constants.LotteryType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder


class LSTMPredictor(
    private val context: Context,
    private val lotteryType: LotteryType = LotteryType.SSQ
) {

    private var redInterpreter: Interpreter? = null
    private var blueInterpreter: Interpreter? = null


    private val config: LotteryConfig = when (lotteryType) {
        LotteryType.SSQ -> LotteryConfig(
            redModelFile = "lottery_lstm_red_ssq.tflite",
            blueModelFile = "lottery_lstm_blue_ssq.tflite",
            numRedBalls = 33,
            numBlueBalls = 16
        )
        LotteryType.CJDLT -> LotteryConfig(
            redModelFile = "lottery_lstm_red_dlt.tflite",
            blueModelFile = "lottery_lstm_blue_dlt.tflite",
            numRedBalls = 35,
            numBlueBalls = 12
        )
        LotteryType.QLC -> LotteryConfig(
            redModelFile = "lottery_lstm_red_qlc.tflite",
            blueModelFile = "lottery_lstm_blue_qlc.tflite",
            numRedBalls = 30,
            numBlueBalls = 30  
        )
        LotteryType.FC3D -> LotteryConfig(
            redModelFile = "lottery_lstm_red_fc3d.tflite",
            blueModelFile = "lottery_lstm_blue_fc3d.tflite",
            numRedBalls = 10,
            numBlueBalls = 10  
        )
        LotteryType.QXC -> LotteryConfig(
            redModelFile = "lottery_lstm_red_qxc.tflite",
            blueModelFile = "lottery_lstm_blue_qxc.tflite",
            numRedBalls = 10,
            numBlueBalls = 15  
        )
        LotteryType.PL3 -> LotteryConfig(
            redModelFile = "lottery_lstm_red_pl3.tflite",
            blueModelFile = "lottery_lstm_blue_pl3.tflite",
            numRedBalls = 10,
            numBlueBalls = 10  
        )
        LotteryType.PL5 -> LotteryConfig(
            redModelFile = "lottery_lstm_red_pl5.tflite",
            blueModelFile = "lottery_lstm_blue_pl5.tflite",
            numRedBalls = 10,
            numBlueBalls = 10  
        )
        LotteryType.KL8 -> LotteryConfig(
            redModelFile = "lottery_lstm_red_kl8.tflite",
            blueModelFile = "lottery_lstm_blue_kl8.tflite",
            numRedBalls = 80,
            numBlueBalls = 1  
        )
        else -> LotteryConfig(
            redModelFile = "lottery_lstm_red_ssq.tflite",
            blueModelFile = "lottery_lstm_blue_ssq.tflite",
            numRedBalls = 33,
            numBlueBalls = 16
        )
    }

    companion object {
        private const val SEQUENCE_LENGTH = 8  
        private const val NUM_THREADS = 4      
    }


    private data class LotteryConfig(
        val redModelFile: String,
        val blueModelFile: String,
        val numRedBalls: Int,
        val numBlueBalls: Int
    )


    fun initialize(): Boolean {
        return try {

            val redModelBuffer = loadModelFile(config.redModelFile)
            redInterpreter = Interpreter(
                redModelBuffer,
                Interpreter.Options().apply {
                    setNumThreads(NUM_THREADS)


                }
            )


            val blueModelBuffer = loadModelFile(config.blueModelFile)
            blueInterpreter = Interpreter(
                blueModelBuffer,
                Interpreter.Options().apply {
                    setNumThreads(NUM_THREADS)

                }
            )

            Timber.d("✅ TensorFlow Lite LSTM 模型加载成功 (彩种: ${lotteryType.code})")
            Timber.d("   红球模型: ${config.redModelFile} (${config.numRedBalls}球)")
            Timber.d("   蓝球模型: ${config.blueModelFile} (${config.numBlueBalls}球)")
            Timber.d("   红球模型输入形状: ${redInterpreter?.getInputTensor(0)?.shape()?.contentToString()}")
            Timber.d("   红球模型输出形状: ${redInterpreter?.getOutputTensor(0)?.shape()?.contentToString()}")
            Timber.d("   蓝球模型输入形状: ${blueInterpreter?.getInputTensor(0)?.shape()?.contentToString()}")
            Timber.d("   蓝球模型输出形状: ${blueInterpreter?.getOutputTensor(0)?.shape()?.contentToString()}")

            true
        } catch (e: Exception) {
            Timber.e(e, "❌ 加载 TensorFlow Lite 模型失败 (彩种: ${lotteryType.code})")
            false
        }
    }


    private fun loadModelFile(filename: String): ByteBuffer {
        return FileUtil.loadMappedFile(context, filename)
    }


    fun predict(recentHistory: List<DrawHistory>): Pair<Map<String, Float>, Map<String, Float>>? {
        if (redInterpreter == null || blueInterpreter == null) {
            Timber.w("⚠️ 模型未初始化，无法预测")
            return null
        }

        if (recentHistory.size < SEQUENCE_LENGTH) {
            Timber.w("⚠️ 历史数据不足 (需要至少 $SEQUENCE_LENGTH 期，当前 ${recentHistory.size} 期)")
            return null
        }

        try {

            val redInput = prepareRedInput(recentHistory)
            val blueInput = prepareBlueInput(recentHistory)


            val redOutput = Array(1) { FloatArray(config.numRedBalls) }
            val blueOutput = Array(1) { FloatArray(config.numBlueBalls) }

            redInterpreter!!.run(redInput, redOutput)
            blueInterpreter!!.run(blueInput, blueOutput)


            val redProbabilities = mutableMapOf<String, Float>()
            for (i in 0 until config.numRedBalls) {
                redProbabilities[String.format("%02d", i + 1)] = redOutput[0][i]
            }

            val blueProbabilities = mutableMapOf<String, Float>()
            for (i in 0 until config.numBlueBalls) {
                blueProbabilities[String.format("%02d", i + 1)] = blueOutput[0][i]
            }

            Timber.d("🤖 LSTM 预测完成 (彩种: ${lotteryType.code})")
            Timber.d("   红球 Top 5: ${redProbabilities.entries.sortedByDescending { it.value }.take(5)}")
            Timber.d("   蓝球 Top 3: ${blueProbabilities.entries.sortedByDescending { it.value }.take(3)}")

            return Pair(redProbabilities, blueProbabilities)

        } catch (e: Exception) {
            Timber.e(e, "❌ LSTM 预测失败 (彩种: ${lotteryType.code})")
            return null
        }
    }


    private fun prepareRedInput(history: List<DrawHistory>): Array<Array<FloatArray>> {
        val input = Array(1) {
            Array(SEQUENCE_LENGTH) { FloatArray(config.numRedBalls) }
        }


        val recentDraws = history.take(SEQUENCE_LENGTH)

        for (i in 0 until SEQUENCE_LENGTH) {
            val draw = recentDraws[i]

            for (num in draw.redNumbers) {
                val index = num.toInt() - 1
                if (index in 0 until config.numRedBalls) {
                    input[0][i][index] = 1.0f
                }
            }
        }

        return input
    }


    private fun prepareBlueInput(history: List<DrawHistory>): Array<Array<FloatArray>> {
        val input = Array(1) {
            Array(SEQUENCE_LENGTH) { FloatArray(config.numBlueBalls) }
        }


        val recentDraws = history.take(SEQUENCE_LENGTH)

        for (i in 0 until SEQUENCE_LENGTH) {
            val draw = recentDraws[i]

            for (num in draw.blueNumbers) {
                val index = num.toInt() - 1
                if (index in 0 until config.numBlueBalls) {
                    input[0][i][index] = 1.0f
                }
            }
        }

        return input
    }


    fun close() {
        redInterpreter?.close()
        blueInterpreter?.close()
        redInterpreter = null
        blueInterpreter = null
        Timber.d("TensorFlow Lite 模型已关闭")
    }


    data class DrawHistory(
        val redNumbers: List<String>,
        val blueNumbers: List<String>
    )
}

