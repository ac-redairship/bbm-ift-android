package com.redairship.ocbc.transfer.model

import java.io.Serializable

data class BeneficiaryData(
    val id: String,
    val isAuthSmsOtp: String,
    val orgId: String,
    val name: String,
    val payType: String,
    val payDetail: PayDetail,
    val isLocalBank: Boolean,
    val bankDetailDTO: BankDetailDTO,
    val statusCode: String,
    val transactionVersion: String,
    val customizedStatus: String,
    val singleAccess: Boolean,
    val isSelected: Boolean
) : Serializable

data class BankDetailDTO(
    val bankCodeLinkingCd: String,
    val beneficiaryBankAddress1: String,
    val beneficiaryBankAddress2: String,
    val beneficiaryBankAddress3: String,
    val beneficiaryBankCd: String,
    val beneficiaryBankCountryCode: String,
    val beneficiaryBankName: String,
    val clearingCode: String,
    val swiftCode: String,
) : Serializable

data class PayDetail(
    val proxyType: String,
    val accountNo: String,
    val beneficiaryName: String,
    val beneficiaryAddress1: String,
    val beneficiaryAddress2: String,
    val beneficiaryAddress3: String,
    val beneficiaryCity: String,
    val beneficiaryStreet: String,
    val beneficiaryPostalCode: String,
    val beneficiaryResidentStatus: String,
    val beneficiaryState: String,
    val beneficiaryTaxId: String,
    val beneficiaryType: String,
    val businessRegistrationNo: String,
    val dialingPrefix: String,
    val emailAddress: String,
    val icNumber: String,
    val passportNo: String,
    val phoneNumber: String,
    val armyPoliceNo: String,
    val uenNumber: String,
    val virtualPaymentAddress: String,
) : Serializable
