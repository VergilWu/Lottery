package com.vergil.lottery.presentation.screens.home

import androidx.compose.runtime.Immutable
import com.vergil.lottery.core.constants.LotteryType
import com.vergil.lottery.core.mvi.MviEffect
import com.vergil.lottery.core.mvi.MviIntent
import com.vergil.lottery.core.mvi.MviState
import com.vergil.lottery.domain.analyzer.NumberVerificationEngine
import com.vergil.lottery.domain.model.DrawResult


object HomeContract {


    sealed interface Intent : MviIntent {
        data object LoadAllLatestDraws : Intent
        data object RefreshAllDraws : Intent
        data class SelectVerificationLottery(val type: LotteryType) : Intent
        data class InputRedNumbers(val numbers: List<String>) : Intent
        data class InputBlueNumbers(val numbers: List<String>) : Intent
        data object VerifyNumbers : Intent
        data object ClearVerification : Intent
    }


    @Immutable
    data class State(
        val allDraws: Map<LotteryType, DrawResult> = emptyMap(),
        val isLoading: Boolean = false,
        val error: String? = null,

        val verificationLotteryType: LotteryType = LotteryType.SSQ,
        val inputRedNumbers: List<String> = emptyList(),
        val inputBlueNumbers: List<String> = emptyList(),
        val verificationResult: NumberVerificationEngine.VerificationResult? = null,
        val showVerificationPanel: Boolean = false
    ) : MviState


    sealed interface Effect : MviEffect {
        data class ShowToast(val message: String) : Effect
        data class NavigateToHistory(val lotteryType: LotteryType) : Effect
        data object ShowVerificationSuccess : Effect
    }
}

