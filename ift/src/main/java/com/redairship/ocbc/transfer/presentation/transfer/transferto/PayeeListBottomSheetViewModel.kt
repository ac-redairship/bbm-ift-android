package com.redairship.ocbc.transfer.presentation.transfer.transferto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redairship.ocbc.transfer.LocalTransferCoordinatorInterface
import com.redairship.ocbc.transfer.UiState
import com.redairship.ocbc.transfer.model.BeneficiaryData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PayeeListBottomSheetViewModel (
    private val localTransferRepository: LocalTransferCoordinatorInterface
) : ViewModel() {
    private var _beneficiarylist = MutableStateFlow<UiState<List<BeneficiaryData>>>(UiState.Loading)
    val beneficiarylist: StateFlow<UiState<List<BeneficiaryData>>> = _beneficiarylist.asStateFlow()

    fun getBeneficiaryList() {
        viewModelScope.launch {
            localTransferRepository.getBeneficiaryList()
                .catch {
                    _beneficiarylist.value = UiState.Error(it)
                }
                .collect {
                    _beneficiarylist.value = it
                }
        }
    }

}