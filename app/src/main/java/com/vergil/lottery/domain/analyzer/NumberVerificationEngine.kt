package com.vergil.lottery.domain.analyzer

import com.vergil.lottery.core.constants.LotteryType
import com.vergil.lottery.domain.model.DrawResult


class NumberVerificationEngine {


    data class VerificationResult(
        val isWin: Boolean,           
        val prizeLevel: String?,      
        val matchedRed: List<String>, 
        val matchedBlue: List<String>, 
        val message: String           
    )


    fun verify(
        inputRed: List<String>,
        inputBlue: List<String>,
        drawResult: DrawResult,
        lotteryType: LotteryType
    ): VerificationResult {
        val matchedRed = inputRed.filter { it in drawResult.red }
        val matchedBlue = inputBlue.filter { it in drawResult.blue }

        return when (lotteryType) {
            LotteryType.SSQ -> verifyShuangSeQiu(matchedRed.size, matchedBlue.size)
            LotteryType.CJDLT -> verifyDaLeTou(matchedRed.size, matchedBlue.size)
            LotteryType.QLC -> verifyQiLeCai(matchedRed.size, matchedBlue.size)
            LotteryType.FC3D -> verifyFuCai3D(inputRed, drawResult.red)
            LotteryType.QXC -> verifyQiXingCai(inputRed + inputBlue, drawResult.red + drawResult.blue)
            LotteryType.PL3 -> verifyPaiLie3(inputRed, drawResult.red)
            LotteryType.PL5 -> verifyPaiLie5(inputRed, drawResult.red)
            LotteryType.KL8 -> verifyKuaiLe8(matchedRed.size)
        }.copy(matchedRed = matchedRed, matchedBlue = matchedBlue)
    }


    private fun verifyShuangSeQiu(redMatched: Int, blueMatched: Int): VerificationResult {
        return when {
            redMatched == 6 && blueMatched == 1 -> VerificationResult(
                isWin = true,
                prizeLevel = "一等奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "🎉 恭喜中一等奖！6+1全中"
            )
            redMatched == 6 && blueMatched == 0 -> VerificationResult(
                isWin = true,
                prizeLevel = "二等奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "🎊 恭喜中二等奖！6+0"
            )
            redMatched == 5 && blueMatched == 1 -> VerificationResult(
                isWin = true,
                prizeLevel = "三等奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "👏 恭喜中三等奖！5+1"
            )
            redMatched == 5 && blueMatched == 0 || redMatched == 4 && blueMatched == 1 -> VerificationResult(
                isWin = true,
                prizeLevel = "四等奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "🎁 恭喜中四等奖！"
            )
            redMatched == 4 && blueMatched == 0 || redMatched == 3 && blueMatched == 1 -> VerificationResult(
                isWin = true,
                prizeLevel = "五等奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "🎈 恭喜中五等奖！"
            )
            redMatched <= 2 && blueMatched == 1 -> VerificationResult(
                isWin = true,
                prizeLevel = "六等奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "🎀 恭喜中六等奖！"
            )
            else -> VerificationResult(
                isWin = false,
                prizeLevel = null,
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "未中奖，匹配 ${redMatched}个红球 ${blueMatched}个蓝球"
            )
        }
    }


    private fun verifyDaLeTou(redMatched: Int, blueMatched: Int): VerificationResult {
        return when {
            redMatched == 5 && blueMatched == 2 -> VerificationResult(
                isWin = true,
                prizeLevel = "一等奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "🎉 恭喜中一等奖！5+2全中"
            )
            redMatched == 5 && blueMatched == 1 -> VerificationResult(
                isWin = true,
                prizeLevel = "二等奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "🎊 恭喜中二等奖！5+1"
            )
            redMatched == 5 && blueMatched == 0 -> VerificationResult(
                isWin = true,
                prizeLevel = "三等奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "👏 恭喜中三等奖！5+0"
            )
            redMatched == 4 && blueMatched == 2 -> VerificationResult(
                isWin = true,
                prizeLevel = "四等奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "🎁 恭喜中四等奖！4+2"
            )
            redMatched == 4 && blueMatched == 1 || redMatched == 3 && blueMatched == 2 -> VerificationResult(
                isWin = true,
                prizeLevel = "五等奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "🎈 恭喜中五等奖！"
            )
            redMatched == 4 && blueMatched == 0 || redMatched == 3 && blueMatched == 1 || redMatched == 2 && blueMatched == 2 -> VerificationResult(
                isWin = true,
                prizeLevel = "六等奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "🎀 恭喜中六等奖！"
            )
            redMatched == 3 && blueMatched == 0 || redMatched == 2 && blueMatched == 1 || redMatched == 1 && blueMatched == 2 || redMatched == 0 && blueMatched == 2 -> VerificationResult(
                isWin = true,
                prizeLevel = "七等奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "🎁 恭喜中七等奖！"
            )
            else -> VerificationResult(
                isWin = false,
                prizeLevel = null,
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "未中奖，匹配 ${redMatched}个红球 ${blueMatched}个蓝球"
            )
        }
    }


    private fun verifyQiLeCai(redMatched: Int, blueMatched: Int): VerificationResult {
        return when {
            redMatched == 7 -> VerificationResult(
                isWin = true,
                prizeLevel = "一等奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "🎉 恭喜中一等奖！基本号全中"
            )
            redMatched == 6 && blueMatched == 1 -> VerificationResult(
                isWin = true,
                prizeLevel = "二等奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "🎊 恭喜中二等奖！6+特别号"
            )
            redMatched == 6 -> VerificationResult(
                isWin = true,
                prizeLevel = "三等奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "👏 恭喜中三等奖！6个基本号"
            )
            redMatched == 5 && blueMatched == 1 -> VerificationResult(
                isWin = true,
                prizeLevel = "四等奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "🎁 恭喜中四等奖！5+特别号"
            )
            redMatched == 5 -> VerificationResult(
                isWin = true,
                prizeLevel = "五等奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "🎈 恭喜中五等奖！5个基本号"
            )
            redMatched == 4 && blueMatched == 1 -> VerificationResult(
                isWin = true,
                prizeLevel = "六等奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "🎀 恭喜中六等奖！4+特别号"
            )
            redMatched == 4 -> VerificationResult(
                isWin = true,
                prizeLevel = "七等奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "🎁 恭喜中七等奖！4个基本号"
            )
            else -> VerificationResult(
                isWin = false,
                prizeLevel = null,
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "未中奖，匹配 ${redMatched}个基本号 ${blueMatched}个特别号"
            )
        }
    }


    private fun verifyFuCai3D(input: List<String>, draw: List<String>): VerificationResult {
        return when {
            input.size == 3 && input == draw -> VerificationResult(
                isWin = true,
                prizeLevel = "直选",
                matchedRed = input,
                matchedBlue = emptyList(),
                message = "🎉 恭喜中直选！号码完全匹配"
            )
            input.size == 3 && input.sorted() == draw.sorted() -> VerificationResult(
                isWin = true,
                prizeLevel = "组选",
                matchedRed = input,
                matchedBlue = emptyList(),
                message = "🎊 恭喜中组选！"
            )
            else -> VerificationResult(
                isWin = false,
                prizeLevel = null,
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "未中奖"
            )
        }
    }


    private fun verifyQiXingCai(input: List<String>, draw: List<String>): VerificationResult {
        val matchedCount = input.zip(draw).count { (a, b) -> a == b }
        return when {
            matchedCount == 7 -> VerificationResult(
                isWin = true,
                prizeLevel = "一等奖",
                matchedRed = input.take(6),
                matchedBlue = input.drop(6),
                message = "🎉 恭喜中一等奖！7位全中"
            )
            matchedCount == 6 -> VerificationResult(
                isWin = true,
                prizeLevel = "二等奖",
                matchedRed = input.take(6),
                matchedBlue = input.drop(6),
                message = "🎊 恭喜中二等奖！6位匹配"
            )
            matchedCount == 5 -> VerificationResult(
                isWin = true,
                prizeLevel = "三等奖",
                matchedRed = input.take(6),
                matchedBlue = input.drop(6),
                message = "👏 恭喜中三等奖！5位匹配"
            )
            matchedCount == 4 -> VerificationResult(
                isWin = true,
                prizeLevel = "四等奖",
                matchedRed = input.take(6),
                matchedBlue = input.drop(6),
                message = "🎁 恭喜中四等奖！4位匹配"
            )
            matchedCount == 3 -> VerificationResult(
                isWin = true,
                prizeLevel = "五等奖",
                matchedRed = input.take(6),
                matchedBlue = input.drop(6),
                message = "🎈 恭喜中五等奖！3位匹配"
            )
            matchedCount == 2 -> VerificationResult(
                isWin = true,
                prizeLevel = "六等奖",
                matchedRed = input.take(6),
                matchedBlue = input.drop(6),
                message = "🎀 恭喜中六等奖！2位匹配"
            )
            else -> VerificationResult(
                isWin = false,
                prizeLevel = null,
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "未中奖，匹配 ${matchedCount}位"
            )
        }
    }


    private fun verifyPaiLie3(input: List<String>, draw: List<String>): VerificationResult {
        return verifyFuCai3D(input, draw)
    }


    private fun verifyPaiLie5(input: List<String>, draw: List<String>): VerificationResult {
        return when {
            input.size == 5 && input == draw -> VerificationResult(
                isWin = true,
                prizeLevel = "一等奖",
                matchedRed = input,
                matchedBlue = emptyList(),
                message = "🎉 恭喜中一等奖！5位全中"
            )
            else -> VerificationResult(
                isWin = false,
                prizeLevel = null,
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "未中奖"
            )
        }
    }


    private fun verifyKuaiLe8(matched: Int): VerificationResult {
        return when {
            matched >= 10 -> VerificationResult(
                isWin = true,
                prizeLevel = "一等奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "🎉 恭喜中一等奖！选中${matched}个号码"
            )
            matched >= 8 -> VerificationResult(
                isWin = true,
                prizeLevel = "二等奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "🎊 恭喜中二等奖！选中${matched}个号码"
            )
            matched >= 6 -> VerificationResult(
                isWin = true,
                prizeLevel = "三等奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "👏 恭喜中三等奖！选中${matched}个号码"
            )
            matched >= 5 -> VerificationResult(
                isWin = true,
                prizeLevel = "四等奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "🎁 恭喜中四等奖！选中${matched}个号码"
            )
            matched == 0 -> VerificationResult(
                isWin = true,
                prizeLevel = "幸运奖",
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "🍀 恭喜中幸运奖！一个不中"
            )
            else -> VerificationResult(
                isWin = false,
                prizeLevel = null,
                matchedRed = emptyList(),
                matchedBlue = emptyList(),
                message = "未中奖，匹配 ${matched}个号码"
            )
        }
    }
}

