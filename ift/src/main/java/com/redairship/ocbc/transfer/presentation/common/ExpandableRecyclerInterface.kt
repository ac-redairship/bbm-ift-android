package com.redairship.ocbc.transfer.presentation.common

import com.redairship.ocbc.transfer.model.AccountItemModel

interface ExpandableRecyclerInterface<ExpandedType : Any,
        ExpandableType : ExpandableRecyclerViewAdapter.ExpandableGroup<ExpandedType>> {

    fun selectParentItem(account: ExpandableType)

    fun selectChildItem(parentAccount: ExpandableType, childAccount: ExpandedType)
}
