package com.redairship.ocbc.transfer.presentation.transfer.transferto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ocbc.transfer.R
import com.redairship.ocbc.transfer.UiState
import com.ocbc.transfer.databinding.TransferToBottomSheetBinding
import com.redairship.ocbc.transfer.model.TransferToTypeResponse
import com.redairship.ocbc.transfer.presentation.base.TransferStatus
import com.redairship.ocbc.transfer.presentation.base.launchWhenCreated
import com.redairship.ocbc.transfer.presentation.common.TransferToBottomInterface
import com.redairship.ocbc.bb.components.views.bottomsheet.BBBottomSheet
import com.redairship.ocbc.transfer.presentation.local.LocalTransferViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class TransferToMethodsBottomSheet(private val type: TransferStatus,
                                   private val bottomInterface: TransferToBottomInterface
): BBBottomSheet(),
    TransferToBottomInterface {
    private lateinit var binding: TransferToBottomSheetBinding
    val sharedViewModel: LocalTransferViewModel by activityViewModels()
    var listAdapter : TransferToMethodsBottomSheetAdapter? = null

    override fun getContentView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TransferToBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when(type) {
            TransferStatus.TransferTo -> {
                binding.textTitle.text = getString(R.string.transfer_to_lowercase)
                sharedViewModel.getTransferToTypeList()
            }
        }
        observeViewModelResponse()
    }

    private fun observeViewModelResponse(){
        launchWhenCreated {
            launch {
                sharedViewModel.transferToTypeList
                    .onEach {
//                        binding.progress.isVisible = it is UiState.Loading
                    }
                    .collectLatest {
//                        showError(it)
                        if (it is UiState.Success) {
                            populateAdapterWithInfo(it.data)
                        }
                    }
            }
        }
    }

    private fun populateAdapterWithInfo(list : List<TransferToTypeResponse>){
        listAdapter =
            activity?.let { TransferToMethodsBottomSheetAdapter(ArrayList(list), this@TransferToMethodsBottomSheet) }
        listAdapter?.let {
            val layoutManager = LinearLayoutManager(context)
            binding.listRv.layoutManager = layoutManager
            binding.listRv.adapter = it
            it.notifyDataSetChanged()
        }
    }

    companion object {
        fun newInstance(type: TransferStatus, bottomInterface: TransferToBottomInterface)
        = TransferToMethodsBottomSheet(type, bottomInterface)
    }

    override fun selectTransferTo(type: TransferToTypeResponse) {
        dismiss()
        bottomInterface.selectTransferTo(type)
    }

}