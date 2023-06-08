package com.redairship.ocbc.transfer.presentation.local

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redairship.ocbc.bb.components.views.fragments.errors.GenericMessageFragment
import com.redairship.ocbc.transfer.*
import com.redairship.ocbc.transfer.domain.LocalTransferError
import com.redairship.ocbc.transfer.model.*
import com.redairship.ocbc.transfer.utils.DomainException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LocalTransferViewModel (
    private val localTransferRepository: LocalTransferCoordinatorInterface
) : ViewModel() {

    sealed interface ResponseFlow {
        object SENDER: ResponseFlow
        object RECIPIENT: ResponseFlow
    }

    var responseFlow: ResponseFlow = ResponseFlow.RECIPIENT

    private var _accountList = MutableStateFlow<UiState<AccountItemListModel>>(UiState.Loading)
    val accountList: StateFlow<UiState<AccountItemListModel>> = _accountList.asStateFlow()

    private var _transferToTypeList = MutableStateFlow<UiState<List<TransferToTypeResponse>>>(UiState.Loading)
    val transferToTypeList: StateFlow<UiState<List<TransferToTypeResponse>>> = _transferToTypeList.asStateFlow()

    private var _fxContract = MutableStateFlow<UiState<FxContract>>(UiState.Loading)
    val fxContract: StateFlow<UiState<FxContract>> = _fxContract.asStateFlow()

    private var _checkValueDate = MutableStateFlow<UiState<CheckValueDate>>(UiState.Loading)
    val checkValueDate: StateFlow<UiState<CheckValueDate>> = _checkValueDate.asStateFlow()

    private var _transferToConfirm = MutableStateFlow<UiState<InfoToStartDetailResponse>>(UiState.Loading)
    val transferToConfirm: StateFlow<UiState<InfoToStartDetailResponse>> = _transferToConfirm.asStateFlow()

    private var _insufficientBalanceOutcome = MutableStateFlow<UiState<SubmitResponseData>>(UiState.NotLoading)
    val insufficientBalanceOutcome: StateFlow<UiState<SubmitResponseData>> = _insufficientBalanceOutcome.asStateFlow()

    private val _localTransferData = MutableLiveData<LocalTransferData>()
    val localTransferData: LiveData<LocalTransferData> = _localTransferData

    fun initialState() {
        _insufficientBalanceOutcome.value = UiState.NotLoading
    }

    fun updateLocalTransferData(newData: LocalTransferData) {
        _localTransferData.value = newData.copy()
    }

    fun updateProductCode(functionCode:String) {
        val localTransferData = localTransferData.value ?: return
        updateLocalTransferData(
            localTransferData.copy(
                functionCode = functionCode
            )
        )
    }

    fun getAccountList(functionCode:String) {
        viewModelScope.launch {
            localTransferRepository.getAccountList(functionCode)
                .catch {
                    _accountList.value = UiState.Error(it)
                }
                .collect {
                    _accountList.value = it
                }
        }
    }

    fun getCounterFX(isSender: Boolean, code: String, isUserRate: Boolean = false) {
        responseFlow = if (isSender) ResponseFlow.SENDER else ResponseFlow.RECIPIENT
        viewModelScope.launch {
            localTransferRepository.getCounterFX(code)
                .catch {
                    _fxContract.value = UiState.Error(it)
                }
                .collect {
                    updateLocalTransferData(localTransferData.value!!.copy(
                        fxContract = it.data?.copy(
                            isUserRate = isUserRate
                        )
                    ))
                    _fxContract.value = UiState.Success(it.data!!.copy(isUserRate = isUserRate))
                }
        }
    }

    fun getProductTransactionLimit(date: String) {
        viewModelScope.launch {
            localTransferRepository.getProductTransactionLimit()
                .catch {
                    _checkValueDate.value = UiState.Error(it)
                }
                .collect {
                    localTransferRepository.checkValueDate(date)
                        .catch {
                            _checkValueDate.value = UiState.Error(it)
                        }
                        .collectLatest {
                            updateLocalTransferData(
                                localTransferData.value!!.copy(
                                    getCheckValueDate = GetCheckValueDate(
                                        valueDate = it.data!!.nextValueDate,
                                        nextWorkingDay = it.data!!.nextWorkingDay,
                                        maxDay = it.data!!.maxDay
                                    )
                                )
                            )
                            _checkValueDate.value = it
                        }

                }
        }
    }

    fun getTransferToTypeList() {
        viewModelScope.launch {
            localTransferRepository.getTransferToTypeList()
                .catch {
                    _transferToTypeList.value = UiState.Error(it)
                }
                .collect {
                    _transferToTypeList.value = it
                }
        }
    }

    fun sendToConfirm(code:String) {
        viewModelScope.launch {
            localTransferRepository.sendToConfirm(code)
                .catch {
                    _transferToConfirm.value = UiState.Error(it)
                }
                .collect {
                    _transferToConfirm.value = it
                }
        }
    }

    fun doPreSubmit(type: TransactionPreSubmitType) {
        _insufficientBalanceOutcome.value = UiState.Loading

        val recipientAccountData = localTransferData.value?.recipientAccountData ?: return
        if (type != TransactionPreSubmitType.SUBMIT && isValueGreaterThanBalance(recipientAccountData)) {
            _insufficientBalanceOutcome.value = UiState.Error(InsufficientBalanceException())
        } else {
            viewModelScope.launch {
                localTransferRepository.getE2EEDetails()
                    .collectLatest { it ->
                        localTransferRepository.doPreAndSubmit(it, type)
                            .catch {
                                if (it is DomainException) {
                                    if (it.error is LocalTransferError.SameDayFee) {
                                        _insufficientBalanceOutcome.value = UiState.Error(AcceptSameDayTransferFee())
                                    } else {
                                        _insufficientBalanceOutcome.value = UiState.Error(InsufficientBalanceException())
                                    }
                                } else {
                                    _insufficientBalanceOutcome.value = UiState.Error(UnexpectedException(it.message ?: "unexpected exception"))
                                }

                            }
                            .collectLatest {
                                _insufficientBalanceOutcome.value = UiState.Success(it)
                            }


//                        localTransferRepository.createPreviewSubmissionModel(it).doOnSuccess { response->
//                            localTransferRepository.doPreAndSubmit(response, type)
//                                .subscribe({
//                                    if (it.status.equals("FAILED", true)) {
//                                        _insufficientBalanceOutcome.value = UiState.Error(
//                                            UnexpectedException("FAILED")
//                                        )
//                                    } else
//                                        _insufficientBalanceOutcome.value = UiState.Success(it)
//                                }, {
//                                    _insufficientBalanceOutcome.value = UiState.Error(NetworkException(it))
//                                })
//                        }.doOnError{
//                            _insufficientBalanceOutcome.value = UiState.Error(NetworkException(it))
//                        }.ignoreElement() ?: Completable.never()
                    }
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribeBy({
//                        _insufficientBalanceOutcome.value = UiState.Error(NetworkException(it))
//                    })
            }

        }

        _insufficientBalanceOutcome.value = UiState.NotLoading

    }

    private fun isValueGreaterThanBalance(recipientAccountData: AccountItemModel): Boolean {
        val localTransferData = localTransferData.value ?: return true
        val amountToSend = recipientAccountData.amount.value.movePointLeft(2)
        if (localTransferData.defaultMinLimit.toBigDecimal().movePointLeft(2) > amountToSend) return true
        if (amountToSend > localTransferData.senderAccountData.amount.value) return true

        return false
    }

    fun goToOneTokenVerification() {
        localTransferRepository.mainCoordinator.goToOneTokenOffline()
    }

    fun goToEmailVerification(otpFragment: Fragment) {
        localTransferRepository.goToEmailVerification(otpFragment)
    }

    fun showGenericErrorScreen(
        tag: String,
        title: String,
        description: String,
        buttonText: String,
        type: Int,
        interceptDoneAction: Boolean = false,
        listener: GenericMessageFragment.Listener?,
        closeListener: GenericMessageFragment.CloseListener?,
        hasCloseIcon: Boolean = false,
        interceptCloseAction: Boolean = false
    ) {
        localTransferRepository.showGenericErrorScreen(
            tag = tag,
            title = title,
            description = description,
            buttonText = buttonText,
            type = type,
            interceptDoneAction = interceptDoneAction,
            listener = listener,
            closeListener = closeListener,
            hasCloseIcon = hasCloseIcon,
            interceptCloseAction = interceptCloseAction
        )
    }

    fun goToInternalFundsTransfer() {
        localTransferRepository.goToInternalFundsTransferFragment()
    }

    fun goToTransactionSummary() {
        localTransferRepository.goToTransactionSummary()
    }

    fun goToUseMyRate() {
        localTransferRepository.goToUseMyRate()
    }


}

