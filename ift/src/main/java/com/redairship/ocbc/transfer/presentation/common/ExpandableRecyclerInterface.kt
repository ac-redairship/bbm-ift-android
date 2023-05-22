package com.redairship.ocbc.transfer.presentation.common

import com.redairship.ocbc.transfer.model.AccountItemModel

interface ExpandableRecyclerInterface<ExpandedType : Any,
        ExpandableType : ExpandableRecyclerViewAdapter.ExpandableGroup<ExpandedType>> {

    fun selectParentItem(account: ExpandableType)

    fun onExpandOrCollapse(isExpanded: Boolean)

    fun selectChildItem(parentAccount: ExpandableType, childAccount: ExpandedType)
}
