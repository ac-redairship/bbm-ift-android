package com.redairship.ocbc.transfer.presentation.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.ocbc.transfer.R
import com.redairship.ocbc.transfer.model.AccountItemModel
import com.redairship.ocbc.transfer.presentation.base.getFlagResouce
import com.redairship.ocbc.transfer.utils.DataTransform
import kotlinx.android.synthetic.main.accountlist_child_item.view.*
import kotlinx.android.synthetic.main.accountlist_parent_item.view.*
import java.math.BigDecimal


class AccountListExpandableAdapter(parents: ArrayList<AccountItemModel>,
                                   private val expandableRecyclerInterface: ExpandableRecyclerInterface<AccountItemModel, AccountItemModel>
) :
    ExpandableRecyclerViewAdapter<AccountItemModel, AccountItemModel, AccountListExpandableAdapter.PViewHolder, AccountListExpandableAdapter.CViewHolder>(
        parents, ExpandingDirection.VERTICAL
    ) {

    var hideBalance : Boolean = false
    private val minRenderTime = 36L

    class PViewHolder(v: View) : ExpandableRecyclerViewAdapter.ExpandableViewHolder(v) {
        internal var layout = v.country_item_parent_container
        internal var title : TextView = v.title
        internal var expandIcon = v.close_arrow
        internal var collapseIcon = v.up_arrow
        internal var description = v.description
        internal var availableBalance = v.availableBalance
    }

    class CViewHolder(v: View) : ExpandableRecyclerViewAdapter.ExpandedViewHolder(v) {
        internal var layout = v.country_item_child_container
        internal var itemIcon : ImageView = v.item_icon
        internal var itemName : TextView = v.item_name
        internal var itemDesp = v.item_desp
        internal var itemAmount = v.item_amount
    }

    override fun onCreateParentViewHolder(parent: ViewGroup, viewType: Int): PViewHolder {
        return PViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.accountlist_parent_item, parent, false
            )
        )
    }


    override fun onCreateChildViewHolder(child: ViewGroup, viewType: Int): CViewHolder {
        return CViewHolder(
            LayoutInflater.from(child.context).inflate(
                R.layout.accountlist_child_item,
                child,
                false
            )
        )
    }

    override fun onBindParentViewHolder(
        parentViewHolder: PViewHolder,
        expandableType: AccountItemModel,
        position: Int
    ) {

        val money = DataTransform.formatMoney(BigDecimal(expandableType.amount.value.toString().replace(",".toRegex(), ""))).toString()

        parentViewHolder.description.text = money + " " + parentViewHolder.containerView.context.getText(R.string.available_text)
        parentViewHolder.description.visibility = if (hideBalance) View.INVISIBLE else View.VISIBLE
        if (expandableType.availableAmounts.isEmpty()) {
            parentViewHolder.title.text = "${expandableType.accountNumber} ${expandableType.currency.currencyCode} - ${expandableType.accountName}"
            parentViewHolder.expandIcon.visibility = View.INVISIBLE
            parentViewHolder.collapseIcon.visibility = View.INVISIBLE
        } else {
            parentViewHolder.title.text = "${expandableType.accountNumber} - Multi-currency"
        }
    }

    override fun onBindChildViewHolder(
        childViewHolder: CViewHolder,
        expandedType: AccountItemModel,
        expandableType: AccountItemModel,
        position: Int
    ) {
        childViewHolder.itemName.text = expandedType.amount.currency.displayName
        childViewHolder.itemDesp.text = expandedType.amount.currency.currencyCode
        childViewHolder.itemAmount.amount = expandedType.amount
        childViewHolder.itemAmount.visibility = if (hideBalance) View.INVISIBLE else View.VISIBLE

        val id = getFlagResouce(expandedType.amount.currency.currencyCode, childViewHolder.itemView.context)
        childViewHolder.itemIcon.setImageResource(id)
    }

    override fun onExpandableClick(
        position: Int,
        expandableViewHolder: PViewHolder,
        expandableType: AccountItemModel
    ) {

        if (expandableType.availableAmounts.isEmpty()) {
            expandableRecyclerInterface.selectParentItem(expandableType)
        } else {
            if (expandableType.isExpanded) {
                expandableViewHolder.availableBalance.visibility = View.VISIBLE
                expandableViewHolder.expandIcon.visibility = View.GONE
                expandableViewHolder.collapseIcon.visibility = View.VISIBLE
            } else {
                expandableViewHolder.availableBalance.visibility = View.GONE
                expandableViewHolder.expandIcon.visibility = View.VISIBLE
                expandableViewHolder.collapseIcon.visibility = View.GONE
            }

            // 30fps = 32 ms per frame + some allowance,
            // 99th percentile of android devices can render a frame in 36ms
            expandableViewHolder.collapseIcon.postDelayed({
                notifyItemChanged(position)
            }, minRenderTime)
        }

    }

    override fun onExpandedClick(
        expandableViewHolder: PViewHolder,
        expandedViewHolder: CViewHolder,
        expandedType: AccountItemModel,
        expandableType: AccountItemModel
    ) {
        expandableRecyclerInterface.selectChildItem(expandableType, expandedType)
    }

}