package com.vergil.lottery.core.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow


object ToastManager {


    data class Message(
        val text: String,
        val duration: SnackbarDuration = SnackbarDuration.Short,
        val actionLabel: String? = null,
        val onActionPerformed: (() -> Unit)? = null
    )

    private val _messages = Channel<Message>(Channel.BUFFERED)
    val messages: Flow<Message> = _messages.receiveAsFlow()


    fun showToast(message: String) {
        _messages.trySend(
            Message(
                text = message,
                duration = SnackbarDuration.Short
            )
        )
    }


    fun showSnackbar(
        message: String,
        actionLabel: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        onActionPerformed: () -> Unit
    ) {
        _messages.trySend(
            Message(
                text = message,
                duration = duration,
                actionLabel = actionLabel,
                onActionPerformed = onActionPerformed
            )
        )
    }


    fun showLongToast(message: String) {
        _messages.trySend(
            Message(
                text = message,
                duration = SnackbarDuration.Long
            )
        )
    }


    fun showError(message: String) {
        _messages.trySend(
            Message(
                text = "❌ $message",
                duration = SnackbarDuration.Long
            )
        )
    }


    fun showSuccess(message: String) {
        _messages.trySend(
            Message(
                text = "✅ $message",
                duration = SnackbarDuration.Short
            )
        )
    }


    fun showWarning(message: String) {
        _messages.trySend(
            Message(
                text = "⚠️ $message",
                duration = SnackbarDuration.Long
            )
        )
    }
}


suspend fun SnackbarHostState.handleMessage(message: ToastManager.Message): SnackbarResult {
    return showSnackbar(
        message = message.text,
        actionLabel = message.actionLabel,
        duration = message.duration
    ).also {
        if (it == SnackbarResult.ActionPerformed) {
            message.onActionPerformed?.invoke()
        }
    }
}

