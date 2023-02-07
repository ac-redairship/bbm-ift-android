package com.redairship.ocbc.transfer

import kotlinx.coroutines.flow.MutableStateFlow

sealed class UiState<out T> {

    open val data: T? = null

    abstract fun <R> map(f: (T) -> R): UiState<R>

    inline fun doOnData(f: (T) -> Unit) {
        if (this is Success) {
            f(data)
        }
    }

    data class Success<out T>(override val data: T) : UiState<T>() {
        override fun <R> map(f: (T) -> R): UiState<R> = Success(f(data))
    }

    data class Error(val throwable: Throwable) : UiState<Nothing>() {
        constructor(message: String) : this(Throwable(message))

        override fun <R> map(f: (Nothing) -> R): UiState<R> = this
    }

    object Loading : UiState<Nothing>() {
        override fun <R> map(f: (Nothing) -> R): UiState<R> = this
    }

    object NotLoading : UiState<Nothing>() {
        override fun <R> map(f: (Nothing) -> R): UiState<R> = this
    }
}

fun <T> UiState<T>.successOr(fallback: T): T {
    return (this as? UiState.Success<T>)?.data ?: fallback
}

/**
 * Updates value of [MutableStateFlow] if [Result] is of type [Success]
 */
inline fun <reified T> UiState<T>.updateOnSuccess(stateFlow: MutableStateFlow<T>) {
    if (this is UiState.Success) {
        stateFlow.value = data
    }
}

