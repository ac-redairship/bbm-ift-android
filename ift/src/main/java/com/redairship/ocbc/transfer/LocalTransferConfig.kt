package com.redairship.ocbc.transfer

import android.app.Activity
import android.content.Context
import com.redairship.ocbc.transfer.model.AccountItemModel
import com.redairship.ocbc.transfer.model.FxOtherRate
import com.redairship.ocbc.transfer.presentation.local.OnIFTFxContractListener

/**
 * Noti centre behaviour and resource config.
 * @author Gopalan
 */
interface LocalTransferConfig {

    fun openLocalTransferActivity(activity: Activity,
                                  selectedToAccount:String,
                                  ownAccountsList: List<AccountItemModel>,
                                  selectFromAc: AccountItemModel
    )

    fun openFxContractListView(context: Context,
                               ownAccountsList: List<AccountItemModel>,
                               selectFromAc: AccountItemModel,
                               selectedContract: FxOtherRate?,
                               amount: String,
                               remitCurrency: String,
                               isMSApi: Boolean,
                               fucntionCode: String?,
                               action: String,
                               listener: OnIFTFxContractListener
    )

    fun logout(activity: Activity, isAbsoluteLogout: String?)

}