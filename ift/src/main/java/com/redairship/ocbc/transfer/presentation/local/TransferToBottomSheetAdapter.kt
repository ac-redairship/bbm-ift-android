package com.ocbc.transfer.presentation.local

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.redairship.ocbc.transfer.model.TransferToTypeResponse
import com.ocbc.transfer.R
import com.ocbc.transfer.presentation.common.TransferToBottomInterface
import com.redairship.ocbc.transfer.presentation.base.TransferStatus

class TransferToBottomSheetAdapter(
    private val items: ArrayList<TransferToTypeResponse>,
    private val bottomInterface: TransferToBottomInterface
) :
    RecyclerView.Adapter<TransferToBottomSheetAdapter.TransferToViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): TransferToViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.accountlist_transferto_item, parent, false)
        return TransferToViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: TransferToViewHolder, position: Int) {
        holder.bind(items[position])

        holder.itemView.setOnClickListener {
            bottomInterface.selectTransferTo(items[position])
        }
    }

    class TransferToViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: TransferToTypeResponse) {
            itemView.findViewById<TextView>(R.id.title).text = item?.itemName

            var imageView = itemView.findViewById<ImageView>(R.id.close_arrow)
            when(item.type) {
                TransferStatus.TransferToUEN.name -> {
                    imageView.setImageResource(R.drawable.icon_paynow)
                }
                TransferStatus.TransferToMyAccounts.name -> {
                    imageView.setImageResource(R.drawable.ras_components_ic_bb_chevron_right)
                }
                else -> {
                    imageView.visibility = View.GONE
                }
            }
        }
    }
}