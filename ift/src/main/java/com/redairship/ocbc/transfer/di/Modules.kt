package com.redairship.ocbc.transfer.di

import com.ocbc.transfer.databinding.CurrencyListBottomSheetBinding
import com.redairship.ocbc.transfer.presentation.common.AccountListBottomSheetViewModel
import com.redairship.ocbc.transfer.presentation.common.PayeeListBottomSheetViewModel
import com.redairship.ocbc.transfer.presentation.local.LocalTransferViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val iftModules = module {
    viewModel { LocalTransferViewModel(get()) }
    viewModel { AccountListBottomSheetViewModel(get()) }
    viewModel { PayeeListBottomSheetViewModel(get()) }
}
