package com.redairship.ocbc.transfer.presentation.common

import com.redairship.ocbc.transfer.model.AccountItemModel
import com.redairship.ocbc.transfer.presentation.base.TransferStatus

interface AccountListBottomInterface {
    fun selectAccountItem(type: TransferStatus, item: AccountItemModel)
    fun showErrorMessage(error: String?)
}