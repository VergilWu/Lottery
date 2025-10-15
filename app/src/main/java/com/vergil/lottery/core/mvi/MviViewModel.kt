package com.vergil.lottery.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


abstract class MviViewModel<TIntent : MviIntent, TState : MviState, TEffect : MviEffect>(
    initialState: TState
) : ViewModel() {


    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<TState> = _state.asStateFlow()


    private val _effect = Channel<TEffect>(Channel.BUFFERED)
    val effect: Flow<TEffect> = _effect.receiveAsFlow()


    protected val currentState: TState
        get() = _state.value


    fun sendIntent(intent: TIntent) {
        handleIntent(intent)
    }


    protected abstract fun handleIntent(intent: TIntent)


    protected fun setState(reducer: TState.() -> TState) {
        _state.value = currentState.reducer()
    }


    protected fun setEffect(effect: TEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}

