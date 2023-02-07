package com.redairship.ocbc.transfer.presentation.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.redairship.ocbc.transfer.UiState
import com.redairship.ocbc.transfer.model.AccountItemListModel
import com.redairship.ocbc.transfer.model.AccountItemModel
import com.redairship.ocbc.transfer.presentation.base.TransferStatus
import com.redairship.ocbc.transfer.presentation.base.launchWhenCreated
import com.redairship.ocbc.bb.components.views.bottomsheet.BBBottomSheet
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import com.ocbc.transfer.databinding.TransferBottomSheetAccountListBinding

abstract class AccountListBottomSheet(
    val type: TransferStatus,
    val listInterface: AccountListBottomInterface
) : BBBottomSheet(),
    ExpandableRecyclerInterface<AccountItemModel, AccountItemModel> {

    var accountlistAdapter : AccountListExpandableAdapter? = null
    lateinit var binding: TransferBottomSheetAccountListBinding
    val viewModel: AccountListBottomSheetViewModel by sharedViewModel()

    override fun getContentView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TransferBottomSheetAccountListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModelResponse()
    }

    fun observeViewModelResponse(){
        launchWhenCreated {
            launch {
                viewModel.accountlist
                    .collectLatest {
                        if (it is UiState.Success) {
                            populateAdapterWithInfo(it.data)
                        } else if (it is UiState.Error) {
                            dismiss()
                            listInterface.showErrorMessage(it.throwable.message)
                        }
                    }
            }
        }
    }

    abstract fun populateAdapterWithInfo(expandableList : AccountItemListModel)

    override fun selectParentItem(account: AccountItemModel) {
        dismiss()
        listInterface.selectAccountItem(type, account)
    }

    override fun selectChildItem(parentAccount: AccountItemModel, childAccount: AccountItemModel) {
        dismiss()
        listInterface.selectAccountItem(type, childAccount)
    }
}