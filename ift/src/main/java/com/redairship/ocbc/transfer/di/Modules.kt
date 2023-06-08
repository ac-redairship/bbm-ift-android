package com.redairship.ocbc.transfer.di

import com.redairship.ocbc.onetokenoffline.offlinemode.OneTokenPinViewModel
import com.redairship.ocbc.transfer.presentation.common.AccountListBottomSheetViewModel
import com.redairship.ocbc.transfer.presentation.common.CurrencyListBottomSheetViewModel
import com.redairship.ocbc.transfer.presentation.transfer.transferto.PayeeListBottomSheetViewModel
import com.redairship.ocbc.transfer.presentation.local.LocalTransferViewModel
import com.redairship.ocbc.transfer.presentation.transfer.local.otp.OTPViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val iftModules = module {
    viewModel { LocalTransferViewModel(get()) }
    viewModel { AccountListBottomSheetViewModel(get()) }
    viewModel { PayeeListBottomSheetViewModel(get()) }
    viewModel { CurrencyListBottomSheetViewModel(get()) }
    viewModel { OTPViewModel(get()) }
    viewModel { OneTokenPinViewModel(get()) }
}
