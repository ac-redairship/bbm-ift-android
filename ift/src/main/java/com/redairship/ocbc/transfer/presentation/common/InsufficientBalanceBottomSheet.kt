package com.redairship.ocbc.transfer.presentation.common

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ocbc.transfer.R
import com.ocbc.transfer.databinding.DialogBottomSheetInsufficientBalanceBinding
import com.redairship.ocbc.bb.components.views.bottomsheet.BBBottomSheet

class InsufficientBalanceBottomSheet : BBBottomSheet() {
    var onChangeAccountClicked: (() -> Unit)? = null

    lateinit var binding: DialogBottomSheetInsufficientBalanceBinding

    override fun getContentView(inflater: LayoutInflater, container: ViewGroup?,
                                savedInstanceState: Bundle?): View {
        binding = DialogBottomSheetInsufficientBalanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvTitle.text = getString(R.string.insufficient_balance)
        binding.btOkay.setOnClickListener {
            dismiss()
        }
        binding.btChangeaccount.setOnClickListener {
            onChangeAccountClicked?.invoke()
        }
    }


}