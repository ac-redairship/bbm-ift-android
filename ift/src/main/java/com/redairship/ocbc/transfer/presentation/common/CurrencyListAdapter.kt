package com.redairship.ocbc.transfer.presentation.common

import android.view.View
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.redairship.ocbc.transfer.presentation.base.CurrencyCode
import com.redairship.ocbc.transfer.presentation.base.getFlagResouce
import com.ocbc.transfer.R
import com.redairship.ocbc.bb.components.views.textviews.BBAmountTextView
import kotlinx.android.synthetic.main.accountlist_child_item.view.*
import kotlinx.android.synthetic.main.group_header_item.view.*

class CurrencyListAdapter(
    selectedCurrency: String?,
    val popularList: List<CurrencyCode>,
    val allList: List<CurrencyCode>,
    private val clickListener: OnGroupRecyclerViewAdapterListener<CurrencyCode>,
    private val searchResultsListener: SearchResultsListener
) : GroupRecyclerViewAdapter<CurrencyListAdapter.Group, CurrencyCode, CurrencyListAdapter.HViewHolder, CurrencyListAdapter.CViewHolder>
    (
    listOf(
        Group.FREQUENTLY_USED to popularList,
        Group.ALL to allList,
    )
) {

    enum class Group(val desc: String) {
        FREQUENTLY_USED("Frequently used"), ALL("All")
    }

    var searchList: List<CurrencyCode> = listOf()

    override fun getItemViewType(position: Int): Int {
        if (searchList.isEmpty()) {
            // Header for 'Most popular countries'
            if (position == 0) {
                return VIEW_TYPE_GROUP
            }
            // Header for 'All' countries
            if (position == popularList.size + 1) {
                return VIEW_TYPE_GROUP
            }
        }
        return VIEW_TYPE_ITEM
    }

    class HViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        internal var layout = v.header_lly
        internal var title: TextView = v.header_title
    }

    class CViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        internal var layout = v.country_item_child_container
        internal var itemIcon: ImageView = v.item_icon
        internal var itemName: TextView = v.item_name
        internal var itemDesp = v.item_desp
        internal var itemAmount: BBAmountTextView = v.item_amount
    }

    private fun getCurrencyCode(position: Int): CurrencyCode {
        var item: CurrencyCode
        if (searchList.isEmpty()) {
            if (position - 1 < popularList.size) {
                item = popularList[position - 1]
            } else {
                item = allList[position - popularList.size - 2]
            }
        } else {
            item = searchList[position]
        }

        return item
    }

    fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val oReturn = FilterResults()
                val totalList = java.util.ArrayList<CurrencyCode>()
                if (constraint.isNotEmpty()) {
                    popularList.forEach {
                        if (it.currencyName.contains(constraint, ignoreCase = true))
                            totalList.add(it)
                    }
                    allList.forEach {
                        if (it.currencyName.contains(constraint, ignoreCase = true))
                            totalList.add(it)
                    }
                }
                oReturn.values = totalList
                return oReturn
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                searchList = results.values as ArrayList<CurrencyCode>
                notifyDataSetChanged()
                if (constraint.isNotEmpty() && searchList.isEmpty())
                    searchResultsListener.noResultsFound()
                else
                    searchResultsListener.resultsFound()
            }
        }
    }

    override fun getGroupLayout(): Int = R.layout.group_header_item
    override fun getItemLayout(): Int = R.layout.currency_list_item

    override fun onCreateGroupViewHolder(view: View): HViewHolder {
        return HViewHolder(view)
    }

    override fun onCreateItemViewHolder(view: View): CViewHolder {
        return CViewHolder(view)
    }

    override fun onBindGroupViewHolder(holder: HViewHolder, group: Group) {
        if (searchList.isEmpty()) {
            when (group) {
                Group.FREQUENTLY_USED -> {
                    holder.title.text = holder.itemView.resources.getText(R.string.frequently_used)
                    holder.layout.setBackgroundColor(
                        ContextCompat.getColor(
                            holder.layout.context,
                            R.color.ras_components_bb_color_white
                        )
                    )
                }
                Group.ALL -> {
                    holder.title.text = holder.itemView.resources.getText(R.string.others)
                }
            }
        }
    }

    val flagMap = mutableMapOf<String, Int>()

    override fun onBindItemViewHolder(holder: CViewHolder, item: CurrencyCode, group: Group) {
        holder.itemName.text = item.currencyName
        holder.itemDesp.text = item.code
        holder.itemAmount.visibility = View.INVISIBLE
        holder.itemAmount.visibility = View.INVISIBLE

        holder.layout.setOnClickListener {
            clickListener.onChildItemClick(item)
        }

        val id = flagMap.getOrPut(item.code) {
            getFlagResouce(item.code, holder.itemView.context)
        }

        try {
            holder.itemIcon.setImageDrawable(ResourcesCompat.getDrawable(holder.itemView.context.resources, id, null))
        } catch (e: Exception) {

        }

    }
}