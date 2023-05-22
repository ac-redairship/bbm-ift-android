package com.redairship.ocbc.transfer.presentation

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ocbc.transfer.databinding.DialogSameDayTransferBinding
import com.redairship.ocbc.bb.components.views.bottomsheet.BBBottomSheet

class SameDayTransferDialog(val listener: ResponseListener) : BBBottomSheet() {

    private lateinit var binding: DialogSameDayTransferBinding

    override fun getContentView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogSameDayTransferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerViewEvents()
    }

    private fun registerViewEvents() {
        binding.btCancel.setOnClickListener {
            listener.response(SameDayTransferResult.Cancel)
            dismiss()
        }
        binding.btProceed.setOnClickListener {
            listener.response(SameDayTransferResult.Proceed)
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { thisDialog ->
            val d = thisDialog as BottomSheetDialog
            val bottomSheet =
                d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            BottomSheetBehavior.from<FrameLayout?>(bottomSheet).state =
                BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    private fun setupViews() {
    }

    interface ResponseListener {
        fun dismiss()
        fun response(result: SameDayTransferResult)
    }

    companion object {
        fun newInstance(listener: ResponseListener) = SameDayTransferDialog(listener)
    }

    sealed class SameDayTransferResult {
        object Proceed : SameDayTransferResult()
        object Cancel : SameDayTransferResult()
    }
}
