package com.redairship.ocbc.transfer.presentation.base

import android.content.Context
import com.ocbc.transfer.R

data class CurrencyCode(val code: String,val currencyName:String,val sequence:Int,val flag:String)

enum class CurrencyCodeEnum(val code: String,val currencyName:String,val favorite:Boolean,val flag:String) {
    USD("USD","US Dollar",true,"country_usd"),
    EUR("EUR","Euro",true,"country_eur"),
    GBP("GBP","GBP",false,"country_gbp"),
    AUD("AUD","AUD",false,"country_aud"),
    JPY("JPY","JPY",false,"country_jpy"),
    HKD("HKD","HKD",false,"country_hkd"),
    SGD("SGD","SGD",true,"country_sgd"),
    AED("AED","AED",false,"country_aed"),
    BND("BND","BND",false,"country_bnd"),
    CAD("CAD","CAD",false,"country_cad"),
    CHF("CHF","CHF",false,"country_chf"),
    CNH("CNH","CNH",false,"country_cnh"),
    DKK("DKK","DKK",false,"country_dkk"),
    IDR("IDR","IDR",false,"country_idr"),
    INR("INR","INR",false,"country_inr"),
    LKR("LKR","LKR",false,"country_lkr"),
    MXN("MXN","MXN",false,"country_mxn"),
    MYR("MYR","MYR",false,"country_myr"),
    NOK("NOK","NOK",false,"country_nok"),
    NZD("NZD","NZD",false,"country_nzd"),
    PHP("PHP","PHP",false,"country_php"),
    RUB("RUB","RUB",false,"country_rub"),
    SAR("SAR","SAR",false,"country_sar"),
    SEK("SEK","SEK",false,"country_sek"),
    THB("THB","THB",false,"country_thb"),
    TRY("TRY","TRY",false,"country_try"),
    TWD("TWD","TWD",false,"country_twd"),
    ZAR("ZAR","ZAR",false,"country_zar"),
    UNKNOWN("","",false,"layout_border");


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
        val id = context.resources.getIdentifier(currencyCode.flag, "drawable", context.packageName)
        return id
    }
    return R.drawable.layout_border

}
