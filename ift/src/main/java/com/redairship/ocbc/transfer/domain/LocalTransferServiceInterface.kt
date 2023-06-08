package com.redairship.ocbc.transfer.domain

import com.redairship.domain.DomainResponse
import com.redairship.domain.ServiceInterface
import com.redairship.ocbc.transfer.LocalTransferData
import com.redairship.ocbc.transfer.model.*

interface LocalTransferServiceInterface: ServiceInterface {

    suspend fun getAccountList(functionCode: String): DomainResponse<AccountItemListModel, LocalTransferError>
    suspend fun checkValueDate(date: String): DomainResponse<CheckValueDate, LocalTransferError>
    suspend fun getTransferToTypeList(): DomainResponse<List<TransferToTypeResponse>, LocalTransferError>
    suspend fun sendToConfirm(code: String): DomainResponse<InfoToStartDetailResponse, LocalTransferError>
    suspend fun checkInsufficientBalance(code: LocalTransferData): DomainResponse<InfoToStartDetailResponse, LocalTransferError>
    suspend fun getCurrencyList(): DomainResponse<CustomFields, LocalTransferError>
    suspend fun getCounterFX(code: String): DomainResponse<FxContract, LocalTransferError>
    suspend fun getProductTransactionLimit(): DomainResponse<Unit, LocalTransferError>
    suspend fun getBeneficiaryList(): DomainResponse<List<BeneficiaryData>, LocalTransferError>

    suspend fun getE2EEDetails(): DomainResponse<E2EEDetailsResponse, LocalTransferError>
    suspend fun createPreviewSubmissionModel(e2eeDetails: E2EEDetailsResponse): DomainResponse<E2EEDetailsResponse, LocalTransferError>
    suspend fun doPreAndSubmit(
        e2eeDetails: E2EEDetailsResponse,
        type: TransactionPreSubmitType
    ): DomainResponse<SubmitResponseData, LocalTransferError>

    fun useOldFxRatesScreen(): Boolean
    fun showOldFxRates()
}