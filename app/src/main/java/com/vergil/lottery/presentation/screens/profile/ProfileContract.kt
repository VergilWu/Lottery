package com.vergil.lottery.presentation.screens.profile

import androidx.compose.runtime.Immutable
import com.vergil.lottery.core.constants.LotteryType
import com.vergil.lottery.core.mvi.MviEffect
import com.vergil.lottery.core.mvi.MviIntent
import com.vergil.lottery.core.mvi.MviState


object ProfileContract {


    sealed interface Intent : MviIntent {
        data object NavigateToThemeSetting : Intent
        data object NavigateToDefaultLotterySetting : Intent
        data object NavigateToAbout : Intent
        data object NavigateToSettings : Intent
        data class ChangeDefaultLotteryType(val type: LotteryType) : Intent
        data object ToggleFavoriteMode : Intent  
        data object ClearCache : Intent  
    }


    @Immutable
    data class State(
        val userName: String = "彩票达人",  
        val userAvatar: String? = null,  
        val defaultLotteryType: LotteryType = LotteryType.SSQ,  
        val favoriteCount: Int = 0,  
        val viewedDrawsCount: Int = 0,  
        val cacheSize: String = "0 MB",  
        val isLoading: Boolean = false,
        val error: String? = null
    ) : MviState


    sealed interface Effect : MviEffect {
        data class ShowToast(val message: String) : Effect
        data object NavigateToThemeSetting : Effect
        data object NavigateToDefaultLotterySetting : Effect
        data object NavigateToAbout : Effect
        data object NavigateToSettings : Effect
    }
}
