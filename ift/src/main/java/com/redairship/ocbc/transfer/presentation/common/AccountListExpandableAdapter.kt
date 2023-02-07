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

    class PViewHolder(v: View) : ExpandableRecyclerViewAdapter.ExpandableViewHolder(v) {
        internal var layout = v.country_item_parent_container
        internal var title : TextView = v.title
        internal var closeImage = v.close_arrow
        internal var upArrowImg = v.up_arrow
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
        parentViewHolder.title.text = expandableType.accountNo + expandableType.currency

        val money = DataTransform.formatMoney(BigDecimal(expandableType.amount.toString().replace(",".toRegex(), ""))).toString()

        parentViewHolder.description.text = money + " " + parentViewHolder.containerView.context.getText(R.string.available_text)
        parentViewHolder.description.visibility = if (hideBalance) View.INVISIBLE else View.VISIBLE
        if (expandableType.accountItems.isEmpty()) {
            parentViewHolder.closeImage.visibility = View.INVISIBLE
            parentViewHolder.upArrowImg.visibility = View.INVISIBLE
        }
    }

    override fun onBindChildViewHolder(
        childViewHolder: CViewHolder,
        expandedType: AccountItemModel,
        expandableType: AccountItemModel,
        position: Int
    ) {
        childViewHolder.itemName.text = expandedType.accountNo
        childViewHolder.itemDesp.text = expandedType.currency
        childViewHolder.itemAmount.text = expandedType.amount.toString()
        childViewHolder.itemAmount.visibility = if (hideBalance) View.INVISIBLE else View.VISIBLE

        var id = getFlagResouce(expandedType.currency!!, childViewHolder.itemView.context)
        childViewHolder.itemIcon.setImageResource(id)
    }

    override fun onExpandableClick(
        expandableViewHolder: PViewHolder,
        expandableType: AccountItemModel
    ) {
        if (expandableType.accountItems.isEmpty()) {
            expandableRecyclerInterface.selectParentItem(expandableType)
        } else {
            if (expandableType.isExpanded) {
                expandableViewHolder.availableBalance.visibility = View.VISIBLE
                expandableViewHolder.closeImage.visibility = View.GONE
                expandableViewHolder.upArrowImg.visibility = View.VISIBLE
            } else {
                expandableViewHolder.availableBalance.visibility = View.GONE
                expandableViewHolder.closeImage.visibility = View.VISIBLE
                expandableViewHolder.upArrowImg.visibility = View.GONE
            }
        }

        notifyDataSetChanged()
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