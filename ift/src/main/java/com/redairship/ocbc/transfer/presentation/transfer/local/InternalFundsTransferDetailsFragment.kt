package com.redairship.ocbc.transfer.presentation.transfer.local

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.ocbc.transfer.R
import com.ocbc.transfer.databinding.DialogBottomSheetAnotherOcbcAcBinding
import com.redairship.ocbc.bb.components.views.bottomsheet.BBBottomSheet
import com.redairship.ocbc.transfer.LocalTransferData
import com.redairship.ocbc.transfer.model.BeneficiaryData
import com.redairship.ocbc.transfer.presentation.local.LocalTransferViewModel
import com.redairship.ocbc.transfer.presentation.transfer.transferto.PayeeListBottomSheet
import kotlinx.android.synthetic.main.dialog_bottom_sheet_another_ocbc_ac.*
import java.util.*

class InternalFundsTransferDetailsFragment() : BBBottomSheet(R.style.ras_components_BBBottomSheetDialogThemeNoBackground) {
    private var isFromPayeeList: Boolean = false
    var onSelectNextClicked: (() -> Unit)? = null
    val sharedViewModel: LocalTransferViewModel by activityViewModels()
    lateinit var binding: DialogBottomSheetAnotherOcbcAcBinding
    private var selectedPayee: BeneficiaryData? = null
    private var localTransferData: LocalTransferData = sharedViewModel.localTransferData.value!!

    override fun getContentView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogBottomSheetAnotherOcbcAcBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        binding.bottomsheetAnotherOcbcSubmit.isEnabled = false

        sharedViewModel.localTransferData.observe(viewLifecycleOwner) {
            localTransferData = it
        }

        binding.bottomsheetAnotherOcbcSubmit.apply {
            setOnClickListener {
                dismiss()
                sharedViewModel.localTransferData.value?.let { localTransferData ->
                    sharedViewModel.updateLocalTransferData(
                        localTransferData.copy(
                            recipientAccountData = localTransferData.recipientAccountData.copy(
                                currency = Currency.getInstance("SGD")
                            )
                        )
                    )
                }

                onSelectNextClicked?.invoke()
            }
        }
        binding.tfBottomsheetAnotherOcbcPayeeList.setOnClickListener {
            PayeeListBottomSheet(selectedPayee).apply {
                onSelectedPayeeClicked = { data ->
                    this@InternalFundsTransferDetailsFragment.selectedPayee = data
                    updateBeneficialData(data)
                }
            }.show(childFragmentManager, "PayeeListBottomSheet")
        }
        binding.tfBottomsheetAnotherOcbcAcNumber.setInputType(InputType.TYPE_CLASS_NUMBER)
        binding.tfBottomsheetAnotherOcbcAcNumber.addTextChangeListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(toAccount: Editable) {
                when {
                    (toAccount.startsWith("988") && toAccount.length < 10) ||
                            ((toAccount.startsWith("888") || toAccount.startsWith("889")) && toAccount.length < 10) ||
                            (!toAccount.startsWith("988") && !toAccount.startsWith("888") && !toAccount.startsWith(
                                "889"
                            ) && (toAccount.length < 10 || toAccount.length == 11 || toAccount.length > 13)) -> {
                        binding.tfBottomsheetAnotherOcbcAcNumber.isError = false
                        sharedViewModel.updateLocalTransferData(
                            localTransferData.copy(
                                recipientAccountData = localTransferData.recipientAccountData.copy(
                                    accountNumber = toAccount.toString()
                                )
                            )
                        )
                        checkAccountPayeeData()
                    }
                    toAccount.toString() == localTransferData.senderAccountData.accountNumber -> {
                        binding.tfBottomsheetAnotherOcbcAcNumber.errorText =
                            "Beneficiary account number cannot be the same as the debit account number"
                        binding.tfBottomsheetAnotherOcbcAcNumber.isError = true
                    }
                    else -> {
                        binding.tfBottomsheetAnotherOcbcAcNumber.errorText = "Invalid account"
                        binding.tfBottomsheetAnotherOcbcAcNumber.isError = true
                    }
                }

                if (!isFromPayeeList) {
                    v_add_to_payee_list.isVisible = true
                }

            }
        })

        binding.tfBottomsheetAnotherOcbcPayeeName.binding.etInput.filters = arrayOf<InputFilter>(
            InputFilter.LengthFilter(35)
        )
        binding.tfBottomsheetAnotherOcbcPayeeName.addTextChangeListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                if (isNameValid(editable.toString())) {
                    binding.tfBottomsheetAnotherOcbcPayeeName.isError = false
                    sharedViewModel.updateLocalTransferData(
                        localTransferData.copy(
                            recipientAccountData = localTransferData.recipientAccountData.copy(
                                bankName = editable.toString()
                            )
                        )
                    )
                    checkAccountPayeeData()
                } else {
                    binding.tfBottomsheetAnotherOcbcPayeeName.isError = true
                }

            }
        })
    }

    private fun updateBeneficialData(data: BeneficiaryData) {
        isFromPayeeList = true
        v_add_to_payee_list.isVisible = false
        tf_bottomsheet_another_ocbc_ac_number.text = data.payDetail.accountNo ?: ""
        tf_bottomsheet_another_ocbc_payee_name.text = data.name
        isFromPayeeList = false
    }


    fun isNameValid(input: String): Boolean {
        val invalidChars = input.filter { !it.isWhitespace() && !it.isLetterOrDigit() && it !in "*':;!\\?~%" }
        return invalidChars.isEmpty()
    }

    private fun checkAccountPayeeData() {
        val recipientAccountData = sharedViewModel.localTransferData.value?.recipientAccountData ?: return
        if (recipientAccountData.accountNumber.isNotEmpty() && recipientAccountData.bankName.isNotEmpty()) {
            binding.bottomsheetAnotherOcbcSubmit.isEnabled = true
        }
    }

}