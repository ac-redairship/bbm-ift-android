package com.redairship.ocbc.transfer

import com.redairship.domain.common.Coordinator
import com.redairship.domain.common.types.OtpInfo
import com.redairship.domain.common.types.TwoFactorResult
import com.redairship.ocbc.transfer.model.*
import kotlinx.coroutines.flow.Flow

interface LocalTransferCoordinatorInterface: Coordinator {
    var localTransferData: LocalTransferData

    suspend fun getAccountList(functionCode:String): Flow<UiState<AccountItemListModel>>
    suspend fun checkValueDate(date: String): Flow<UiState<CheckValueDate>>
    suspend fun getTransferToTypeList(): Flow<UiState<List<TransferToTypeResponse>>>
    suspend fun sendToConfirm(code:String): Flow<UiState<InfoToStartDetailResponse>>
    suspend fun checkInsufficientBalance(code: LocalTransferData): Flow<UiState<InfoToStartDetailResponse>>
    suspend fun getCurrencyList(): Flow<UiState<CustomFields>>
    suspend fun getCounterFX(code: String): Flow<UiState<FxContract>>
    suspend fun getProductTransactionLimit(): Flow<UiState<Unit>>
    suspend fun getBeneficiaryList(): Flow<UiState<List<BeneficiaryData>>>

    suspend fun getE2EEDetails(): Flow<E2EEDetailsResponse>
    suspend fun createPreviewSubmissionModel(e2eeDetails: E2EEDetailsResponse): Flow<E2EEDetailsResponse>
    suspend fun doPreAndSubmit(e2eeDetails: E2EEDetailsResponse, type: TransactionPreSubmitType): Flow<SubmitResponseData>
    fun goToUseMyRate()
    fun useOldFxRatesScreen(): Boolean
    fun verifyOtp(otp: String): Flow<TwoFactorResult>
    fun goToOneTokenVerification(otpFragment: Any)
    fun goToEmailVerification(otpFragment: Any)
    fun goToInternalFundsTransferFragment()
    fun goToTransactionSummary()

    fun <LISTENER, CLOSE_LISTENER> showGenericErrorScreen(
        tag: String,
        title: String,
        description: String,
        buttonText: String,
        type: Int,
        interceptDoneAction: Boolean,
        listener: LISTENER?,
        closeListener: CLOSE_LISTENER?,
        hasCloseIcon: Boolean,
        interceptCloseAction: Boolean
    )

    fun resendOtp(): Flow<OtpInfo>


}