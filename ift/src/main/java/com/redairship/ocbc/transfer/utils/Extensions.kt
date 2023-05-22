package com.redairship.ocbc.transfer.utils

import com.redairship.domain.DomainError
import com.redairship.domain.DomainResponse
import com.redairship.ocbc.transfer.UiState
import com.redairship.ocbc.transfer.domain.LocalTransferError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*

class DomainException(val error: DomainError = LocalTransferError.ServerResponseError("")) :
    Exception(error.message)

fun <T, E : DomainError> flowFromAppDomain(block: suspend () -> DomainResponse<T, E>): Flow<UiState<T>> {
    return flow {
        when (val result = block.invoke()) {
            is DomainResponse.Success -> {
                emit(UiState.Success(result.value))
            }
            is DomainResponse.Error -> {
                throw DomainException(error = result.error)
            }
        }
    }
}

fun <T, E : DomainError> flowFromAppDomain2(block: suspend () -> DomainResponse<T, E>): Flow<T> {
    return flow {
        when (val result = block.invoke()) {
            is DomainResponse.Success -> {
                emit(result.value)
            }
            is DomainResponse.Error -> {
                throw DomainException(error = result.error)
            }
        }
    }
}

fun String.capitalise(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

fun String.titlecase(): String {
    return split(" ").map { it.lowercase().capitalise() }.joinToString(" ")
}