package com.redairship.ocbc.transfer.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redairship.ocbc.transfer.LocalTransferCoordinatorInterface
import com.redairship.ocbc.transfer.UiState
import com.redairship.ocbc.transfer.model.AccountItemListModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AccountListBottomSheetViewModel (
    private val localTransferRepository: LocalTransferCoordinatorInterface
) : ViewModel() {
    var _accountlist = MutableStateFlow<UiState<AccountItemListModel>>(UiState.Loading)
    val accountlist: StateFlow<UiState<AccountItemListModel>> = _accountlist.asStateFlow()

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
}