package com.redairship.ocbc.transfer.presentation.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ocbc.transfer.R
import com.redairship.ocbc.transfer.model.BeneficiaryData
import kotlinx.android.synthetic.main.payee_list_item.view.*

class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    internal var layout = v.item_container
    internal var itemName : TextView = v.item_name
    internal var itemDesp = v.item_desp
}

class PayeeListAdapter(val list: List<BeneficiaryData>,
                       val listInterface: PayeeListBottomInterface
): RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.payee_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.itemName.text = item.name
        holder.itemDesp.text = item.payDetail.accountNo +"\"" + item.payType
        holder.layout.setOnClickListener {
            listInterface.selectItem(item)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}