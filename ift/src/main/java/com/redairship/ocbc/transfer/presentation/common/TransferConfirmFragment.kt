package com.redairship.ocbc.transfer.presentation.common

import android.content.Intent
import android.os.Handler
import androidx.core.app.ShareCompat
import androidx.core.view.isVisible
import com.ocbc.transfer.LocalTransferConfig
import com.ocbc.transfer.R
import com.ocbc.transfer.databinding.FragmentLocaltransferConfirmDetailsBinding
import com.redairship.ocbc.transfer.presentation.base.BaseTransferFragment
import com.redairship.ocbc.transfer.presentation.base.TransferStatus
import com.redairship.ocbc.transfer.presentation.base.convertStrToDateFormat
import com.redairship.ocbc.transfer.presentation.base.viewShoot
import com.ocbc.transfer.presentation.local.TransferActivity
import kotlinx.android.synthetic.main.transferto_confirmation_top.*
import org.koin.android.ext.android.inject

class TransferConfirmFragment : BaseTransferFragment<FragmentLocaltransferConfirmDetailsBinding>() {
    override fun getContentLayoutId(): Int = R.layout.fragment_localtransfer_confirm_details
    val config by inject<LocalTransferConfig>()

    override fun onBindView() {
        with(dataBinding) {

            btNewtransfer.setOnClickListener {
                var intent = Intent(requireActivity(), TransferActivity::class.java)
                startActivity(intent)
            }
            btDone.setOnClickListener {
                requireActivity().finish()
            }
            logoutBtn.setOnClickListener {
                config.logout(requireActivity(), "N")
            }
            executePendingBindings()
        }
    }

    override fun setupViewEvents() {
        chk_extendbody.setOnClickListener {
            if (detail_body.isVisible) {
                detail_body.isVisible = false
                view_bottomOfBody.isVisible = false
                chk_extendbody.setImageResource(R.drawable.ras_components_ic_bb_chevron_down_thick)
            } else {
                detail_body.isVisible = true
                view_bottomOfBody.isVisible = true
                chk_extendbody.setImageResource(R.drawable.ras_components_ic_bb_chevron_up_thick)
            }
        }
        tv_share_transfer_detail.setOnClickListener {
            detail_body.isVisible = true
            Handler().postDelayed({
                val screenShoot = viewShoot(requireContext(), confirm_body, "localtransfer_confirm.jpeg")
                ShareCompat.IntentBuilder.from(requireActivity())
                    .setStream(screenShoot)
                    .setType("*/*")
                    .startChooser()
            },100)
        }
//        info_banner.apply {
//            onSpanClicked = {
//            }
//        }
//        dataBinding.scrollviewDetail.setPivotView(chk_extendbody, view_bottomOfBody)
    }

    private fun showCoachBox() {
//        CoachMarkGlobalRectView(dataBinding.rootview, tv_share_transfer_detail, false)
//            .apply {
//                this.anchorGap = 3.0f
//                startMargin = resources.getDimension(R.dimen.size_32dp).toInt()
//                endMargin = resources.getDimension(R.dimen.size_32dp).toInt()
//
//                this.message = getString(R.string.tap_to_share_success_of_this_transfer)
//            }.show()
    }

    override fun setViewData(transfertype: TransferStatus?) {
//        showCoachBox()
        tv_confirm_title.text = getString(
            R.string.you_have_send_to,
            sharedViewModel.getLocalTransferData().selectToAc.amount.toString(),
            sharedViewModel.getLocalTransferData().selectToAc.currency)
        tv_sendto_name.text = sharedViewModel.getLocalTransferData().selectToAc.name
        tv_sendto_account.text = sharedViewModel.getLocalTransferData().selectToAc.description

        confirm_fromaccount.title = sharedViewModel.getLocalTransferData().selectFromAc.name
        confirm_valueDate.title = convertStrToDateFormat(sharedViewModel.getLocalTransferData().selectedDate)
        confirm_remarks.title = sharedViewModel.getLocalTransferData().remarks
        confirm_reference.title = sharedViewModel.getLocalTransferData().referenceNumber
    }
}