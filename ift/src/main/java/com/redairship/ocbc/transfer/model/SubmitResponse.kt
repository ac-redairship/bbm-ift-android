package com.redairship.ocbc.transfer.model

import java.io.Serializable

data class SubmitResponseData(
    val transactionRecordGroupId: String = "",
    val valueDate: String = "",
    val transactionAmount: Int = 0,
    val groupType: String = "",
    val currencyCode: String = "",
    val customerReferenceNo: String = "",
    val reasonForRejection: String = "",
    val accountNo: String = "",
    val accountAlias: String = "",
    val accountName: String = "",
    val accountCurrencyCode: String = "",
    val accountEntity: String = "",
    val singleAccess: Boolean = false,
    val beneficiaryName: String = "",
    val beneficiaryAccountNo: String = "",
    val beneficiaryAcctCurrency: String = "",
    val beneficiaryAccountEntity: String = "",
    val beneficiaryAdviceEmail: String = "",
    val beneficiaryAdviceFax: String = "",
    val beneficiaryAdviceDetails: String = "",
    val beneficiaryAdviceFooter: String = "",
    val notifyBeneficiary: Boolean = false,
    val paymentDetails: String = "",
    val sendBy: String = "",
    val notifySoftToken: Int = 0,
    val transactionVersion: Int = 0,
    val createdTime: String = "",
) : Serializable