package com.vergil.lottery.presentation.screens.analysis

import androidx.lifecycle.viewModelScope
import com.vergil.lottery.core.mvi.MviViewModel
import com.vergil.lottery.core.util.Result
import com.vergil.lottery.data.cache.CachedLotteryRepository
import com.vergil.lottery.di.AppModule
import com.vergil.lottery.domain.analyzer.AnalysisEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber


class AnalysisViewModel(
    private val repository: CachedLotteryRepository = AppModule.cachedRepository
) : MviViewModel<AnalysisContract.Intent, AnalysisContract.State, AnalysisContract.Effect>(
    initialState = AnalysisContract.State()
) {


    private var currentLoadJob: Job? = null


    private val analysisEngine = AnalysisEngine()

    init {

        viewModelScope.launch {
            kotlinx.coroutines.delay(100) 
            loadHistory(state.value.selectedType.code)
        }
    }

    override fun handleIntent(intent: AnalysisContract.Intent) {
        when (intent) {
            is AnalysisContract.Intent.SelectLotteryType -> {

                setState {
                    copy(
                        selectedType = intent.type,
                        isLoading = true,
                        error = null,
                        historyList = emptyList(),  
                        currentPage = 0,            
                        hasMore = true              
                    )
                }

                currentLoadJob?.cancel()

                loadHistory(intent.type.code, page = 0)
            }

            is AnalysisContract.Intent.SelectDimension -> {
                setState { copy(selectedDimension = intent.dimension) }

                if (intent.dimension != AnalysisContract.AnalysisDimension.HISTORY) {
                    analyzeData()
                }
            }

            is AnalysisContract.Intent.SelectBallType -> {
                setState { copy(selectedBallType = intent.ballType) }

                if (state.value.selectedDimension != AnalysisContract.AnalysisDimension.HISTORY) {
                    analyzeData()
                }
            }

            is AnalysisContract.Intent.LoadHistory -> {
                loadHistory(state.value.selectedType.code, page = 0)
            }

            is AnalysisContract.Intent.RefreshHistory -> {
                currentLoadJob?.cancel()
                setState {
                    copy(
                        historyList = emptyList(),
                        currentPage = 0,
                        hasMore = true
                    )
                }
                loadHistory(state.value.selectedType.code, page = 0, forceRefresh = true)
            }

            is AnalysisContract.Intent.LoadMoreHistory -> {

                val currentState = state.value
                if (!currentState.isLoadingMore && currentState.hasMore && !currentState.isLoading) {
                    loadHistory(currentState.selectedType.code, page = currentState.currentPage + 1)
                }
            }

            is AnalysisContract.Intent.AnalyzeData -> {
                analyzeData()
            }
        }
    }


    private fun loadHistory(lotteryCode: String, page: Int = 0, forceRefresh: Boolean = false) {
        val pageSize = state.value.pageSize
        val isLoadingMore = page > 0  


        if (isLoadingMore) {
            setState { copy(isLoadingMore = true, error = null) }
        } else {
            setState { copy(isLoading = true, error = null) }
        }



        currentLoadJob = repository.getHistory(lotteryCode, size = 100, forceRefresh = forceRefresh)
            .onEach { result ->
                when (result) {
                    is Result.Loading -> {

                    }

                    is Result.Success -> {
                        val allData = result.data
                        val totalSize = allData.size


                        val startIndex = page * pageSize
                        val endIndex = minOf(startIndex + pageSize, totalSize)

                        if (startIndex >= totalSize) {

                            setState {
                                copy(
                                    isLoading = false,
                                    isLoadingMore = false,
                                    hasMore = false
                                )
                            }
                            Timber.d("No more data available")
                        } else {

                            val pageData = allData.subList(startIndex, endIndex)


                            val hasMore = endIndex < totalSize

                            setState {
                                if (isLoadingMore) {

                                    copy(
                                        historyList = historyList + pageData,
                                        currentPage = page,
                                        isLoadingMore = false,
                                        hasMore = hasMore,
                                        error = null
                                    )
                                } else {

                                    copy(
                                        historyList = pageData,
                                        currentPage = page,
                                        isLoading = false,
                                        hasMore = hasMore,
                                        error = null
                                    )
                                }
                            }

                            Timber.d("Loaded page $page: ${pageData.size} records (total: ${state.value.historyList.size}/${totalSize})")


                            if (state.value.selectedDimension != AnalysisContract.AnalysisDimension.HISTORY) {
                                analyzeData()
                            }
                        }
                    }

                    is Result.Error -> {
                        setState {
                            copy(
                                isLoading = false,
                                isLoadingMore = false,
                                error = result.exception.message ?: "加载失败"
                            )
                        }
                        setEffect(AnalysisContract.Effect.ShowToast("加载失败: ${result.exception.message}"))
                        Timber.e(result.exception, "Failed to load history")
                    }
                }
            }
            .launchIn(viewModelScope)
    }


    private fun analyzeData() {
        viewModelScope.launch {
            try {
                val currentState = state.value
                val history = currentState.historyList

                if (history.isEmpty()) {
                    Timber.w("历史数据为空，无法分析")
                    return@launch
                }

                val isRed = currentState.selectedBallType == AnalysisContract.BallType.RED

                when (currentState.selectedDimension) {
                    AnalysisContract.AnalysisDimension.HISTORY -> {

                    }

                    AnalysisContract.AnalysisDimension.OMISSION -> {
                        val data = analysisEngine.analyzeOmission(history, currentState.selectedType, isRed)
                        setState { copy(omissionData = data) }
                        Timber.d("遗漏分析完成: ${data.size} 个号码")
                    }

                    AnalysisContract.AnalysisDimension.FREQUENCY -> {
                        val data = analysisEngine.analyzeFrequency(history, currentState.selectedType, isRed)
                        setState { copy(frequencyData = data) }
                        Timber.d("频率分析完成: ${data.size} 个号码")
                    }

                    AnalysisContract.AnalysisDimension.HOT_COLD -> {
                        val data = analysisEngine.analyzeHotCold(history, currentState.selectedType, isRed)
                        setState { copy(hotColdData = data) }
                        Timber.d("冷热号分析完成: ${data.size} 个号码")
                    }

                    AnalysisContract.AnalysisDimension.CONSECUTIVE -> {
                        val data = analysisEngine.analyzeConsecutive(history, isRed)
                        setState { copy(consecutiveData = data) }
                        Timber.d("连号分析完成: ${data.size} 组连号")
                    }

                    AnalysisContract.AnalysisDimension.SAME_TAIL -> {
                        val data = analysisEngine.analyzeSameTail(history, currentState.selectedType, isRed)
                        setState { copy(sameTailData = data) }
                        Timber.d("同尾号分析完成: ${data.size} 种尾数")
                    }

                    AnalysisContract.AnalysisDimension.SUM_VALUE -> {
                        val data = analysisEngine.analyzeSumValue(history, isRed)
                        setState { copy(sumValueData = data) }
                        Timber.d("和值分析完成: ${data.size} 个和值")
                    }

                    AnalysisContract.AnalysisDimension.SPAN -> {
                        val data = analysisEngine.analyzeSpan(history, isRed)
                        setState { copy(spanData = data) }
                        Timber.d("跨度分析完成: ${data.size} 个跨度")
                    }

                    AnalysisContract.AnalysisDimension.AC_VALUE -> {
                        val data = analysisEngine.analyzeACValue(history, isRed)
                        setState { copy(acValueData = data) }
                        Timber.d("AC值分析完成: ${data.size} 个AC值")
                    }

                    AnalysisContract.AnalysisDimension.ODD_EVEN -> {
                        val data = analysisEngine.analyzeOddEvenRatio(history, isRed)
                        setState { copy(oddEvenData = data) }
                        Timber.d("奇偶比分析完成: ${data.size} 种比例")
                    }

                    AnalysisContract.AnalysisDimension.SIZE_RATIO -> {
                        val data = analysisEngine.analyzeSizeRatio(history, currentState.selectedType, isRed)
                        setState { copy(sizeRatioData = data) }
                        Timber.d("大小比分析完成: ${data.size} 种比例")
                    }

                    AnalysisContract.AnalysisDimension.PRIME_COMPOSITE -> {
                        val data = analysisEngine.analyzePrimeCompositeRatio(history, isRed)
                        setState { copy(primeCompositeData = data) }
                        Timber.d("质合比分析完成: ${data.size} 种比例")
                    }

                    AnalysisContract.AnalysisDimension.ZONE -> {
                        val data = analysisEngine.analyzeZone(history, currentState.selectedType, isRed, zoneCount = 3)
                        setState { copy(zoneData = data) }
                        Timber.d("区间分析完成: ${data.size} 个区间")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "数据分析失败")
                setEffect(AnalysisContract.Effect.ShowToast("分析失败: ${e.message}"))
            }
        }
    }
}

