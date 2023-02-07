package com.redairship.ocbc.transfer.presentation.local

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.ocbc.transfer.LocalTransferConfig
import com.redairship.ocbc.transfer.LocalTransferData
import com.ocbc.transfer.R
import com.ocbc.transfer.databinding.TransferFragmentEntryBinding
import com.redairship.ocbc.transfer.model.AccountItemModel
import com.redairship.ocbc.transfer.model.BeneficiaryData
import com.redairship.ocbc.transfer.model.TransferToTypeResponse
import com.redairship.ocbc.transfer.presentation.base.BaseTransferFragment
import com.redairship.ocbc.transfer.presentation.base.TransferStatus
import com.redairship.ocbc.transfer.presentation.base.checkAlphanumericDashPattern
import com.redairship.ocbc.transfer.presentation.base.launchWhenStarted
import com.redairship.ocbc.transfer.presentation.common.AccountListBottomInterface
import com.redairship.ocbc.transfer.presentation.common.PayeeListBottomSheet
import com.ocbc.transfer.presentation.common.TransferToBottomInterface
import com.ocbc.transfer.presentation.local.TransferFromAccountListBottomSheet
import com.ocbc.transfer.presentation.local.TransferToBottomSheet
import com.redairship.ocbc.bb.components.models.ThinIcon
import com.redairship.ocbc.transfer.presentation.SameDayTransferDialog
import com.redairship.ocbc.transfer.presentation.common.UseMyRateFragment
import kotlinx.android.synthetic.main.dialog_bottom_sheet_another_ocbc_ac.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_short_content.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*

const val PRODUCT_CODE_InternalTransferOwnAcc = "ITOA"
const val PRODUCT_CODE_InternalTransferSameBankAcc = "ITSO"

class LocalTransferEntryFragment : BaseTransferFragment<TransferFragmentEntryBinding>(),
    AccountListBottomInterface, TransferToBottomInterface {
    val config by inject<LocalTransferConfig>()
    
    private var transferFromBottom: TransferFromAccountListBottomSheet? = null
    private var transferToBottom: TransferToBottomSheet? = null
    private var behaviorNextDialog: BottomSheetBehavior<*>? = null
    private var behaviorAnotherOcbcAc: BottomSheetBehavior<*>? = null

    override fun getContentLayoutId(): Int = R.layout.transfer_fragment_entry

    override fun onBindView() {
        with(dataBinding) {
            vStandardHeader.changeStartIcon(ThinIcon.ChevronLeftWhite)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.initialState()

        sharedViewModel.updateLocalTransferData(LocalTransferData())
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        sharedViewModel.getLocalTransferData().selectedDate = df.format(Date())

        transferFromBottom = TransferFromAccountListBottomSheet.newInstance(
            TransferStatus.TransferFrom,
            this@LocalTransferEntryFragment
        )

        transferToBottom = TransferToBottomSheet.newInstance(
            TransferStatus.TransferTo,
            this@LocalTransferEntryFragment
        )
    }

    override fun setViewData(transfertype: TransferStatus?) {
        launchWhenStarted {
            launch {
                delay(100)
//                if (sharedViewModel.getLocalTransferData()?.selectFromAc?.name.isNullOrEmpty())
//                    openTransferFromBottomView()
                sharedViewModel.useMyRate()
//                UseMyRateFragment.newInstance()
//                    .show(childFragmentManager, UseMyRateFragment::class.java.simpleName)
            }
        }
    }

    override fun setupViewEvents() {
        dataBinding.vStandardHeader.apply {
            showDivider = false
            showEndButton = false
            onStartButtonClicked = {
                activity?.finish()
            }
            hasShadow = true
        }
        dataBinding.transferfrom.apply {
            setOnClickListener {
                openTransferFromBottomView()
            }
        }
        dataBinding.transferto.apply {
            setOnClickListener {
                openTransferToBottomView()
            }
        }
    }

    override fun selectAccountItem(type: TransferStatus, item: AccountItemModel) {
        updateSelectAccountItem(type, item)
        when(type) {
            TransferStatus.TransferFrom -> {
//                dataBinding.transferfrom.title = "${item.accountNo} ${item.currency} ${item.name}}"
                dataBinding.transferto.visibility = View.VISIBLE

                viewLifecycleOwner.lifecycleScope.launch {
                    delay(1000)
                    if (sharedViewModel.getLocalTransferData()?.selectToAc?.name.isNullOrEmpty())
                        openTransferToBottomView()
                }
            }

            TransferStatus.TransferToMyAccounts -> {
//                dataBinding.transferto.title = "${item.accountNo} ${item.currency} ${item.name}}"
                openTransferToMyAcNextButtonBottomSheet()
            }
        }
    }

    override fun showErrorMessage(error: String?) {
        showGenericError(error, true)
    }

    override fun selectTransferTo(item: TransferToTypeResponse) {
//        dataBinding.transferto.title = item.itemName

        when(item.type) {
            TransferStatus.TransferToMyAccounts.name -> {
                sharedViewModel.updateProductCode(PRODUCT_CODE_InternalTransferOwnAcc)
                MyAccountListBottomSheet.newInstance(
                    TransferStatus.TransferToMyAccounts,
                    sharedViewModel.getLocalTransferData()?.selectFromAc,
                    this@LocalTransferEntryFragment
                )
                    .apply {
                        onBackClicked = {
                            openTransferToBottomView()
                        }
                    }.show(childFragmentManager, null)
            }
            TransferStatus.TransferToOcbcAccount.name -> {
                sharedViewModel.updateProductCode(PRODUCT_CODE_InternalTransferSameBankAcc)
                openAnotherOcbcBankAcBottomSheet()
            }
            TransferStatus.TransferToOtherBank.name -> {
                config.openLocalTransferActivity(requireActivity(), "Other Bank",
                    sharedViewModel.getLocalTransferData().ownAccountsList,
                    sharedViewModel.getLocalTransferData().selectFromAc)
            }
            TransferStatus.TransferToUEN.name -> {
                config.openLocalTransferActivity(requireActivity(), "Mobile / NRIC / UEN",
                    sharedViewModel.getLocalTransferData().ownAccountsList,
                    sharedViewModel.getLocalTransferData().selectFromAc
                )
            }
        }
    }

    private fun openTransferFromBottomView() {
        if (transferFromBottom?.isVisible == false) {
            transferFromBottom?.show(childFragmentManager, null)
        }
    }

    private fun openTransferToBottomView() {
        if (transferToBottom?.isVisible == false) {
            transferToBottom?.show(childFragmentManager, null)
        }
    }

    private fun checkAccountPayeeData() {
        if (sharedViewModel.getLocalTransferData().selectToAc?.accountNo.isNotEmpty() &&
            sharedViewModel.getLocalTransferData().selectToAc?.payeename.isNotEmpty()) {
            bottomsheet_another_ocbc_submit.isEnabled = true
        }
    }

    private fun openAnotherOcbcBankAcBottomSheet() {
        bottomsheet_another_ocbc_submit.isEnabled = false
        bottomsheet_another_ocbc_submit.apply {
            setOnClickListener {
                dialog_bottom_sheet_another_ocbc.isVisible = false
                bottom_sheet_behavior_id.isVisible = false
                sharedViewModel.getLocalTransferData().selectToAc.currency = "SGD"
//                findNavController().navigate(LocalTransferEntryFragmentDirections.actiontodetail())
            }
        }
        tf_bottomsheet_another_ocbc_ac_number.addTextChangeListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                val text = checkAlphanumericDashPattern(editable.toString())
                if (text) {
                    tf_bottomsheet_another_ocbc_ac_number.isError = false
                    sharedViewModel.getLocalTransferData().selectToAc.accountNo = editable.toString()
                    checkAccountPayeeData()
                } else {
                    tf_bottomsheet_another_ocbc_ac_number.isError = true
                }
            }
        })

        tf_bottomsheet_another_ocbc_payee_name.addTextChangeListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                sharedViewModel.getLocalTransferData().selectToAc.payeename = editable.toString()
                checkAccountPayeeData()
            }
        })

        tf_bottomsheet_another_ocbc_payee_list.setOnClickListener {
            PayeeListBottomSheet().apply {
                onSelectedPayeeClicked = { data ->
                    updateBeneficialData(data)
                }
            }.show(childFragmentManager, "")
        }

        dialog_bottom_sheet_another_ocbc.isVisible = true
        bottom_sheet_behavior_id.isVisible = false
    }

    private fun updateBeneficialData(data: BeneficiaryData) {
        tf_bottomsheet_another_ocbc_ac_number.text = data.payDetail.accountNo ?: ""
        tf_bottomsheet_another_ocbc_payee_name.text = data.name
    }

    private fun openTransferToMyAcNextButtonBottomSheet() {
        bottomsheet_title.text = getString(R.string.account_details)
        bottomsheet_desc.text = sharedViewModel.getLocalTransferData().selectToAc.name
        bottomsheet_submit.setOnClickListener {
//            findNavController().navigate(LocalTransferEntryFragmentDirections.actiontodetail())
        }
//        behaviorNextDialog?.setState(BottomSheetBehavior.STATE_EXPANDED)
        bottom_sheet_behavior_id.isVisible = true
        dialog_bottom_sheet_another_ocbc.isVisible = false
    }
}