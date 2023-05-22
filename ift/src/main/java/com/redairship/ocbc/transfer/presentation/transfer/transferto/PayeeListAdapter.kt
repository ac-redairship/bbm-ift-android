package com.redairship.ocbc.transfer.presentation.localtransfer.transferto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ocbc.transfer.R
import com.redairship.ocbc.transfer.model.BeneficiaryData
import com.redairship.ocbc.transfer.presentation.transfer.transferto.PayeeListBottomInterface
import kotlinx.android.synthetic.main.payee_list_item.view.*

class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    internal var layout = v.item_container
    internal var itemName = v.tv_name
    internal var itemBank = v.tv_bank
    internal var itemAccountNumber = v.tv_account_number
    internal var itemOverseas = v.tv_overseas
    internal var itemCountry = v.tv_country
    internal var itemSelected = v.v_check
}

class PayeeListAdapter(
    val listInterface: PayeeListBottomInterface
) : ListAdapter<BeneficiaryData, ViewHolder>(
    COMPARATOR
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.payee_list_item, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.itemName.text = item.name
        holder.itemBank.text = item.payType
        holder.itemAccountNumber.text = item.payDetail.accountNo
        holder.itemOverseas.isVisible = !item.isLocalBank
        holder.itemCountry.isVisible = !item.isLocalBank
        holder.itemSelected.isVisible = item.isSelected
        holder.itemCountry.text = item.bankDetailDTO.beneficiaryBankCountryCode
        holder.layout.setOnClickListener {
            listInterface.selectItem(item)
        }
    }

    companion object {
        val COMPARATOR = object : DiffUtil.ItemCallback<BeneficiaryData>() {
            override fun areItemsTheSame(
                oldItem: BeneficiaryData,
                newItem: BeneficiaryData
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: BeneficiaryData,
                newItem: BeneficiaryData
            ): Boolean = oldItem == newItem

        }
    }

}