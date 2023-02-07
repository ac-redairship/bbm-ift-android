package com.redairship.ocbc.transfer.domain

import com.redairship.domain.DomainError

open class LocalTransferError(errorMessage: String): DomainError(errorMessage)
