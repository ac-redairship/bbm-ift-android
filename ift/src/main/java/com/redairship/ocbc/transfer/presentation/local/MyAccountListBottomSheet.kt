package com.redairship.ocbc.transfer.presentation.local

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.redairship.ocbc.transfer.model.AccountItemListModel
import com.redairship.ocbc.transfer.model.AccountItemModel
import com.redairship.ocbc.transfer.presentation.base.TransferStatus
import com.redairship.ocbc.transfer.presentation.common.AccountListBottomInterface
import com.redairship.ocbc.transfer.presentation.common.AccountListBottomSheet
import com.redairship.ocbc.transfer.presentation.common.AccountListExpandableAdapter


class MyAccountListBottomSheet(type: TransferStatus, listInterface: AccountListBottomInterface) :
    AccountListBottomSheet(type, listInterface){
    private var selectedAccount: AccountItemModel? = null

    var onBackClicked: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedAccount = it.getSerializable(SELECTED_ACCOUNT) as AccountItemModel?
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when(type) {
            TransferStatus.TransferToMyAccounts -> {
                binding.iconLeft.isVisible = true
                binding.textTitle.text = "My accounts"
                binding.showBalance.isVisible = false

                viewModel.getAccountList("ITSO")
                binding.iconLeft.setOnClickListener {
                    onBackClicked?.invoke()
                    this@MyAccountListBottomSheet.dismiss()
                }
            }
        }
    }

    override fun populateAdapterWithInfo(expandableList: AccountItemListModel) {
        var newList = expandableList.accountList.filter {
            it.id != selectedAccount?.id
        }
        accountlistAdapter =
            activity?.let { AccountListExpandableAdapter(ArrayList(newList), this@MyAccountListBottomSheet) }

        accountlistAdapter?.let {
            it.setExpanded(false)
            val layoutManager = LinearLayoutManager(context)
            binding.accountListRv.layoutManager = layoutManager
            binding.accountListRv.adapter = it
//            binding.accountListRv.addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
            it.notifyDataSetChanged()
        }
    }

    companion object {
        val SELECTED_ACCOUNT = "selectedaccount"

        fun newInstance(type: TransferStatus, selectedItem: AccountItemModel, listinterface: AccountListBottomInterface)
        = MyAccountListBottomSheet(type, listinterface).apply {
            arguments = Bundle().apply {
                putSerializable(SELECTED_ACCOUNT, selectedItem)
            }
        }
    }

}