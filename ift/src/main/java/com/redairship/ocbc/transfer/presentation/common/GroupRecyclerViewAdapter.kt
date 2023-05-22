package com.redairship.ocbc.transfer.presentation.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class GroupRecyclerViewAdapter<G, T, GVH : RecyclerView.ViewHolder, TVH : RecyclerView.ViewHolder>
    (private var items: List<Pair<G, List<T>>>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_GROUP = 0
        const val VIEW_TYPE_ITEM = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == VIEW_TYPE_GROUP) {
            val view = inflater.inflate(getGroupLayout(), parent, false)
            onCreateGroupViewHolder(view)
        } else {
            val view = inflater.inflate(getItemLayout(), parent, false)
            onCreateItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)

        if (viewType == VIEW_TYPE_GROUP) {
            val groupViewHolder = holder as GVH
            val groupIndex = getItemIndex(position)
            val group = items[groupIndex].first
            onBindGroupViewHolder(groupViewHolder, group)
        } else {
            val itemViewHolder = holder as TVH
            val itemIndex = getItemIndex(position)
            val itemPosition = getItemPosition(position)
            if (itemIndex < 0 || itemPosition < 0) return
            val item = items[itemIndex].second[itemPosition]
            val group = items[itemIndex].first
            onBindItemViewHolder(itemViewHolder, item, group)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isGroup(position)) VIEW_TYPE_GROUP else VIEW_TYPE_ITEM
    }

    override fun getItemCount(): Int {
        var count = 0

        items.forEach { count += 1 + it.second.size }

        return count
    }

    fun getGroupCount(): Int = items.size

    fun getItemCount(groupPosition: Int): Int = items[groupPosition].second.size

    fun isGroup(position: Int): Boolean {
        var count = 0

        items.forEach {
            count++

            if (position == count - 1) {
                return true
            }

            count += it.second.size
        }

        return false
    }

    fun getItemIndex(adapterPosition: Int): Int {
        var position = adapterPosition
        for (groupIndex in items.indices) {
            if (position == 0) {
                return groupIndex
            }
            val groupSize = items[groupIndex].second.size
            if (position <= groupSize) {
                return groupIndex
            }
            position -= groupSize + 1
        }
        throw IndexOutOfBoundsException("Invalid adapter position")
    }

    fun getItemPosition(adapterPosition: Int): Int {
        var position = adapterPosition
        for (groupIndex in items.indices) {
            val groupSize = items[groupIndex].second.size
            if (position <= groupSize) {
                return position - 1
            }
            position -= groupSize + 1
        }
        throw IndexOutOfBoundsException("Invalid adapter position")
    }

    abstract fun getGroupLayout(): Int

    abstract fun getItemLayout(): Int

    abstract fun onCreateGroupViewHolder(view: View): GVH

    abstract fun onCreateItemViewHolder(view: View): TVH

    abstract fun onBindGroupViewHolder(holder: GVH, group: G)

    abstract fun onBindItemViewHolder(holder: TVH, item: T, group: G)

    fun setItems(newItems: List<Pair<G, List<T>>>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun getItems(): List<Pair<G, List<T>>> = items

    interface OnGroupRecyclerViewAdapterListener<T> {
        fun onGroupItemClick(item: T)
        fun onChildItemClick(item: T)
    }
}