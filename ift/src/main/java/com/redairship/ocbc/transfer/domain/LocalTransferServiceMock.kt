package com.redairship.ocbc.transfer.domain

import com.redairship.domain.DomainResponse
import com.redairship.ocbc.transfer.LocalTransferData
import com.redairship.ocbc.transfer.model.*
import com.redairship.ocbc.transfer.presentation.base.TransferStatus

class LocalTransferServiceMock : LocalTransferServiceInterface {
    override suspend fun getAccountList(functionCode: String): DomainResponse<AccountItemListModel, LocalTransferError> {
        return DomainResponse.Success(
            LocalTransferMockData.oneSingleCurrencyAccount
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
            TransferStatus.values().map {
                return@map TransferToTypeResponse(it.name, it.name)
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
        return DomainResponse.Success(CustomFields("", listOf()))
    }

    override suspend fun getCounterFX(code: String): DomainResponse<FxContract, LocalTransferError> {
        return DomainResponse.Error(LocalTransferError("No counter FX"))
    }

    override suspend fun getProductTransactionLimit(): DomainResponse<Unit, LocalTransferError> {
        return DomainResponse.Success(Unit)
    }

    override suspend fun getBeneficiaryList(): DomainResponse<List<BeneficiaryData>, LocalTransferError> {
        return DomainResponse.Success(listOf())
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
        return DomainResponse.Error(LocalTransferError("Not yet implemented"))
    }
}