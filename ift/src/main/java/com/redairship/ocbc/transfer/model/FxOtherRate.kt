package com.redairship.ocbc.transfer.model

import java.io.Serializable

class FxOtherRate(
    val contractNo: String? = null,
    val murexNo: String? = null,
    val rateTier1: String? = null,
    val outstandingBuyAmount: String? = null,
    val outstandingSellAmount: String? = null,
    val originalBuyAmount: String? = null,
    val originalSellAmount: String? = null,
    val dealerName: String? = null,
    val equivalentAmount: String? = null,
    val dateMaturity: String? = null,
    var fromCurrency: String? = null,
    var toCurrency: String? = null,
    val fxIndicativeAmount: String?=null,
    val fxEquivalentAmount: String?=null
) : Serializable