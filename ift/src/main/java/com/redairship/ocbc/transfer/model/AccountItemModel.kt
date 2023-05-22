package com.redairship.ocbc.transfer.model

import com.redairship.ocbc.bb.components.models.Amount
import com.redairship.ocbc.transfer.presentation.common.ExpandableRecyclerViewAdapter
import java.io.Serializable
import java.math.BigDecimal
import java.util.*
import java.util.Currency

data class AccountItemListModel(
    val accountList: List<AccountItemModel> = emptyList(),
) : Serializable


data class AccountItemModel(
    val availableAmounts: List<AccountItemModel> = emptyList(),
    val accountNumber: String,
    val accountType: AccountType,
    val currency: Currency,
    val currencyList: List<Currency>,
    val frequentlyUsedCurrencies: List<Currency>,
    val accountName: String,
    val amount: Amount,
    val displayName: String,
    val bankName: String,
    val userRole: UserRole,
    val is1MC: Boolean,
    val id: String = "",
    val accTokenId: String = "",
    val accountEntity: String = "",
    val cifNo: String = "",
    val cifName: String = "",
    val islamicFlag: String = "",
    val orgAcctId: String = ""
) : ExpandableRecyclerViewAdapter.ExpandableGroup<AccountItemModel>(), Serializable {

    override fun getExpandingItems(): List<AccountItemModel> {
        return availableAmounts
    }

    fun amountUtilized(): BigDecimal {
        return String.format("%.2f", amount).toBigDecimal()
    }

    companion object {
        fun init(accountNumber: String, currency: Currency): AccountItemModel {
            return AccountItemModel(
                accountNumber = accountNumber,
                accountType = AccountType.SingleCurrency,
                currency = currency,
                currencyList = listOf(),
                frequentlyUsedCurrencies = listOf(),
                accountName = "",
                amount = Amount(BigDecimal.ZERO, currency.symbol, Locale.getDefault(), currency),
                displayName = "",
                bankName = "",
                userRole = UserRole.Authorizer,
                is1MC = false,
                id = "",
                accTokenId = "",
                accountEntity = "",
                cifNo = "",
                cifName = "",
                islamicFlag = "",
                orgAcctId = ""
            )
        }
    }
}

enum class UserRole {
    Maker,
    Authorizer,
    // If user is 1MC, user is both maker and authorizer
    // If user is not 1MC and has both role, he will be the maker for the transaction instance
    Both
}

enum class AccountType {
    SingleCurrency,
    MultiCurrency
}