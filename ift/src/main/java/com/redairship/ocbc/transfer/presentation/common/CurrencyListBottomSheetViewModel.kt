//package com.ocbc.transfer.presentation.common
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.redairship.ocbc.transfer.LocalTransferRepositoryInterface
//import com.redairship.ocbc.transfer.UiState
//import com.redairship.ocbc.transfer.presentation.base.CurrencyCode
//import com.redairship.ocbc.transfer.presentation.base.CurrencyCodeEnum
//import kotlinx.coroutines.flow.*
//import kotlinx.coroutines.launch
//
//class CurrencyListBottomSheetViewModel (
//    private val localTransferRepository: LocalTransferRepositoryInterface
//) : ViewModel() {
//    var _currencytlist = MutableStateFlow<UiState<List<CurrencyCode>>>(UiState.Loading)
//    val currencytlist: StateFlow<UiState<List<CurrencyCode>>> = _currencytlist.asStateFlow()
//
//    fun getCurrencyList() {
//        viewModelScope.launch {
//            localTransferRepository.getCurrencyList()
//                .catch {
//                    _currencytlist.value = UiState.Error(it)
//                }
//                .collectLatest {
//                    var allList = it.data?.Currencies?.map { currency->
//                        val findcurrency = CurrencyCodeEnum.findCurrency(currency.entry)
//                        if (findcurrency.code.isEmpty()) {
//                            CurrencyCode(currency.entry, "", 999, "")
//                        } else {
//                            CurrencyCode(findcurrency.code, findcurrency.currencyName, currency.sequence, findcurrency.flag)
//                        }
//                    } ?: emptyList()
//                    _currencytlist.value = UiState.Success(allList)
//                }
//        }
//    }
//
//}