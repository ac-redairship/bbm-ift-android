package com.redairship.ocbc.transfer

import com.redairship.ocbc.transfer.model.AccountItemModel
import com.redairship.ocbc.transfer.model.FxContract
import com.redairship.domain.DomainError
import java.io.Serializable

class LocalTransferData : Serializable {
    var fucntionCode: String = ""
    var selectFromAc = AccountItemModel()
    var selectToAc = AccountItemModel()
    var selectToAnotherCurrency = AccountItemModel()
    var purpose: String = ""
    var remarks: String = ""
    var referenceNumber: String = ""
    var addAspayee: Boolean = false
    var contractNo: String = ""
    var selectedDate: String = ""
    var getCheckValueDate: GetCheckValueDate? = null
    var fxContract: FxContract? = null
    var defaultMaxLimit: Float = 0.0f
    var defaultMinLimit: Float = 0.0f

    var ownAccountsList: List<AccountItemModel> = emptyList()
}

class GetCheckValueDate (
    var valueDate: String,
    var nextWorkingDay: String,
    var maxDay: Int,
): Serializable

class TransferError(error: String) : DomainError(error)