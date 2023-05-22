package com.redairship.ocbc.transfer.domain

import com.redairship.domain.DomainResponse
import com.redairship.domain.mock.MockDelayDuration
import com.redairship.domain.mock.mockDelay
import com.redairship.ocbc.transfer.LocalTransferData
import com.redairship.ocbc.transfer.model.*
import com.redairship.ocbc.transfer.presentation.base.CurrencyCodeEnum
import com.redairship.ocbc.transfer.presentation.base.TransferStatus

class LocalTransferServiceMock : LocalTransferServiceInterface {
    override suspend fun getAccountList(functionCode: String): DomainResponse<AccountItemListModel, LocalTransferError> {
        return DomainResponse.Success(
            LocalTransferMockData.normalAccounts
        )
    }

    override suspend fun checkValueDate(date: String): DomainResponse<CheckValueDate, LocalTransferError> {
        return DomainResponse.Success(
            CheckValueDate(
                workingHourFlag = true,
                workingDayFlag = true,
                nextWorkingDayFlag = true,
                evalDateTime = "",
                nextDayOffset = 0,
                variant = "",
                groupCodes = listOf(),
                isWorkingDay = true,
                isWorkingHour = true,
                nextWorkingDay = "",
                nextValueDate = "",
                maxDay = 1
            )
        )
    }

    override suspend fun getTransferToTypeList(): DomainResponse<List<TransferToTypeResponse>, LocalTransferError> {
        return DomainResponse.Success(
            TransferStatus.values()
                .filter {
                    return@filter when(it) {
                        TransferStatus.TransferTo,
                        TransferStatus.TransferFrom,
                        TransferStatus.TransferMakerReview,
                        TransferStatus.TransferConfirm -> false
                        else -> true
                    }
                }
                .map {
                    return@map TransferToTypeResponse(it.description, it.name)
                }
        )
    }

    override suspend fun sendToConfirm(code: String): DomainResponse<InfoToStartDetailResponse, LocalTransferError> {
        return DomainResponse.Success(InfoToStartDetailResponse(""))
    }

    override suspend fun checkInsufficientBalance(code: LocalTransferData): DomainResponse<InfoToStartDetailResponse, LocalTransferError> {
        return DomainResponse.Success(InfoToStartDetailResponse(""))
    }

    override suspend fun getCurrencyList(): DomainResponse<CustomFields, LocalTransferError> {
        return DomainResponse.Success(CustomFields("", CurrencyCodeEnum.values().map {
            Currency(entry = it.code, if (it.favorite) 0 else 999)
        }))
    }

    override suspend fun getCounterFX(code: String): DomainResponse<FxContract, LocalTransferError> {
        mockDelay(MockDelayDuration.VERY_LONG)
        val contracts = LocalTransferMockData.getMockUserFXContracts()
        val foundContract = contracts.find { it.contractBuyCurrency == code.uppercase() }
            ?: contracts.first()
        return DomainResponse.Success(foundContract)
    }

    override suspend fun getProductTransactionLimit(): DomainResponse<Unit, LocalTransferError> {
        return DomainResponse.Success(Unit)
    }

    override suspend fun getBeneficiaryList(): DomainResponse<List<BeneficiaryData>, LocalTransferError> {
        return DomainResponse.Success(
            listOf(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16).map {
                BeneficiaryData(
                    id = it.toString(),
                    isAuthSmsOtp = "1",
                    orgId = "1",
                    name = if (it == 4) "OCBC Bank Pte Ltd also if really long max 2 lines and will truncate if it goes beyond the 2 lines" else "Sample User $it",
                    payType = if (it == 3) "Cindy Chua really long max 2 lines and will truncate if it goes beyond the 2 lines" else "OCBC Bank Pte. Ltd. ",
                    payDetail = PayDetail(
                        proxyType = "",
                        accountNo = "12345678$it",
                        beneficiaryName = "Cindy Chua",
                        beneficiaryAddress1 = "",
                        beneficiaryAddress2 = "",
                        beneficiaryAddress3 = "",
                        beneficiaryCity = "",
                        beneficiaryStreet = "",
                        beneficiaryPostalCode = "",
                        beneficiaryResidentStatus = "",
                        beneficiaryState = "",
                        beneficiaryTaxId = "",
                        beneficiaryType = "",
                        businessRegistrationNo = "",
                        dialingPrefix = "",
                        emailAddress = "",
                        icNumber = "",
                        passportNo = "",
                        phoneNumber = "",
                        armyPoliceNo = "",
                        uenNumber = "",
                        virtualPaymentAddress = "",
                    ),
                    isLocalBank = it % 2 == 0,
                    bankDetailDTO = BankDetailDTO(
                        bankCodeLinkingCd = "",
                        beneficiaryBankAddress1 = "",
                        beneficiaryBankAddress2 = "",
                        beneficiaryBankAddress3 = "",
                        beneficiaryBankCd = "",
                        beneficiaryBankCountryCode = "United States",
                        beneficiaryBankName = "",
                        clearingCode = "",
                        swiftCode = "",
                    ),
                    statusCode = "",
                    transactionVersion = "",
                    customizedStatus = "",
                    singleAccess = true,
                    isSelected = false
                )
            }
        )
    }

    override suspend fun getE2EEDetails(): DomainResponse<E2EEDetailsResponse, LocalTransferError> {
        return DomainResponse.Success(
            E2EEDetailsResponse(
                encryptKey = "",
                exponent = "",
                modulus = "",
                securityNonce = "",
                productCode = "",
            )
        )
    }

    override suspend fun createPreviewSubmissionModel(e2eeDetails: E2EEDetailsResponse): DomainResponse<E2EEDetailsResponse, LocalTransferError> {
        return DomainResponse.Success(
            E2EEDetailsResponse(
                encryptKey = "",
                exponent = "",
                modulus = "",
                securityNonce = "",
                productCode = "",
            )
        )
    }

    override suspend fun doPreAndSubmit(
        e2eeDetails: E2EEDetailsResponse,
        type: TransactionPreSubmitType
    ): DomainResponse<SubmitResponseData, LocalTransferError> {
        mockDelay(MockDelayDuration.LONG)
        return DomainResponse.Success(SubmitResponseData())
//        return DomainResponse.Error(LocalTransferError.SameDayFee)
//        return DomainResponse.Error(LocalTransferError.ServerResponseError("Not yet implemented"))
    }
}