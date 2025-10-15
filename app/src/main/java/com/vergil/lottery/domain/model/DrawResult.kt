package com.vergil.lottery.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable


@Serializable
@Immutable
data class DrawResult(
    val type: String,
    val name: String,
    val code: String,
    val issue: String,
    val red: List<String>,
    val blue: List<String>,
    val drawDate: String,
    val timeRule: String,
    val saleMoney: String?,
    val prizePool: String?,
    val winnerDetail: List<WinnerDetail>?
)


@Serializable
@Immutable
data class WinnerDetail(
    val awardEtc: String,
    val baseBetWinner: BetWinner?,
    val addToBetWinner: BetWinner?,  
    val addToBetWinner2: BetWinner?,
    val addToBetWinner3: BetWinner?
)


@Serializable
@Immutable
data class BetWinner(
    val remark: String,
    val awardNum: String,
    val awardMoney: String,
    val totalMoney: String
)

