package com.redairship.ocbc.transfer.domain

import com.redairship.ocbc.bb.components.models.Amount
import com.redairship.ocbc.transfer.model.*
import java.math.BigDecimal
import java.util.*
import java.util.Currency

object LocalTransferMockData {

    val sgd = Currency.getInstance(Locale("en", "SG"))
    val usd = Currency.getInstance(Locale("en", "US"))
    val hkd = Currency.getInstance(Locale("en", "HK"))
    val eur = Currency.getInstance("EUR")
    val thb = Currency.getInstance(Locale("th", "TH"))
    val jpy = Currency.getInstance(Locale("ja", "JP"))
    val nzd = Currency.getInstance(Locale("en", "NZ"))


    val oneSingleCurrencyAccount = AccountItemListModel(
        accountList = listOf(
            getSingleCurrencyAccount()
        )
    )

    val oneMultiCurrencyAccount = AccountItemListModel(
        accountList = listOf(
            getMultiCurrencyAccount()
        )
    )

    val normalAccounts = AccountItemListModel(
        accountList = listOf(
            getMultiCurrencyAccount(),
            getSingleCurrencyAccount(number = "647561400224", name = "Esolution Alpha"),
            getSingleCurrencyAccount()
        )
    )

    val manyAccounts = AccountItemListModel(
        accountList = listOf(
            getMultiCurrencyAccount(),
            getMultiCurrencyAccount(),
            getMultiCurrencyAccount(),
            getSingleCurrencyAccount(name = "Esolution Alpha"),
            getSingleCurrencyAccount(),
            getSingleCurrencyAccount(),
            getSingleCurrencyAccount(),
            getSingleCurrencyAccount(),
            getSingleCurrencyAccount(),
            getSingleCurrencyAccount(),
            getSingleCurrencyAccount(),
            getSingleCurrencyAccount(),
            getSingleCurrencyAccount(),
            getSingleCurrencyAccount(),
            getSingleCurrencyAccount(),
            getSingleCurrencyAccount(),
            getSingleCurrencyAccount()
        )
    )


    fun getSingleCurrencyAccount(number: String? = null, name: String? = null): AccountItemModel {
        return AccountItemModel(
            id = number ?: "1",
            accountNumber = number ?: "647561400225",
            accountType = AccountType.SingleCurrency,
            availableAmounts = listOf(),
            amount = Amount(
                value = BigDecimal.valueOf(20000),
                currency = sgd,
                locale = Locale.getDefault(),
                symbol = sgd.symbol
            ),
            accountName = name
                ?: "Esolution Beta Pte Ltd",
            displayName = "Sandy Chua Wei Ling",
            currency = sgd,
            currencyList = listOf(),
            frequentlyUsedCurrencies = listOf(),
            bankName = "OCBC Bank Pte Ltd",
            userRole = UserRole.Maker,
            is1MC = false,
        )
    }

    fun getMultiCurrencyAccount(name: String? = null): AccountItemModel {

        val mockCurrencies = listOf(sgd, usd, hkd, jpy, thb, nzd)
        val mockFrequentlyUsedCurrencies = listOf(usd, sgd, hkd, eur, jpy)

        val mainAccount = AccountItemModel(
            id = UUID.randomUUID().toString(),
            accountNumber = "647561400112",
            accountType = AccountType.MultiCurrency,
            availableAmounts = listOf(),
            amount = Amount(
                currency = sgd,
                value = BigDecimal.valueOf(133133312),
                symbol = sgd.symbol,
                locale = Locale("en", "SG")
            ),
            currency = sgd,
            accountName = name ?: "Multi-currency",
            displayName = "Sandy Chua Wei Ling",
            currencyList = mockCurrencies,
            frequentlyUsedCurrencies = mockFrequentlyUsedCurrencies,
            bankName = "OCBC Bank",
            userRole = UserRole.Both,
            is1MC = true
        )

        return mainAccount.copy(
            availableAmounts = listOf(
                mainAccount.copy(
                    id = mainAccount.id + "1",
                    amount = Amount(
                        value = BigDecimal.valueOf(100333120),
                        currency = sgd,
                        symbol = sgd.symbol,
                        locale = Locale("en", "SG")
                    )
                ),
                mainAccount.copy(
                    id = mainAccount.id + "2",
                    amount = Amount(
                        value = BigDecimal.valueOf(53055270),
                        currency = usd,
                        symbol = usd.symbol,
                        locale = Locale("en", "US")
                    ),
                ),
                mainAccount.copy(
                    id = mainAccount.id + "3",
                    amount = Amount(
                        value = BigDecimal.valueOf(53055270),
                        currency = hkd,
                        symbol = hkd.symbol,
                        locale = Locale("en", "HK")
                    ),
                ),
                mainAccount.copy(
                    id = mainAccount.id + "4",
                    amount = Amount(
                        value = BigDecimal.valueOf(53055270),
                        currency = jpy,
                        symbol = jpy.symbol,
                        locale = Locale("ja", "JP")
                    ),
                ),
                mainAccount.copy(
                    id = mainAccount.id + "5",
                    amount = Amount(
                        value = BigDecimal.valueOf(53055270),
                        currency = thb,
                        symbol = thb.symbol,
                        locale = Locale("th", "TH")
                    ),
                ),
                mainAccount.copy(
                    id = mainAccount.id + "6",
                    amount = Amount(
                        value = BigDecimal.valueOf(53055270),
                        currency = nzd,
                        symbol = nzd.symbol,
                        locale = Locale("en", "NZ")
                    ),
                )
            )
        )
    }

    fun getMockUserFXContracts(): List<FxContract> {
        return listOf(
            FxContract(
                indicativeAmount = 1897890f,
                fxRate = 1.3841f,
                contractBuyCurrency = "USD",
                fxDate = Date().toString(),
                quoteId = "0011223341"
            ),
            FxContract(
                indicativeAmount = 158970f,
                fxRate = 1.1822f,
                contractBuyCurrency = "HKD",
                fxDate = Date().toString(),
                quoteId = "0011223342"
            ),
            FxContract(
                indicativeAmount = 1800f,
                fxRate = 1.6677f,
                contractBuyCurrency = "GBP",
                fxDate = Date().toString(),
                quoteId = "0011223343"
            ), FxContract(
                indicativeAmount = 158970f,
                fxRate = 0.1822f,
                contractBuyCurrency = "HKD",
                fxDate = Date().toString(),
                quoteId = "0011223342"
            ),
            FxContract(
                indicativeAmount = 6500000f,
                fxRate = 0.8677f,
                contractBuyCurrency = "NZD",
                fxDate = Date().toString(),
                quoteId = "0011223344"
            ),
            FxContract(
                indicativeAmount = 98500f,
                fxRate = 0.92677f,
                contractBuyCurrency = "AUD",
                fxDate = Date().toString(),
                quoteId = "0011223345"
            )
        )
    }
}

