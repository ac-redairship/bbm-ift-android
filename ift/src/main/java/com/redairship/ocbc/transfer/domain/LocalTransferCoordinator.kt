package com.redairship.ocbc.transfer.domain

import android.content.Context
import android.content.Intent
import com.ocbc.transfer.presentation.local.TransferActivity
import com.redairship.domain.AppDomainManagerInterface
import com.redairship.domain.common.Coordinator
import com.redairship.domain.common.MainCoordinatorInterface
import com.redairship.domain.getService
import com.redairship.ocbc.transfer.LocalTransferCoordinatorInterface
import com.redairship.ocbc.transfer.LocalTransferData
import com.redairship.ocbc.transfer.UiState
import com.redairship.ocbc.transfer.model.*
import com.redairship.ocbc.transfer.presentation.common.UseMyRateFragment
import com.redairship.ocbc.transfer.utils.flowFromAppDomain
import com.redairship.ocbc.transfer.utils.flowFromAppDomain2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow


class LocalTransferCoordinator(
    private val appDomainManager: AppDomainManagerInterface,
    private val coordinator: LocalCoordinator,
    private val context: Context
) : LocalTransferCoordinatorInterface {

    val service by lazy {
        appDomainManager.serviceCentre().getService<LocalTransferServiceInterface>()
    }
    override var localTransferData: LocalTransferData = LocalTransferData()

    override suspend fun getAccountList(functionCode: String): Flow<UiState<AccountItemListModel>> = flowFromAppDomain {
        service.getAccountList(functionCode)
    }

    override suspend fun checkValueDate(date: String): Flow<UiState<CheckValueDate>> = flowFromAppDomain {
        service.checkValueDate(date)
    }

    override suspend fun getTransferToTypeList(): Flow<UiState<List<TransferToTypeResponse>>> = flowFromAppDomain {
        service.getTransferToTypeList()
    }

    override suspend fun sendToConfirm(code: String): Flow<UiState<InfoToStartDetailResponse>> = flowFromAppDomain {
        service.sendToConfirm(code)
    }

    override suspend fun checkInsufficientBalance(code: LocalTransferData): Flow<UiState<InfoToStartDetailResponse>> = flowFromAppDomain {
        service.checkInsufficientBalance(code)
    }

    override suspend fun getCurrencyList(): Flow<UiState<CustomFields>> = flowFromAppDomain {
        service.getCurrencyList()
    }

    override suspend fun getCounterFX(code: String): Flow<UiState<FxContract>> = flowFromAppDomain {
        service.getCounterFX(code)
    }

    override suspend fun getProductTransactionLimit(): Flow<UiState<Unit>> = flowFromAppDomain {
        service.getProductTransactionLimit()
    }

    override suspend fun getBeneficiaryList(): Flow<UiState<List<BeneficiaryData>>> = flowFromAppDomain {
        service.getBeneficiaryList()
    }

    override suspend fun getE2EEDetails(): Flow<E2EEDetailsResponse> = flowFromAppDomain2 {
        service.getE2EEDetails()
    }

    override suspend fun createPreviewSubmissionModel(e2eeDetails: E2EEDetailsResponse): Flow<E2EEDetailsResponse> = flowFromAppDomain2 {
        service.getE2EEDetails()
    }

    override suspend fun doPreAndSubmit(
        e2eeDetails: E2EEDetailsResponse,
        type: TransactionPreSubmitType
    ): Flow<SubmitResponseData> = flowFromAppDomain2 {
        service.doPreAndSubmit(e2eeDetails, type)
    }
    override var childCoordinators: List<Coordinator> = emptyList()

    override var parentCoordinator: Coordinator? = coordinator.mainCoordinator

    override var mainCoordinator: MainCoordinatorInterface = coordinator.mainCoordinator

    override fun newInstance(): Coordinator {
        TODO("Not yet implemented")
    }

    override fun start(vararg args: Any) {
        context.startActivity(Intent(context, TransferActivity::class.java))
    }


    override fun goToUseMyRate() {
        coordinator.navigateTo(UseMyRateFragment.newInstance(), false)
    }
}