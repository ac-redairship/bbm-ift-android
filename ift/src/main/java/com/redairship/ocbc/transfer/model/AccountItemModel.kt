package com.redairship.ocbc.transfer.model

import com.redairship.ocbc.transfer.presentation.common.ExpandableRecyclerViewAdapter
import java.io.Serializable
import java.math.BigDecimal

class AccountItemListModel(
    var accountList: List<AccountItemModel> = emptyList(),
) : Serializable

class AccountItemModel : ExpandableRecyclerViewAdapter.ExpandableGroup<AccountItemModel>(),
    Serializable {
    var accountItems: List<AccountItemModel> = emptyList()
    var accountNo: String = ""
    var accountType: String = ""
    var currency: String = ""
    var name: String = ""
    var cifNo: String = ""
    var cifName: String = ""
    var accountEntity: String = ""
    var accTokenId: String = ""
    var id: String = ""
    var islamicFlag: String = ""
    var orgAcctId: String = ""
    var amount: Double = 0.0
    var description: String = ""
    var payeename: String = ""

    override fun getExpandingItems(): List<AccountItemModel> {
        return accountItems
    }


    fun amountUtilized(): BigDecimal {
        return String.format("%.2f", amount).toBigDecimal()
    }
}
