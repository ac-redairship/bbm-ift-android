package com.ocbc.transfer.presentation.local

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.redairship.ocbc.transfer.presentation.base.TransferStatus
import com.ocbc.transfer.R
import com.redairship.ocbc.transfer.model.AccountItemListModel
import com.redairship.ocbc.transfer.presentation.common.AccountListBottomInterface
import com.redairship.ocbc.transfer.presentation.common.AccountListBottomSheet
import com.redairship.ocbc.transfer.presentation.common.AccountListExpandableAdapter

class TransferFromAccountListBottomSheet(type: TransferStatus, listInterface: AccountListBottomInterface):
    AccountListBottomSheet(type, listInterface) {

    override fun populateAdapterWithInfo(expandableList: AccountItemListModel) {
        accountlistAdapter =
            activity?.let { AccountListExpandableAdapter(ArrayList(expandableList.accountList),
                this@TransferFromAccountListBottomSheet) }
        accountlistAdapter?.let {
            it.setExpanded(false)
            val layoutManager = LinearLayoutManager(context)
            binding.accountListRv.layoutManager = layoutManager
            binding.accountListRv.adapter = it
            it.notifyDataSetChanged()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when(type) {
            TransferStatus.TransferFrom -> {
                binding.textTitle.text = getString(R.string.transfer_from)
                viewModel.getAccountList("ITSO")
            }
        }

        binding.showBalance.apply {
            setOnClickListener{
                accountlistAdapter?.let { adapter ->
                    adapter.hideBalance = !adapter.hideBalance
                    binding.showBalance.text =
                        if(adapter.hideBalance) getString(R.string.show_balance) else getString(R.string.hide_balance)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    companion object {
        fun newInstance(type: TransferStatus, listinterface: AccountListBottomInterface)
        = TransferFromAccountListBottomSheet(type, listinterface)
    }
}