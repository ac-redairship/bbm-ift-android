package com.redairship.ocbc.transfer.presentation.common

import android.content.Intent
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import androidx.core.view.isVisible
import com.redairship.ocbc.transfer.LocalTransferConfig
import com.ocbc.transfer.R
import com.ocbc.transfer.databinding.FragmentLocaltransferConfirmDetailsBinding
import com.redairship.ocbc.transfer.presentation.base.BaseTransferFragment
import com.redairship.ocbc.transfer.presentation.base.TransferStatus
import com.redairship.ocbc.transfer.presentation.base.convertStrToDateFormat
import com.redairship.ocbc.transfer.presentation.base.viewShoot
import com.redairship.ocbc.transfer.presentation.local.TransferActivity
import kotlinx.android.synthetic.main.transferto_confirmation_top.*
import org.koin.android.ext.android.inject
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class TransferConfirmFragment : BaseTransferFragment<FragmentLocaltransferConfirmDetailsBinding>() {
    val config by inject<LocalTransferConfig>()

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = FragmentLocaltransferConfirmDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onBindView() {
        with(binding) {

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
        binding.vTransferDetailsBody.tvShareTransferDetail.setOnClickListener {
            binding.vTransferDetailsBody.detailBody.isVisible = true
            binding.logoutBtn.isVisible = false
            binding.bottomLayout.isVisible = false
            binding.vTransferDetailsBody.tvShareTransferDetail.isVisible = false
            binding.vTransferDetailsBody.vInfoBanner.isVisible = false
            Handler().postDelayed({
                val screenShoot = viewShoot(requireContext(), binding.rootview, "localtransfer_confirm.jpeg")
                ShareCompat.IntentBuilder.from(requireActivity())
                    .setStream(screenShoot)
                    .setType("*/*")
                    .startChooser()

                binding.vTransferDetailsBody.detailBody.isVisible = true
                binding.vTransferDetailsBody.vInfoBanner.isVisible = true
                binding.vTransferDetailsBody.tvShareTransferDetail.isVisible = true
                binding.logoutBtn.isVisible = true
                binding.bottomLayout.isVisible = true
            },100)


        }
//        info_banner.apply {
//            onSpanClicked = {
//            }
//        }
//        binding.scrollviewDetail.setPivotView(chk_extendbody, view_bottomOfBody)
    }

    private fun showCoachBox() {
//        CoachMarkGlobalRectView(binding.rootview, tv_share_transfer_detail, false)
//            .apply {
//                this.anchorGap = 3.0f
//                startMargin = resources.getDimension(R.dimen.size_32dp).toInt()
//                endMargin = resources.getDimension(R.dimen.size_32dp).toInt()
//
//                this.message = getString(R.string.tap_to_share_success_of_this_transfer)
//            }.show()
    }

    override fun setViewData(transfertype: TransferStatus?) = with(binding.vTransferDetailsBody) {
//        showCoachBox()
        val selectToAc = localTransferData.selectToAc
        val hasFractions = selectToAc.amount.currency.defaultFractionDigits > 0
        val decimalFormat = getDecimalFormat(hasFractions, selectToAc.amount.locale)

        tvConfirmTitle.text = getString(
            R.string.you_have_send_to,
            decimalFormat.format(selectToAc.amount.value.movePointLeft(2)),
            selectToAc.currency)
        tvSendtoName.text = selectToAc.displayName
        tvSendtoAccount.text = "${selectToAc.accountNumber} - ${selectToAc.bankName}"


        tvReviewAccountDetails.text = localTransferData.selectFromAc.accountName
        tvReviewValueDate.text = convertStrToDateFormat(localTransferData.selectedDate)
        tvReviewNoteForPayeeValue.text = localTransferData.remarks
        tvReviewReferenceValue.text = localTransferData.referenceNumber

        tvReviewNoteForPayeeLabel.isVisible = localTransferData.remarks.isNotBlank()
        tvReviewNoteForPayeeValue.isVisible = localTransferData.remarks.isNotBlank()
        tvReviewReferenceLabel.isVisible = localTransferData.referenceNumber.isNotBlank()
        tvReviewReferenceValue.isVisible = localTransferData.referenceNumber.isNotBlank()

    }

    private fun getDecimalFormat(hasFractions: Boolean, locale: Locale): DecimalFormat {
        val symbols = DecimalFormatSymbols(locale)
        val pattern = if (hasFractions) "#,##0.##" else "#,##0"
        return DecimalFormat(pattern, symbols)
    }

}