package com.vergil.lottery.presentation.screens.home

import androidx.lifecycle.viewModelScope
import com.vergil.lottery.core.mvi.MviViewModel
import com.vergil.lottery.core.util.Result
import com.vergil.lottery.data.cache.CachedLotteryRepository
import com.vergil.lottery.di.AppModule
import com.vergil.lottery.domain.analyzer.NumberVerificationEngine
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds


class HomeViewModel(
    private val repository: CachedLotteryRepository = AppModule.cachedRepository
) : MviViewModel<HomeContract.Intent, HomeContract.State, HomeContract.Effect>(
    initialState = HomeContract.State()
) {


    private val verificationEngine = NumberVerificationEngine()

    init {

        viewModelScope.launch {

            loadAllLatestDraws(useCache = true)


            kotlinx.coroutines.delay(200) 
            loadAllLatestDraws(useCache = false) 
        }
    }

    override fun handleIntent(intent: HomeContract.Intent) {
        when (intent) {
            is HomeContract.Intent.LoadAllLatestDraws -> {
                loadAllLatestDraws()
            }

            is HomeContract.Intent.RefreshAllDraws -> {
                loadAllLatestDraws(forceRefresh = true)
            }

            is HomeContract.Intent.SelectVerificationLottery -> {
                setState {
                    copy(
                        verificationLotteryType = intent.type,
                        showVerificationPanel = true,
                        inputRedNumbers = emptyList(),
                        inputBlueNumbers = emptyList(),
                        verificationResult = null
                    )
                }
            }

            is HomeContract.Intent.InputRedNumbers -> {
                setState { copy(inputRedNumbers = intent.numbers) }
            }

            is HomeContract.Intent.InputBlueNumbers -> {
                setState { copy(inputBlueNumbers = intent.numbers) }
            }

            is HomeContract.Intent.VerifyNumbers -> {
                verifyNumbers()
            }

            is HomeContract.Intent.ClearVerification -> {
                setState {
                    copy(
                        inputRedNumbers = emptyList(),
                        inputBlueNumbers = emptyList(),
                        verificationResult = null,
                        showVerificationPanel = false
                    )
                }
            }
        }
    }

    private fun loadAllLatestDraws(forceRefresh: Boolean = false, useCache: Boolean = true) {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }

            try {

                val deferredDraws = com.vergil.lottery.core.constants.LotteryType.entries.map { lotteryType ->
                    async {
                        try {
                            var successData: com.vergil.lottery.domain.model.DrawResult? = null



                            repository.getLatestDraw(lotteryType.code)
                                .timeout(10.seconds)
                                .catch { e ->

                                    if (e !is java.util.concurrent.CancellationException) {
                                        Timber.w(e, "Flow error for ${lotteryType.displayName}")
                                    }
                                }
                                .collect { result ->
                                    when (result) {
                                        is Result.Success -> {
                                            Timber.d("Loaded ${lotteryType.displayName}: ${result.data.issue}")
                                            successData = result.data
                                        }
                                        is Result.Error -> {

                                            if (successData == null) {
                                                Timber.w("Failed to load ${lotteryType.displayName}: ${result.exception.message}")
                                            }
                                        }
                                        is Result.Loading -> {

                                        }
                                    }
                                }


                            if (successData != null) {
                                lotteryType to successData
                            } else {
                                null
                            }
                        } catch (e: Exception) {

                            if (e !is java.util.concurrent.CancellationException && 
                                e !is kotlinx.coroutines.TimeoutCancellationException) {
                                Timber.e(e, "Error loading ${lotteryType.displayName}")
                            }
                            null
                        }
                    }
                }


                val results = deferredDraws.awaitAll().filterNotNull()
                val drawsMap = results.toMap()

                setState { copy(allDraws = drawsMap, isLoading = false) }
                Timber.d("Loaded ${drawsMap.size} lottery types")

            } catch (e: Exception) {
                Timber.e(e, "Failed to load all draws")
                setState { copy(isLoading = false, error = e.message ?: "加载失败") }
            }
        }
    }


    private fun verifyNumbers() {
        viewModelScope.launch {
            try {
                val currentState = state.value
                val lotteryType = currentState.verificationLotteryType
                val drawResult = currentState.allDraws[lotteryType]

                if (drawResult == null) {
                    setEffect(HomeContract.Effect.ShowToast("请先刷新获取最新开奖信息"))
                    return@launch
                }

                if (currentState.inputRedNumbers.isEmpty()) {
                    setEffect(HomeContract.Effect.ShowToast("请输入号码"))
                    return@launch
                }


                val result = verificationEngine.verify(
                    inputRed = currentState.inputRedNumbers,
                    inputBlue = currentState.inputBlueNumbers,
                    drawResult = drawResult,
                    lotteryType = lotteryType
                )

                setState { copy(verificationResult = result) }

                if (result.isWin) {
                    setEffect(HomeContract.Effect.ShowVerificationSuccess)
                }

                Timber.d("号码验证完成: ${result.message}")

            } catch (e: Exception) {
                Timber.e(e, "号码验证失败")
                setEffect(HomeContract.Effect.ShowToast("验证失败: ${e.message}"))
            }
        }
    }
}

