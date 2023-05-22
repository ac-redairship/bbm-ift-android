package com.redairship.ocbc.transfer.presentation.common

import com.redairship.ocbc.transfer.model.TransferToTypeResponse

interface TransferToBottomInterface {
    fun selectTransferTo(type: TransferToTypeResponse)
}