package com.redairship.ocbc.transfer.presentation.base

import android.content.Context
import com.ocbc.transfer.R

data class CurrencyCode(
    val code: String,
    val currencyName: String,
    val sequence: Int,
    val flag: String
)

enum class CurrencyCodeEnum(
    val code: String,
    val currencyName: String,
    val favorite: Boolean,
    val flag: String
) {
    USD("USD", "US Dollar", true, "currency_usd"),
    EUR("EUR", "Euro", true, "currency_eur"),
    GBP("GBP", "GBP", false, "currency_gbp"),
    AUD("AUD", "AUD", false, "currency_aud"),
    JPY("JPY", "JPY", false, "currency_jpy"),
    HKD("HKD", "HKD", false, "currency_hkd"),
    SGD("SGD", "SGD", true, "currency_sgd"),
    AED("AED", "AED", false, "currency_aed"),
    BND("BND", "BND", false, "currency_bnd"),
    CAD("CAD", "CAD", false, "currency_cad"),
    CHF("CHF", "CHF", false, "currency_chf"),
    CNH("CNH", "CNH", false, "currency_cnh"),
    DKK("DKK", "DKK", false, "currency_dkk"),
    IDR("IDR", "IDR", false, "currency_idr"),
    INR("INR", "INR", false, "currency_inr"),
    LKR("LKR", "LKR", false, "currency_lkr"),
    MXN("MXN", "MXN", false, "currency_mxn"),
    MYR("MYR", "MYR", false, "currency_myr"),
    NOK("NOK", "NOK", false, "currency_nok"),
    NZD("NZD", "NZD", false, "currency_nzd"),
    PHP("PHP", "PHP", false, "currency_php"),
    RUB("RUB", "RUB", false, "currency_rub"),
    SAR("SAR", "SAR", false, "currency_sar"),
    SEK("SEK", "SEK", false, "currency_sek"),
    THB("THB", "THB", false, "currency_thb"),
    TRY("TRY", "TRY", false, "currency_try"),
    TWD("TWD", "TWD", false, "currency_twd"),
    ZAR("ZAR", "ZAR", false, "currency_zar"),
    UNKNOWN("", "", false, "currency_nil");


    companion object {
        private val codes = values().associateBy { it.code }
        fun findCurrency(code: String): CurrencyCodeEnum {
            return codes[code] ?: UNKNOWN
        }
    }
}

fun getFlagResouce(code: String, context: Context): Int {
    val currencyCode: CurrencyCodeEnum = CurrencyCodeEnum.findCurrency(code)
    if (currencyCode.flag.isNotEmpty()) {
        return context.resources.getIdentifier(currencyCode.flag, "drawable", context.packageName)
    }
    return R.drawable.layout_border

}
