package com.vergil.lottery.presentation.screens.prediction

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vergil.lottery.core.constants.LotteryType
import com.vergil.lottery.core.util.Result
import com.vergil.lottery.data.cache.CachedLotteryRepository
import com.vergil.lottery.domain.analyzer.PredictionEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber


class PredictionViewModel(
    private val repository: CachedLotteryRepository,
    context: Context? = null
) : ViewModel() {

    private val predictionEngine = PredictionEngine(context)

    private val _state = MutableStateFlow(PredictionContract.State())
    val state: StateFlow<PredictionContract.State> = _state.asStateFlow()

    private val _effect = Channel<PredictionContract.Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var loadJob: Job? = null

    init {

        viewModelScope.launch {
            kotlinx.coroutines.delay(100) 
            loadHistoryData(LotteryType.SSQ)
        }
    }

    fun handleIntent(intent: PredictionContract.Intent) {
        when (intent) {
            is PredictionContract.Intent.SelectLotteryType -> {
                setState { copy(selectedType = intent.type, predictions = emptyList(), error = null) }
                loadHistoryData(intent.type)
            }

            is PredictionContract.Intent.GeneratePrediction -> {
                generatePredictions()
            }

            is PredictionContract.Intent.RefreshData -> {
                loadHistoryData(state.value.selectedType, forceRefresh = true)
            }

            is PredictionContract.Intent.SelectMode -> {
                if (intent.mode == PredictionContract.PredictionMode.CUSTOM) {

                    setState { copy(selectedMode = intent.mode) }
                } else {

                    setState {
                        copy(
                            selectedMode = intent.mode,
                            selectedAlgorithms = intent.mode.algorithms
                        )
                    }
                }
            }

            is PredictionContract.Intent.SelectAlgorithms -> {
                setState {
                    copy(
                        selectedMode = PredictionContract.PredictionMode.CUSTOM,
                        selectedAlgorithms = intent.algorithms
                    )
                }
            }

            is PredictionContract.Intent.ToggleAlgorithm -> {
                val currentAlgorithms = state.value.selectedAlgorithms
                val newAlgorithms = if (intent.algorithm in currentAlgorithms) {
                    currentAlgorithms - intent.algorithm
                } else {
                    currentAlgorithms + intent.algorithm
                }
                setState {
                    copy(
                        selectedMode = PredictionContract.PredictionMode.CUSTOM,
                        selectedAlgorithms = newAlgorithms
                    )
                }
            }

            is PredictionContract.Intent.SetPredictionCount -> {
                val validCount = intent.count.coerceIn(1, 10)
                setState { copy(predictionCount = validCount) }
            }

            is PredictionContract.Intent.SelectAllAlgorithms -> {
                setState {
                    copy(
                        selectedMode = PredictionContract.PredictionMode.CUSTOM,
                        selectedAlgorithms = PredictionContract.PredictionAlgorithm.entries.toSet()
                    )
                }
            }

            is PredictionContract.Intent.DeselectAllAlgorithms -> {
                setState {
                    copy(
                        selectedMode = PredictionContract.PredictionMode.CUSTOM,
                        selectedAlgorithms = emptySet()
                    )
                }
            }

            is PredictionContract.Intent.ShowAlgorithmExplanation -> {
                setEffect(PredictionContract.Effect.ShowAlgorithmExplanation)
            }
        }
    }

    private fun loadHistoryData(lotteryType: LotteryType, forceRefresh: Boolean = false) {
        loadJob?.cancel()

        setState { copy(isLoading = true, error = null) }

        loadJob = repository.getHistory(lotteryType.code, size = 100, forceRefresh = forceRefresh)
            .onEach { result ->
                when (result) {
                    is Result.Loading -> {

                    }
                    is Result.Success -> {
                        setState { copy(isLoading = false, error = null) }
                        Timber.d("Loaded ${result.data.size} history records for prediction")
                    }
                    is Result.Error -> {
                        setState {
                            copy(
                                isLoading = false,
                                error = result.exception.message ?: "加载失败"
                            )
                        }
                        setEffect(PredictionContract.Effect.ShowToast("加载数据失败"))
                        Timber.e(result.exception, "Failed to load history for prediction")
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun generatePredictions() {
        viewModelScope.launch {
            try {
                setState { 
                    copy(
                        isGenerating = true, 
                        currentAlgorithm = "正在加载数据...",
                        algorithmProgress = 0f,
                        error = null
                    ) 
                }


                val lotteryCode = state.value.selectedType.code
                var historyData = emptyList<com.vergil.lottery.domain.model.DrawResult>()
                var loadError: Throwable? = null

                repository.getHistory(lotteryCode, size = 100, forceRefresh = false)
                    .collect { result ->
                        when (result) {
                            is Result.Success -> {
                                historyData = result.data
                            }
                            is Result.Error -> {
                                loadError = result.exception
                            }
                            is Result.Loading -> {

                            }
                        }
                    }


                if (loadError != null) {
                    throw loadError!!
                }

                if (historyData.size < 20) {
                    setState {
                        copy(
                            isGenerating = false,
                            currentAlgorithm = null,
                            algorithmProgress = 0f,
                            error = "历史数据不足（至少需要20期）"
                        )
                    }
                    setEffect(PredictionContract.Effect.ShowToast("历史数据不足，请先加载更多数据"))
                    return@launch
                }


                if (state.value.selectedAlgorithms.isEmpty()) {
                    setState {
                        copy(
                            isGenerating = false,
                            currentAlgorithm = null,
                            algorithmProgress = 0f,
                            error = "请至少选择一种预测算法"
                        )
                    }
                    setEffect(PredictionContract.Effect.ShowToast("请至少选择一种预测算法"))
                    return@launch
                }


                val totalAlgorithms = state.value.selectedAlgorithms.size
                var completedAlgorithms = 0


                state.value.selectedAlgorithms.forEachIndexed { index, algorithm ->
                    setState {
                        copy(
                            currentAlgorithm = algorithm.displayName,
                            algorithmProgress = ((index + 1).toFloat() / totalAlgorithms).coerceIn(0f, 1f)
                        )
                    }

                    kotlinx.coroutines.delay(50)
                }


                val predictions = predictionEngine.generatePredictions(
                    history = historyData,
                    lotteryType = state.value.selectedType,
                    algorithms = state.value.selectedAlgorithms,
                    count = state.value.predictionCount
                )


                setState {
                    copy(
                        currentAlgorithm = "生成完成",
                        algorithmProgress = 1f
                    )
                }
                kotlinx.coroutines.delay(300)  

                setState {
                    copy(
                        isGenerating = false,
                        currentAlgorithm = null,
                        algorithmProgress = 0f,
                        predictions = predictions,
                        error = null
                    )
                }

                setEffect(PredictionContract.Effect.ShowGenerateSuccess)
                setEffect(PredictionContract.Effect.ScrollToResults)  
                Timber.d("Generated ${predictions.size} predictions")

            } catch (e: Exception) {
                setState {
                    copy(
                        isGenerating = false,
                        currentAlgorithm = null,
                        algorithmProgress = 0f,
                        error = e.message ?: "生成失败"
                    )
                }
                setEffect(PredictionContract.Effect.ShowToast("生成预测失败: ${e.message}"))
                Timber.e(e, "Failed to generate predictions")
            }
        }
    }

    private fun setState(reducer: PredictionContract.State.() -> PredictionContract.State) {
        _state.value = state.value.reducer()
    }

    private fun setEffect(effect: PredictionContract.Effect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}

