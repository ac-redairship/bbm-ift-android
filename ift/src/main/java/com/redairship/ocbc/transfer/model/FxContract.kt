package com.redairship.ocbc.transfer.model

import java.io.Serializable

data class FxContract(
    val fxRate: Float? = null,
    val fxDate: String? = null,
    val conversionType: String? = null,
    val crossConvertedAmount: String? = null,
    val convertedToSGD: String? = null,
    val dealhubPromoCd: String? = null,
    val quoteId: String? = null,
    val rfqTimestampString: String? = null,
    val indicativeAmount: Float? = null,
    val dealHubRate: Boolean = false,
    val fxRateTypeCode: String? = null,
    val modeOfContract: String? = null,
    val indicativeRateText: String? = null,
    val symbol: String? = null,
    val contractBuyCurrency: String? = null
) : Serializable