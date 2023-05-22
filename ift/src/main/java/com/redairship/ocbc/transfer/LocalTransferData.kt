package com.redairship.ocbc.transfer

import com.redairship.domain.DomainError
import com.redairship.ocbc.transfer.model.AccountItemModel
import com.redairship.ocbc.transfer.model.FxContract
import java.io.Serializable
import java.util.Currency

data class LocalTransferData(
    val functionCode: String = "",
    val selectFromAc: AccountItemModel = AccountItemModel.init(accountNumber = "", currency = Currency.getInstance("SGD")),
    val selectToAc: AccountItemModel = AccountItemModel.init(accountNumber = "", currency = Currency.getInstance("SGD")),
    val selectToAnotherCurrency: AccountItemModel = AccountItemModel.init(accountNumber = "", currency = Currency.getInstance("SGD")),
    val purpose: String = "",
    val remarks: String = "",
    val referenceNumber: String = "",
    val addAsPayee: Boolean = true,
    val contractNo: String = "",
    val selectedDate: String = "",
    val getCheckValueDate: GetCheckValueDate? = null,
    val fxContract: FxContract? = null,
    val defaultMaxLimit: Float = 20000000000.0f,
    val defaultMinLimit: Float = 2000.0f,
    val ownAccountsList: List<AccountItemModel> = emptyList()
): Serializable

data class GetCheckValueDate (
    val valueDate: String,
    val nextWorkingDay: String,
    val maxDay: Int
): Serializable

data class TransferError(val error: String) : DomainError(error)