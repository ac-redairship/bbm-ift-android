package com.redairship.ocbc.transfer.presentation.local

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redairship.ocbc.transfer.GetCheckValueDate
import com.ocbc.transfer.InsufficientBalanceException
import com.redairship.ocbc.transfer.LocalTransferData
import com.redairship.ocbc.transfer.UiState
import com.redairship.ocbc.transfer.LocalTransferCoordinatorInterface
import com.redairship.ocbc.transfer.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LocalTransferViewModel (
    private val localTransferRepository: LocalTransferCoordinatorInterface
) : ViewModel() {
    var _accountlist = MutableStateFlow<UiState<AccountItemListModel>>(UiState.Loading)
    val accountlist: StateFlow<UiState<AccountItemListModel>> = _accountlist.asStateFlow()

    var _transferToTypeList = MutableStateFlow<UiState<List<TransferToTypeResponse>>>(UiState.Loading)
    val transferToTypeList: StateFlow<UiState<List<TransferToTypeResponse>>> = _transferToTypeList.asStateFlow()

    var _fxContract = MutableStateFlow<UiState<FxContract>>(UiState.Loading)
    val fxContract: StateFlow<UiState<FxContract>> = _fxContract.asStateFlow()

    var _checkvaluedate = MutableStateFlow<UiState<CheckValueDate>>(UiState.Loading)
    val checkvaluedate: StateFlow<UiState<CheckValueDate>> = _checkvaluedate.asStateFlow()

    var _transferToConfirm = MutableStateFlow<UiState<InfoToStartDetailResponse>>(UiState.Loading)
    val transferToConfirm: StateFlow<UiState<InfoToStartDetailResponse>> = _transferToConfirm.asStateFlow()

    var _insufficientBalanceOutcome = MutableStateFlow<UiState<SubmitResponseData>>(UiState.NotLoading)
    val insufficientBalanceOutcome: StateFlow<UiState<SubmitResponseData>> = _insufficientBalanceOutcome.asStateFlow()

    fun initialState() {
        _insufficientBalanceOutcome.value = UiState.NotLoading
    }
    fun getLocalTransferData(): LocalTransferData {
        return localTransferRepository.localTransferData
    }

    fun updateProductCode(fucntionCode:String) {
        localTransferRepository.localTransferData?.fucntionCode = fucntionCode
    }

    fun updateLocalTransferData(data: LocalTransferData): LocalTransferData {
        localTransferRepository.localTransferData = data
        return localTransferRepository.localTransferData
    }

    fun getAccountList(functionCode:String) {
        viewModelScope.launch {
            localTransferRepository.getAccountList(functionCode)
                .catch {
                    _accountlist.value = UiState.Error(it)
                }
                .collect {
                    _accountlist.value = it
                }
        }
    }

    fun getCounterFX(code: String) {
        viewModelScope.launch {
            localTransferRepository.getCounterFX(code)
                .catch {
                    _fxContract.value = UiState.Error(it)
                }
                .collect {
                    localTransferRepository.localTransferData.fxContract = it.data
                    _fxContract.value = it
                }
        }
    }

    fun getProductTransactionLimit(date: String) {
        viewModelScope.launch {
            localTransferRepository.getProductTransactionLimit()
                .catch {
                    _checkvaluedate.value = UiState.Error(it)
                }
                .collect {
                    localTransferRepository.checkValueDate(date)
                        .catch {
                            _checkvaluedate.value = UiState.Error(it)
                        }
                        .collectLatest {
                            localTransferRepository.localTransferData?.getCheckValueDate = GetCheckValueDate(
                                valueDate = it.data!!.nextValueDate,
                                nextWorkingDay = it.data!!.nextWorkingDay,
                                maxDay = it.data!!.maxDay
                            )
                            _checkvaluedate.value = it
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

        if (localTransferRepository.localTransferData.defaultMinLimit
            > localTransferRepository.localTransferData.selectToAc.amount
            || localTransferRepository.localTransferData.selectFromAc.amount
            < localTransferRepository.localTransferData.selectToAc.amount) {
            _insufficientBalanceOutcome.value = UiState.Error(InsufficientBalanceException())
        } else {
//            localTransferRepository.getE2EEDetails()
//                .flatMapCompletable { it ->
//                    localTransferRepository.createPreviewSubmissionModel(it).doOnSuccess { response->
//                        localTransferRepository.doPreAndSubmit(response, type)
//                            .subscribe({
//                                if (it.status.equals("FAILED", true)) {
//                                    _insufficientBalanceOutcome.value = UiState.Error(
//                                        UnexpectedException("FAILED")
//                                    )
//                                } else
//                                    _insufficientBalanceOutcome.value = UiState.Success(it)
//                            }, {
//                                _insufficientBalanceOutcome.value = UiState.Error(NetworkException(it))
//                            })
//                    }.doOnError{
//                        _insufficientBalanceOutcome.value = UiState.Error(NetworkException(it))
//                    }.ignoreElement() ?: Completable.never()
//                }
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeBy({
//                    _insufficientBalanceOutcome.value = UiState.Error(NetworkException(it))
//                })
        }
    }

    fun useMyRate() {
        localTransferRepository.goToUseMyRate()
    }



}

