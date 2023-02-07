package com.redairship.ocbc.transfer.presentation.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.redairship.ocbc.transfer.presentation.base.launchWhenCreated
import com.ocbc.transfer.databinding.ProcessingPaymentBottomSheetBinding
import com.ocbc.transfer.NetworkException
import com.ocbc.transfer.UnexpectedException
import com.redairship.ocbc.transfer.UiState
import com.redairship.ocbc.transfer.model.TransactionPreSubmitType
import com.redairship.ocbc.transfer.presentation.base.TransferStatus
import com.redairship.ocbc.transfer.presentation.local.LocalTransferViewModel
import com.ocbc.transfer.presentation.local.TransferActivity
import com.redairship.ocbc.bb.components.views.bottomsheet.BBBottomSheet
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ProcessingBottomSheet(
    private val type: TransferStatus?): BBBottomSheet() {
    var onProcessingFinish: (() -> Unit)? = null

    private lateinit var binding:ProcessingPaymentBottomSheetBinding
    private val viewModel: LocalTransferViewModel by sharedViewModel()

    override fun getContentView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ProcessingPaymentBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.doPreSubmit(TransactionPreSubmitType.SUBMIT)
        observeViewModelResponse()
    }

    private fun observeViewModelResponse(){
        launchWhenCreated {
            launch {
                viewModel.insufficientBalanceOutcome
                    .collectLatest {
                        when (it) {
                            is UiState.Loading -> {
                            }
                            is UiState.Success -> {
                                onProcessingFinish?.invoke()
                            }
                            is UiState.Error -> {
                                when(it.throwable) {
                                    is UnexpectedException,
                                    is NetworkException -> {
                                        (activity as TransferActivity).showGenericServerErrorScreen(it.throwable.message)
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }

    companion object {
        fun newInstance(type: TransferStatus?) = ProcessingBottomSheet(type)
    }

}