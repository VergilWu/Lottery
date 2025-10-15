package com.vergil.lottery.presentation.screens.profile

import androidx.lifecycle.viewModelScope
import com.vergil.lottery.core.mvi.MviViewModel
import com.vergil.lottery.data.cache.CacheManager
import com.vergil.lottery.data.local.preferences.PreferencesManager
import com.vergil.lottery.di.AppModule
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber


class ProfileViewModel(
    private val preferencesManager: PreferencesManager = AppModule.preferencesManager,
    private val cacheManager: CacheManager = AppModule.cacheManager
) : MviViewModel<ProfileContract.Intent, ProfileContract.State, ProfileContract.Effect>(
    initialState = ProfileContract.State()
) {

    init {

        loadUserInfo()

        loadCacheInfo()
    }

    override fun handleIntent(intent: ProfileContract.Intent) {
        when (intent) {
            is ProfileContract.Intent.NavigateToThemeSetting -> {
                setEffect(ProfileContract.Effect.NavigateToThemeSetting)
            }

            is ProfileContract.Intent.NavigateToDefaultLotterySetting -> {
                setEffect(ProfileContract.Effect.NavigateToDefaultLotterySetting)
            }

            is ProfileContract.Intent.NavigateToAbout -> {
                setEffect(ProfileContract.Effect.NavigateToAbout)
            }

            is ProfileContract.Intent.NavigateToSettings -> {
                setEffect(ProfileContract.Effect.NavigateToSettings)
            }

            is ProfileContract.Intent.ChangeDefaultLotteryType -> {
                changeDefaultLotteryType(intent.type)
            }

            is ProfileContract.Intent.ToggleFavoriteMode -> {
                toggleFavoriteMode()
            }

            is ProfileContract.Intent.ClearCache -> {
                clearCache()
            }

        }
    }

    private fun loadUserInfo() {

        preferencesManager.userName
            .onEach { userName ->
                setState { copy(userName = userName) }
            }
            .launchIn(viewModelScope)


        preferencesManager.defaultLotteryType
            .onEach { type ->
                setState { copy(defaultLotteryType = type) }
            }
            .launchIn(viewModelScope)




        setState {
            copy(
                favoriteCount = 0,
                viewedDrawsCount = 0,
                cacheSize = "0 MB"
            )
        }
    }

    private fun changeDefaultLotteryType(type: com.vergil.lottery.core.constants.LotteryType) {
        viewModelScope.launch {
            try {
                preferencesManager.setDefaultLotteryType(type)
                setEffect(ProfileContract.Effect.ShowToast("已设置默认彩种为${type.displayName}"))
                Timber.d("Default lottery type changed to: ${type.displayName}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to change default lottery type")
                setEffect(ProfileContract.Effect.ShowToast("设置失败"))
            }
        }
    }

    private fun toggleFavoriteMode() {
        viewModelScope.launch {
            try {
                preferencesManager.toggleFavoriteMode()
                setEffect(ProfileContract.Effect.ShowToast("收藏模式已切换"))
                Timber.d("Favorite mode toggled")
            } catch (e: Exception) {
                Timber.e(e, "Failed to toggle favorite mode")
                setEffect(ProfileContract.Effect.ShowToast("切换失败"))
            }
        }
    }

    private fun loadCacheInfo() {
        viewModelScope.launch {
            try {
                val cacheStats = cacheManager.getCacheStats()
                val totalSize = cacheStats.values.sumOf { it.dataSize }
                val sizeInMB = totalSize / (1024.0 * 1024.0)
                val formattedSize = if (sizeInMB < 1.0) {
                    "${(totalSize / 1024.0).toInt()} KB"
                } else {
                    "${String.format("%.1f", sizeInMB)} MB"
                }

                setState { copy(cacheSize = formattedSize) }
                Timber.d("Cache size loaded: $formattedSize")
            } catch (e: Exception) {
                Timber.e(e, "Failed to load cache info")
                setState { copy(cacheSize = "未知") }
            }
        }
    }



    private fun clearCache() {
        viewModelScope.launch {
            try {
                setState { copy(isLoading = true) }


                cacheManager.clearAllCache()


                loadCacheInfo()

                setState { copy(isLoading = false) }

                setEffect(ProfileContract.Effect.ShowToast("缓存已清理"))
                Timber.d("Cache cleared successfully")
            } catch (e: Exception) {
                setState { 
                    copy(
                        isLoading = false,
                        error = "清理失败: ${e.message}"
                    ) 
                }
                Timber.e(e, "Failed to clear cache")
                setEffect(ProfileContract.Effect.ShowToast("清理失败: ${e.message}"))
            }
        }
    }

}
