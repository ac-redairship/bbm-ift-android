package com.ocbc.transfer.presentation.local

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.ocbc.transfer.databinding.DialogBottomSheetAnotherOcbcAcBinding
import com.redairship.ocbc.transfer.presentation.base.checkAlphanumericDashPattern
import com.redairship.ocbc.bb.components.views.bottomsheet.BBBottomSheet
import com.redairship.ocbc.transfer.presentation.local.LocalTransferViewModel

class TransferToAnotherOcbcAcBottomSheet() : BBBottomSheet() {
    var onSelectNextClicked: (() -> Unit)? = null
    val sharedViewModel: LocalTransferViewModel by activityViewModels()
    lateinit var binding: DialogBottomSheetAnotherOcbcAcBinding

    override fun getContentView(inflater: LayoutInflater, container: ViewGroup?,
                                savedInstanceState: Bundle?): View {
        binding = DialogBottomSheetAnotherOcbcAcBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bottomsheetAnotherOcbcSubmit.isEnabled = false
        binding.bottomsheetAnotherOcbcSubmit.apply {
            setOnClickListener {
                dismiss()
                sharedViewModel.getLocalTransferData().selectToAc.currency = "SGD"
                onSelectNextClicked?.invoke()
            }
        }

        binding.tfBottomsheetAnotherOcbcAcNumber.addTextChangeListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                val text = checkAlphanumericDashPattern(editable.toString())
                if (text) {
                    binding.tfBottomsheetAnotherOcbcAcNumber.isError = false
                    sharedViewModel.getLocalTransferData().selectToAc.accountNo = editable.toString()
                    checkAccountPayeeData()
                } else {
                    binding.tfBottomsheetAnotherOcbcAcNumber.isError = true
                }
            }
        })

        binding.tfBottomsheetAnotherOcbcPayeeName.addTextChangeListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                sharedViewModel.getLocalTransferData().selectToAc.payeename = editable.toString()
                checkAccountPayeeData()
            }
        })
    }

    private fun checkAccountPayeeData() {
        if (sharedViewModel.getLocalTransferData().selectToAc.accountNo.isNotEmpty() &&
            sharedViewModel.getLocalTransferData().selectToAc.payeename.isNotEmpty()) {
            binding.bottomsheetAnotherOcbcSubmit.isEnabled = true
        }
    }

}