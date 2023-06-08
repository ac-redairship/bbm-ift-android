package com.redairship.ocbc.transfer.domain

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.redairship.domain.AppDomainManagerInterface
import com.redairship.domain.DomainResponse
import com.redairship.domain.common.Coordinator
import com.redairship.domain.common.MainCoordinatorInterface
import com.redairship.domain.common.types.CaseInfo
import com.redairship.domain.common.types.OtpInfo
import com.redairship.domain.common.types.TwoFactorResult
import com.redairship.domain.common.types.organization.AuthorizedUser
import com.redairship.domain.common.types.organization.CompanyInfo
import com.redairship.domain.common.types.organization.ContactPerson
import com.redairship.domain.common.types.organization.OwnershipType
import com.redairship.domain.common.types.personal.AddressInfo
import com.redairship.domain.common.types.services.BusinessAccount
import com.redairship.domain.common.types.services.BusinessService
import com.redairship.domain.getService
import com.redairship.domain.mock.MockDelayDuration
import com.redairship.domain.mock.MockDomainConfiguration
import com.redairship.domain.mock.MockKey
import com.redairship.domain.mock.mockDelay
import com.redairship.ocbc.bb.components.views.fragments.errors.GenericMessageFragment
import com.redairship.ocbc.bb.components.views.fragments.errors.GenericMessageType
import com.redairship.ocbc.transfer.LocalTransferCoordinatorInterface
import com.redairship.ocbc.transfer.LocalTransferData
import com.redairship.ocbc.transfer.UiState
import com.redairship.ocbc.transfer.model.*
import com.redairship.ocbc.transfer.presentation.common.TransferConfirmFragment
import com.redairship.ocbc.transfer.presentation.transfer.transferto.UseMyRateFragment
import com.redairship.ocbc.transfer.presentation.local.TransferActivity
import com.redairship.ocbc.transfer.presentation.transfer.local.InternalFundsTransferFragment
import com.redairship.ocbc.transfer.utils.flowFromAppDomain
import com.redairship.ocbc.transfer.utils.flowFromAppDomain2
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

    override fun goToInternalFundsTransferFragment() {
        coordinator.navigateTo(InternalFundsTransferFragment(), true)
    }

    override fun goToTransactionSummary() {
        coordinator.navigateTo(TransferConfirmFragment(), true)
    }

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
//        coordinator.navigateTo(TransferEntryFragment(), true)
        context.startActivity(Intent(context, TransferActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    override fun useOldFxRatesScreen(): Boolean = service.useOldFxRatesScreen()

    override fun goToUseMyRate() {
        if (useOldFxRatesScreen()) {
            Toast.makeText(context, "Go to existing fx rates", Toast.LENGTH_SHORT).show()
            service.showOldFxRates()
        } else {
            coordinator.navigateTo(UseMyRateFragment.newInstance(), true)
        }
    }

    override fun verifyOtp(otp: String) = flowFromAppDomain2 {
        mockDelay(MockDelayDuration.LONG)
        return@flowFromAppDomain2 DomainResponse.Success(
            TwoFactorResult(
                isSuccessful = true,
                errorDescription = "",
                remainingAttempts = 3,
                caseInfo = normal
            )
        )
    }

    val normal = CaseInfo(
        address = AddressInfo(
            line1 = "",
            line2 = "",
            country = "",
            postalCode = "",
            formatted = "100 Apple Road #01-01,\n Singapore 123456"
        ),
        companyInfo = CompanyInfo(
            phoneNumber = "+65 1234 5678",
//            officeAddress = "100 Apple Road #01-01, Singapore 123456",
            mailingAddress = "100 Apple Road #01-01, Singapore 123456",
            ownershipType = OwnershipType.SOLE
        ),
        contactPersons = listOf(
            ContactPerson(
                name = "Lim Zhi Wei Alex",
                phoneNumber = "+65 9988 3322",
                email = "zhiwelim-alex@mail.com"
            ),
            ContactPerson(
                name = "Lau Chee Hong",
                phoneNumber = "+65 9977 1234",
                email = "cheehonglau@mail.com"
            )
        ),
        authorisedUsers = listOf(
            AuthorizedUser(
                name = "Lim Zhi Wei Alex",
                phoneNumber = "+65 9988 3322",
                email = "zhiwelim-alex@mail.com",
                hasAuthorisedSignatories = true,
                isSigningOnResolution = true,
                isAuthorisedPerson = true,
                businessServices = listOf(
                    BusinessService(
                        code = "IB",
                        label = "Internet Banking"
                    ),
                    BusinessService(
                        code = "Alert",
                        label = "eAlert"
                    ),
                    BusinessService(
                        code = "DC", label = "Business Debit Card"
                    ),
                )
            ),
            AuthorizedUser(
                name = "Lau Chee Hong",
                phoneNumber = "+65 9977 1234",
                email = "cheehonglau@mail.com",
                hasAuthorisedSignatories = true,
                isSigningOnResolution = false,
                isAuthorisedPerson = false,
                businessServices = listOf(
                    BusinessService(code = "IB", label = "Internet Banking"),
                    BusinessService(code = "Alert", label = "eAlert"),
                )
            ),
        ),
        businessAccounts = listOf(
            BusinessAccount(
                name = "Business Entrepreneur Account",
                currency = "SGD"
            ),
            BusinessAccount(
                name = "Foreign Currency Call Account ",
                currency = "HKD"
            ),
        ),
        businessServices = listOf(
            BusinessService(code = "IB", label = "Internet Banking"),
            BusinessService(code = "Alert", label = "SMS / Email alerts"),
            BusinessService(code = "DC", label = "Business debit card"),
        ),
        beneficialOwner = listOf("Lim Zhi Wei Alex", "Lau Chee Hong"),
        authorisedDate = "17 September 2021"
    )

    override fun goToOneTokenVerification(otpFragment: Any) {
        if (otpFragment !is Fragment) throw IllegalArgumentException("otpFragment should be an instance of Fragment")
        coordinator.navigateTo(otpFragment, true)
    }

    override fun goToEmailVerification(otpFragment: Any) {
        if (otpFragment !is Fragment) throw IllegalArgumentException("otpFragment should be an instance of Fragment")
        coordinator.navigateTo(otpFragment, true)
    }

    override fun <LISTENER, CLOSE_LISTENER> showGenericErrorScreen(
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
    ) {
        coordinator.navigateTo(
            fragment = GenericMessageFragment.newInstance(
                title,
                description,
                buttonText,
                GenericMessageType.findTypeById(type),
                interceptDoneAction,
                hasCloseIcon,
                interceptCloseAction
            ).apply {
                if (listener is GenericMessageFragment.Listener) this.listener = listener
                if (closeListener is GenericMessageFragment.CloseListener) this.closeListener =
                    closeListener
            },
            addToBackStack = true
        )
    }

    override fun resendOtp() = flowFromAppDomain {
        val otpInfo =
            otpInfo ?: return@flowFromAppDomain DomainResponse.Error(PassportAuthStatus.Default)
        passportAuthService.resendOtp(otpInfo)
    }.onEach {
        otpInfo = it
    }
}