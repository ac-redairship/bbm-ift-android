//package com.ocbc.transfer.presentation.common
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Filter
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.ocbc.components.grouprecycler.GroupRecyclerViewAdapter
//import com.ocbc.components.grouprecycler.HolderType
//import com.ocbc.components.grouprecycler.OnGroupRecyclerViewAdapterListener
//import com.redairship.ocbc.transfer.presentation.base.CurrencyCode
//import com.redairship.ocbc.transfer.presentation.base.getFlagResouce
//import com.ocbc.transfer.R
//import kotlinx.android.synthetic.main.accountlist_child_item.view.*
//import kotlinx.android.synthetic.main.group_header_item.view.*
//
//class CurrencyListAdapter(selectedCurrency: String?,
//                          popularList:List<CurrencyCode>,
//                          allList: List<CurrencyCode>,
//                          val mListener: OnGroupRecyclerViewAdapterListener<CurrencyCode>
//)
//    : GroupRecyclerViewAdapter<CurrencyCode, CurrencyCode, CurrencyListAdapter.HViewHolder, CurrencyListAdapter.CViewHolder>
//    (popularList, allList) {
//
//    override fun getItemViewType(position: Int): Int {
//        if (searchList.isEmpty()) {
//            // Header for 'Most popular countries'
//            if (position == 0) {
//                return HolderType.Header.ordinal
//            }
//            // Header for 'All' countries
//            if (position == popularList.size + 1) {
//                return HolderType.Header.ordinal
//            }
//        }
//        return HolderType.Item.ordinal
//    }
//
//    class HViewHolder(v: View) : GroupRecyclerViewAdapter.HeadViewHolder(v) {
//        internal var layout = v.header_lly
//        internal var title : TextView = v.header_title
//    }
//
//    class CViewHolder(v: View) : GroupRecyclerViewAdapter.ItemViewHolder(v) {
//        internal var layout = v.country_item_child_container
//        internal var itemIcon : ImageView = v.item_icon
//        internal var itemName : TextView = v.item_name
//        internal var itemDesp = v.item_desp
//        internal var itemAmount = v.item_amount
//    }
//
//    override fun onCreateHeadViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return HViewHolder(
//            LayoutInflater.from(parent.context).inflate(
//                R.layout.group_header_item, parent, false
//            )
//        )
//    }
//
//    override fun onBindHeadViewHolder(holder: HViewHolder, position: Int) {
//        if (searchList.isEmpty()) {
//            when(position) {
//                0 -> {
//                    holder.title.text = holder.itemView.resources.getText(R.string.frequently_used)
//                    holder.layout.setBackgroundColor(holder.itemView.resources.getColor(R.color.white))
//                }
//                popularList.size + 1 -> {
//                    holder.title.text = holder.itemView.resources.getText(R.string.others)
//                }
//            }
//        }
//    }
//
//    override fun onCreateItemViewHolder(child: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return CViewHolder(
//            LayoutInflater.from(child.context).inflate(
//                R.layout.currency_list_item, child, false
//            )
//        )
//    }
//
//    override fun onBindItemViewHolder(holder: CViewHolder, position: Int) {
//        var item = getCurrencyCode(position)
//
//        holder.itemName.text = item.currencyName
//        holder.itemDesp.text = item.code
//        holder.itemAmount.visibility = View.INVISIBLE
//        holder.itemAmount.visibility = View.INVISIBLE
//
//        var id = getFlagResouce(item.code!!, holder.itemView.context)
//        holder.itemIcon.setImageResource(id)
//    }
//
//    override fun onItemClick(position: Int) {
//        var item = getCurrencyCode(position)
//        mListener.onGroupRecyclerViewAdapterSelected(item)
//    }
//
//    private fun getCurrencyCode(position: Int): CurrencyCode {
//        var item: CurrencyCode
//        if (searchList.isEmpty()) {
//            if (position - 1 < popularList.size) {
//                item = popularList[position - 1]
//            } else {
//                item = allList[position - popularList.size - 2]
//            }
//        } else {
//            item = searchList[position]
//        }
//
//        return item
//    }
//
//    fun getFilter(): Filter {
//        return object : Filter() {
//            override fun performFiltering(constraint: CharSequence): FilterResults {
//                val oReturn = FilterResults()
//                val totalList = java.util.ArrayList<CurrencyCode>()
//                if (constraint.isNotEmpty()) {
//                    popularList.forEach{
//                        if (it.currencyName.contains(constraint, ignoreCase = true))
//                            totalList.add(it)
//                    }
//                    allList.forEach {
//                        if (it.currencyName.contains(constraint, ignoreCase = true))
//                            totalList.add(it)
//                    }
//                }
//                oReturn.values = totalList
//                return oReturn
//            }
//
//            override fun publishResults(constraint: CharSequence, results: FilterResults) {
//                searchList = results.values as ArrayList<CurrencyCode>
//                notifyDataSetChanged()
//                if (constraint.isNotEmpty() && searchList.isEmpty())
//                    mListener.noResultsFound()
//                else
//                    mListener.resultsFound()
//            }
//        }
//    }
//}