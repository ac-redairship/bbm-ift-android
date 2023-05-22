package com.redairship.ocbc.transfer.presentation.transfer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ocbc.transfer.R
import com.ocbc.transfer.databinding.TransferFragmentEntryBinding
import com.redairship.ocbc.bb.components.models.ThickIcon
import com.redairship.ocbc.bb.components.models.ThinIcon
import com.redairship.ocbc.transfer.LocalTransferData
import com.redairship.ocbc.transfer.model.AccountItemModel
import com.redairship.ocbc.transfer.model.TransferToTypeResponse
import com.redairship.ocbc.transfer.presentation.base.BaseTransferFragment
import com.redairship.ocbc.transfer.presentation.base.TransferStatus
import com.redairship.ocbc.transfer.presentation.common.AccountListBottomInterface
import com.redairship.ocbc.transfer.presentation.common.TransferToBottomInterface
import com.redairship.ocbc.transfer.presentation.local.MyAccountListBottomSheet
import com.redairship.ocbc.transfer.presentation.transfer.transferfrom.TransferFromAccountListBottomSheet
import com.redairship.ocbc.transfer.presentation.transfer.transferto.TransferToAnotherOcbcAccountBottomSheet
import com.redairship.ocbc.transfer.presentation.transfer.transferto.TransferToMethodsBottomSheet
import kotlinx.android.synthetic.main.dialog_bottom_sheet_short_content.bottomsheet_desc
import kotlinx.android.synthetic.main.dialog_bottom_sheet_short_content.bottomsheet_submit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

const val PRODUCT_CODE_InternalTransferOwnAcc = "ITOA"
const val PRODUCT_CODE_InternalTransferSameBankAcc = "ITSO"

class TransferEntryFragment : BaseTransferFragment<TransferFragmentEntryBinding>(),
    AccountListBottomInterface, TransferToBottomInterface {

    private lateinit var transferFromBottom: TransferFromAccountListBottomSheet
    private lateinit var transferToBottom: TransferToMethodsBottomSheet
    private val transferToAnotherOcbcAccountBottomSheet by lazy { TransferToAnotherOcbcAccountBottomSheet() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = TransferFragmentEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onBindView() {
        with(binding) {
            vStandardHeader.changeStartIcon(ThinIcon.ChevronLeftWhite)
            btTransferFrom.endIcon = ThickIcon.ChevronDown
            btTransferTo.endIcon = ThickIcon.ChevronDown
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.initialState()

        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        sharedViewModel.updateLocalTransferData(LocalTransferData(selectedDate = df.format(Date())))

        transferFromBottom = TransferFromAccountListBottomSheet.newInstance(
            TransferStatus.TransferFrom,
            this@TransferEntryFragment
        )

        transferToBottom = TransferToMethodsBottomSheet.newInstance(
            TransferStatus.TransferTo,
            this@TransferEntryFragment
        )
    }

    override fun setupViewEvents() {
        binding.vStandardHeader.apply {
            showDivider = false
            showEndButton = false
            onStartButtonClicked = { activity?.finish() }
            hasShadow = true
        }

        binding.btTransferFrom.setOnClickListener { openTransferFromBottomView() }
        binding.btTransferTo.setOnClickListener { openTransferToBottomView() }
    }

    override fun setViewData(transfertype: TransferStatus?) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            delay(100)
            if (sharedViewModel.localTransferData.value?.selectFromAc?.accountName.isNullOrEmpty())
                openTransferFromBottomView()
        }
    }

    override fun selectAccountItem(type: TransferStatus, item: AccountItemModel) {
        updateSelectAccountItem(type, item)
        when(type) {
            TransferStatus.TransferFrom -> updateTransferFromUI(item)
            TransferStatus.TransferToMyAccounts -> updateTransferToMyAccountsUI(item)
        }
    }

    private fun updateTransferFromUI(item: AccountItemModel) {
        binding.btTransferFrom.label = "${item.accountNumber} ${item.currency} ${item.accountName}"
        binding.btTransferTo.isVisible = true

        viewLifecycleOwner.lifecycleScope.launch {
            delay(1000)
            if (sharedViewModel.localTransferData.value?.selectToAc?.accountName.isNullOrEmpty())
                openTransferToBottomView()
        }
    }

    private fun updateTransferToMyAccountsUI(item: AccountItemModel) {
        binding.btTransferTo.label = "${item.accountNumber} ${item.currency} ${item.accountName}"
        openTransferToMyAcNextButtonBottomSheet()
    }

    override fun showErrorMessage(error: String?) {
        showGenericError(error, true)
    }

    override fun selectTransferTo(item: TransferToTypeResponse) {
        binding.btTransferTo.label = item.itemName
        val selectFromAc = sharedViewModel.localTransferData.value?.selectFromAc

        when(item.type) {
            TransferStatus.TransferToMyAccounts.name -> {
                selectFromAc ?: return
                sharedViewModel.updateProductCode(PRODUCT_CODE_InternalTransferOwnAcc)
                MyAccountListBottomSheet.newInstance(
                    TransferStatus.TransferToMyAccounts,
                    selectFromAc,
                    this@TransferEntryFragment
                )
                    .apply {
                        onBackClicked = { openTransferToBottomView() }
                    }.show(childFragmentManager, null)
            }
            TransferStatus.TransferToOcbcAccount.name -> {
                sharedViewModel.updateProductCode(PRODUCT_CODE_InternalTransferSameBankAcc)
                openAnotherOcbcBankAcBottomSheet()
            }
            TransferStatus.TransferToOtherBank.name -> {
                selectFromAc ?: return
                // go to existing "other bank" function
            }
            TransferStatus.TransferToUEN.name -> {
                selectFromAc ?: return
                // go to existing "Mobile / NRIC / UEN" function
            }
        }
    }

    private fun openTransferFromBottomView() {
        if (!transferFromBottom.isVisible) {
            transferFromBottom.show(childFragmentManager, null)
        }
    }

    private fun openTransferToBottomView() {
        if (!transferToBottom.isVisible) {
            transferToBottom.show(childFragmentManager, null)
        }
    }

    private fun openTransferToMyAcNextButtonBottomSheet() = with(binding.vBottomSheetContent) {
        bottomsheetTitle.text = getString(R.string.account_details)
        bottomsheet_desc.text = localTransferData.selectToAc.accountName
        bottomsheet_submit.setOnClickListener {
            findNavController().navigate(TransferEntryFragmentDirections.actiontodetail())
        }
        bottomSheetBehaviorId.isVisible = true
    }


    private fun openAnotherOcbcBankAcBottomSheet() {
        transferToAnotherOcbcAccountBottomSheet.show(childFragmentManager, "TransferToAnotherOcbcAcBottomSheet")
        transferToAnotherOcbcAccountBottomSheet.onSelectNextClicked = {
            try {
                transferToAnotherOcbcAccountBottomSheet.binding.dialogBottomSheetAnotherOcbc.isVisible = false
                binding.vBottomSheetContent.bottomSheetBehaviorId.isVisible = false
            } catch (e: Exception) {
                Log.e("EntryFragment", e.message, e)
            }
            try {
                sharedViewModel.updateLocalTransferData(localTransferData.copy(selectToAc = localTransferData.selectToAc.copy(currency = Currency.getInstance("SGD"))))
            } catch (e: Exception) {
                Log.e("EntryFragment", e.message, e)
            }

            try {
                findNavController().navigate(TransferEntryFragmentDirections.actiontodetail())
            } catch (e: Exception) {
                Log.e("EntryFragment", e.message, e)
            }
        }
    }

}
