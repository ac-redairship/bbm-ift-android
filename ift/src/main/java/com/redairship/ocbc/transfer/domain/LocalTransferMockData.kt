package com.redairship.ocbc.transfer.domain

import com.redairship.ocbc.transfer.model.AccountItemListModel
import com.redairship.ocbc.transfer.model.AccountItemModel

object LocalTransferMockData {

    val oneSingleCurrencyAccount = AccountItemListModel(
        accountList = listOf(
            getSingleCurrencyAccount()
        )
    )
    fun getSingleCurrencyAccount(number: String? = null, name: String? = null): AccountItemModel {
        return AccountItemModel().apply {
            accountItems = listOf(AccountItemModel().apply {
                accountNo = number.orEmpty()
                this.name = name.orEmpty()
                amount = 100333120.0
                currency = "SGD"
                description = "Sandy Chua Wei Ling Very Long Name Which Will Truncate And Wrap To Next Line"
            })

            this.accountNo = number.orEmpty()
            this.name = name.orEmpty()
        }
    }
}

