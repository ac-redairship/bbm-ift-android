package com.redairship.ocbc.transfer.domain

import com.redairship.domain.DomainError

sealed class LocalTransferError(errorMessage: String): DomainError(errorMessage) {
    class ServerResponseError(errorMessage: String): LocalTransferError(errorMessage)
    object SameDayFee: LocalTransferError("Add accept same day response from user")
}
