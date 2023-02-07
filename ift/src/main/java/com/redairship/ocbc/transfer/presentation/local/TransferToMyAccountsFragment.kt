package com.redairship.ocbc.transfer.presentation.local

import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.isVisible
import com.ocbc.transfer.R
import com.ocbc.transfer.*
import com.ocbc.transfer.databinding.FragmentLocalTransferToMyaccountsBinding
import com.ocbc.transfer.presentation.local.TransferActivity
import com.ocbc.transfer.presentation.local.TransferFromAccountListBottomSheet
import com.redairship.ocbc.bb.components.models.Amount
import com.redairship.ocbc.transfer.UiState
import com.redairship.ocbc.transfer.model.AccountItemModel
import com.redairship.ocbc.transfer.model.FxContract
import com.redairship.ocbc.transfer.model.FxOtherRate
import com.redairship.ocbc.transfer.model.TransactionPreSubmitType
import com.redairship.ocbc.transfer.presentation.base.*
import com.redairship.ocbc.transfer.presentation.base.convertDateToString
import com.redairship.ocbc.transfer.presentation.base.convertStrToDateFormat
import com.redairship.ocbc.transfer.presentation.common.AccountListBottomInterface
import com.redairship.ocbc.transfer.presentation.common.AmountWithCurrencyTextView
import com.redairship.ocbc.transfer.presentation.common.InsufficientBalanceBottomSheet
import com.redairship.ocbc.transfer.presentation.common.ProcessingBottomSheet
import com.redairship.ocbc.transfer.presentation.doAfterTextChanged
import kotlinx.android.synthetic.main.transferto_myaccounts_detail.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.Serializable
import java.math.BigDecimal
import java.util.*


interface OnIFTFxContractListener : Serializable {
    fun onIFTSelectFxContract(contract: FxOtherRate?, isManual: Boolean)
}

class TransferToMyAccountsFragment : BaseTransferFragment<FragmentLocalTransferToMyaccountsBinding>(),
    AccountListBottomInterface,
    MotionLayout.TransitionListener {
    val config by inject<LocalTransferConfig>()
    var endState: Int? = null
    var startState: Int? = null
    var firstKeyboardLoad = false
    var selectedBVFxOtherRate: FxOtherRate? = null

    override fun getContentLayoutId(): Int = R.layout.fragment_local_transfer_to_myaccounts

    override fun onBindView() {
        with(dataBinding) {
            endState = motionLayout.endState
            startState = motionLayout.startState
//        binding.motionLayout?.transitionToEnd()
            Handler().postDelayed({
                motionLayout.apply{
                    reSizeScrollView(body_detail_lly, headerLlyEnd)
                    setTransitionListener(this@TransferToMyAccountsFragment)
            //                sendCurrencyView.focusWithKeyboard()
                    vScroll.isVisible = false
                }
            }, 50)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (sharedViewModel.getLocalTransferData().selectedDate.isNullOrEmpty())
            sharedViewModel.getLocalTransferData().selectedDate = Date().time.toString()

        super.onViewCreated(view, savedInstanceState)
        sharedViewModel.getProductTransactionLimit(sharedViewModel.getLocalTransferData().selectedDate)
    }

    override fun setupViewEvents() {
        dataBinding.headerStartIconStart.apply {
            setOnClickListener {
                backStack()
            }
        }
        dataBinding.headerStartIconEnd.apply {
            setOnClickListener {
                backStack()
            }
        }

        dataBinding.root.apply {
            viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val r = Rect()
                    dataBinding.root.getWindowVisibleDisplayFrame(r)
                    //get screen height and calculate the difference with the useable area from the r
                    val screenHeight: Int = dataBinding.root.context.resources.displayMetrics.heightPixels
                    // r.bottom is the position above soft keypad or device button.
                    // if keypad is shown, the r.bottom is smaller than that before.
                    val keypadHeight = screenHeight - r.bottom

                    if (keypadHeight > screenHeight * 0.15) {
                        dataBinding.vScroll.isVisible = false
                        firstKeyboardLoad = true
                    } else {
                        // keyboard is closed
                        dataBinding.vScroll.isVisible = true
                        if (firstKeyboardLoad)
                            dataBinding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                }
            })
        }

        dataBinding.sendCurrencyView.apply {
            val currency = sharedViewModel.getLocalTransferData().selectToAc.currency
            setCurrencyView(this, currency)
            doAfterTextChanged {
                sharedViewModel.getLocalTransferData().selectToAc.amount = it.toString().toDouble()
            }
            this.binding.tvCurrency.setOnClickListener {
                openCurrencyListView(currency)
            }
            this.binding.tvAmount.setOnEditorActionListener { textView, i, keyEvent ->
                clearFocus()
                false
            }
        }
        tf_remarks.apply{
            doAfterTextChanged {
                sharedViewModel.getLocalTransferData().remarks = it
                desp_remarks.text = "${it.length} / 16"
            }
        }
        tf_reference_number.apply{
            doAfterTextChanged {
                sharedViewModel.getLocalTransferData().referenceNumber = it
                desp_reference_number.text = "${it.length} / 140"
            }
        }
        transferfrom_change.setOnClickListener {
            TransferFromAccountListBottomSheet.newInstance(
                TransferStatus.TransferFrom,
                this@TransferToMyAccountsFragment
            ).show(childFragmentManager, null)
        }

        tvValueDate.apply {
            text = convertStrToDateFormat(sharedViewModel.getLocalTransferData().selectedDate)
            setOnClickListener {
                openScrollCalendar()
            }
        }

        lly_addaspayee.setOnClickListener {
            sharedViewModel.getLocalTransferData().addAspayee = !sharedViewModel.getLocalTransferData().addAspayee
            chk_addaspayee.setImageResource(
                if (sharedViewModel.getLocalTransferData().addAspayee) {
                    R.drawable.ras_passportauth_ic_checkbox_checked
                } else {
                    R.drawable.ras_passportauth_ic_checkbox_unchecked
                }
            )
        }

        bt_next.setOnClickListener {
            when (transferStatus) {
                TransferStatus.TransferMakerReview -> {
                    openProcessingBottomView()
                }
                else -> {
                    if (sharedViewModel.getLocalTransferData().defaultMinLimit >
                        sharedViewModel.getLocalTransferData().selectToAc.amount.toFloat()) {
                        var message = getString(R.string.amount_smaller_than,
                            String.format("%,.2f", sharedViewModel.getLocalTransferData().defaultMinLimit))
                        showGenericError(message, false)
                    } else if (sharedViewModel.getLocalTransferData().defaultMaxLimit <
                        sharedViewModel.getLocalTransferData().selectToAc.amount.toFloat()) {
                        var message = getString(R.string.transaction_amount_not_exceed,
                            String.format("%,.2f", sharedViewModel.getLocalTransferData().defaultMaxLimit))
                        showGenericError(message, false)
                    } else {
                        sharedViewModel.doPreSubmit(TransactionPreSubmitType.PREVIEW)
                    }
                }
            }
        }

        launchWhenResumed {
            launch {
                sharedViewModel.insufficientBalanceOutcome
                    .collectLatest {
                        when (it) {
                            is UiState.Loading -> {
                                bt_next.isLoading = true
                            }
                            is UiState.Success -> {
                                bt_next.isLoading = false
                                transferStatus = TransferStatus.TransferMakerReview
                                setViewData(TransferStatus.TransferMakerReview)
                            }
                            is UiState.Error -> {
                                bt_next.isLoading = false
                                when(it.throwable) {
                                    is InsufficientBalanceException -> {
                                        openInsufficientBalanceView()
                                    }
                                    is UnexpectedException,
                                    is NetworkException -> {
                                        (activity as TransferActivity).showGenericServerErrorScreen(it.throwable.message)
                                    }
                                }
                            }
                        }
                    }
            }
        }

        launchWhenCreated {
            launch {
                sharedViewModel.checkvaluedate
                    .collectLatest {
                        if (it is UiState.Success) {
                            sharedViewModel.getLocalTransferData().selectedDate = it.data.nextValueDate
                            updateValueDate(it.data.nextValueDate)
                        }
                    }
            }
        }
        launchWhenCreated {
            launch {
                sharedViewModel.fxContract
                    .collectLatest {
                        when (it) {
                            is UiState.Success -> {
                                sharedViewModel.getLocalTransferData().selectToAnotherCurrency.currency = it.data.symbol!!

                                sharedViewModel.getProductTransactionLimit(sharedViewModel.getLocalTransferData().selectedDate)

                                setShowAndHideSkeletonView(false)
                                dataBinding.recipientCurrencyView.apply {
                                    amount = Amount(it.data.indicativeAmount?.toBigDecimal() ?: BigDecimal.ZERO, it.data.symbol, Locale.ENGLISH, it.data.contractBuyCurrency ?: "SGD")
//                                    amount.setText(it.data.indicativeAmount.toString())

                                    val currency = it.data.symbol
                                    setCurrencyView(this, currency)
                                    doAfterTextChanged { editable ->
                                        sharedViewModel.getLocalTransferData().selectToAnotherCurrency.amount = editable.toString().toDouble()
                                    }
                                    this.binding.tvAmount.setOnEditorActionListener { textView, i, keyEvent ->
                                        clearFocus()
                                        false
                                    }
                                    this.binding.tvCurrency.setOnClickListener {
                                        openCurrencyListView(currency)
                                    }
                                }
                            }
                            is UiState.Error -> {
                                showGenericError(it.throwable.message, false)
                                setShowAndHideSkeletonView(false)
                            }
                        }

                    }
            }
        }
    }

    private fun openCurrencyListView(currency: String?) {
//        CurrencyListBottomSheet(currency).apply {
//            onSelectedCurrencyClicked = { currency ->
//                sharedViewModel.getCounterFX(currency.code)
//                setShowAndHideSkeletonView(true)
//            }
//        }.show(childFragmentManager, "")
    }

    private fun openInsufficientBalanceView() {
        InsufficientBalanceBottomSheet().apply {
            onChangeAccountClicked = {
                TransferFromAccountListBottomSheet.newInstance(
                    TransferStatus.TransferFrom,
                    this@TransferToMyAccountsFragment
                ).show(childFragmentManager, null)
            }
        }.show(childFragmentManager, "")
    }

    private fun openProcessingBottomView() {
        ProcessingBottomSheet(transferStatus).apply{
            onProcessingFinish = {
//                findNavController().navigate(TransferToMyAccountsFragmentDirections.actiontoconfirm())
            }
        }.show(childFragmentManager, "")
    }

    private fun openScrollCalendar() {
//        ScrollCalendarBottomSheet(sharedViewModel.getLocalTransferData().selectedDate).apply {
//            onSelectedDateClicked = { date ->
//                sharedViewModel.getLocalTransferData().selectedDate = convertDateToString(date)
//                updateValueDate(sharedViewModel.getLocalTransferData().selectedDate)
//            }
//        }.show(childFragmentManager, null)
    }

    private fun updateValueDate(dateString: String) {
        tvValueDate.text = convertStrToDateFormat(dateString)
    }

    override fun setViewData(transfertype: TransferStatus?) {
        dataBinding.accountName.text = sharedViewModel.getLocalTransferData().selectToAc.name
        dataBinding.accountNumber.text = sharedViewModel.getLocalTransferData().selectToAc.accountNo

        when(transfertype) {
            TransferStatus.TransferMakerReview->{
                detail_layout.isVisible = false
                detail_review.isVisible = true

                review_from.title = sharedViewModel.getLocalTransferData().selectFromAc.accountNo

                review_valuedate.title = convertStrToDateFormat(sharedViewModel.getLocalTransferData().selectedDate)
                review_remarks.title = sharedViewModel.getLocalTransferData().remarks
                review_reference.title = sharedViewModel.getLocalTransferData().referenceNumber

                var sendMoney = getString(R.string.send_money, sharedViewModel.getLocalTransferData().selectToAnotherCurrency.amount.toString())
                if (sharedViewModel.getLocalTransferData().selectToAnotherCurrency.amount == 0.0) {
                    sendMoney = getString(R.string.send_money, sharedViewModel.getLocalTransferData().selectToAc.amount.toString())
                }
                bt_next.label = sendMoney

                dataBinding.sendCurrencyView.isEnabled = false
//                dataBinding.recipientCurrencyView.setDisableEdited(false)
//                dataBinding.sendCurrencyView.downImage.isVisible = false
//                dataBinding.recipientCurrencyView.downImage.isVisible = false
                dataBinding.tvUsemyrate.isVisible = false
            }
            else -> {
                detail_layout.isVisible = true
                detail_review.isVisible = false

                transferfrom_account.text = sharedViewModel.getLocalTransferData().selectFromAc.name
                transferfrom_account_desp.text = sharedViewModel.getLocalTransferData().selectFromAc.description

                updateValueDate(sharedViewModel.getLocalTransferData().selectedDate)
                tf_remarks.text = sharedViewModel.getLocalTransferData().remarks
                tf_reference_number.text = sharedViewModel.getLocalTransferData().referenceNumber

//                dataBinding.sendCurrencyView.downImage.isVisible = true
//                dataBinding.recipientCurrencyView.downImage.isVisible = true
                bt_next.label = getString(R.string.next_text)
            }
        }
    }

    private fun reSizeScrollView(scrollView:View, topView: View) {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)

        val headerHeight = resources.getDimension(R.dimen.ras_components_bb_spacing_normal)
        val height = displayMetrics.heightPixels - headerHeight + getStatusBarHeight(requireActivity())
        val layoutParams = scrollView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.height = height.toInt()
        scrollView.layoutParams = layoutParams
    }

    private fun setCurrencyView(view: AmountWithCurrencyTextView, currency: String) {
        var id = context?.let { getFlagResouce(currency, it) }
        view.visibility = View.VISIBLE
//        view.currencyCode = currency
//        view.flagImage = id?:0
        view.requestLayout()
    }

    private fun setShowAndHideSkeletonView(showhide : Boolean){
        if (showhide) {
            dataBinding.topShimmer.startShimmer()
            dataBinding.amountbobyViewShimmer.isVisible = true
            dataBinding.amountbobyView.isVisible = false
        } else {
            dataBinding.topShimmer.stopShimmer()
            dataBinding.amountbobyViewShimmer.isVisible = false
            dataBinding.amountbobyView.isVisible = true
            dataBinding.secondCurrencyLine.isVisible = true
            setIndicateRate()
        }
    }

    private fun setIndicateRate(){
        dataBinding.rlIndicative.isVisible = true
        var value = " 1 " + sharedViewModel.getLocalTransferData().selectToAnotherCurrency.currency +
                " = " +
                sharedViewModel.getLocalTransferData().fxContract?.indicativeRateText + " " +
                sharedViewModel.getLocalTransferData().fxContract?.symbol

        dataBinding.tvIndicativeRate.text = value
        dataBinding.tvUsemyrate.setOnClickListener {
            openFxContractListView()
        }
    }

    private fun setFxRate(contract: FxOtherRate?){
        dataBinding.rlIndicative.isVisible = false
        dataBinding.rlFxRate.isVisible = true

        var value = " 1 " + contract?.toCurrency +
                " = " +
                contract?.rateTier1+ " " +
                contract?.fromCurrency

        dataBinding.tvfxrate.text = value
        dataBinding.tvContractNo.text = contract?.contractNo
        dataBinding.tvEditfxrate.setOnClickListener {
            openFxContractListView()
        }
    }

    private fun openFxContractListView() {
        config.openFxContractListView(
            requireContext(),
            sharedViewModel.getLocalTransferData().ownAccountsList,
            sharedViewModel.getLocalTransferData().selectFromAc,
            selectedBVFxOtherRate,
            sharedViewModel.getLocalTransferData().selectToAc.amount.toString(),
            sharedViewModel.getLocalTransferData().selectToAnotherCurrency.currency,
            true,
            sharedViewModel.getLocalTransferData().fucntionCode,
            "Select",
            object : OnIFTFxContractListener {
                override fun onIFTSelectFxContract(data: FxOtherRate?, isManual: Boolean) {
                    selectedBVFxOtherRate = data
                    setFxRate(data)
                    data?.let {
                        sharedViewModel.getLocalTransferData().fxContract = FxContract(
                            fxRate = data.rateTier1?.toFloat(),
                            fxDate = data.dateMaturity,
                            conversionType = "DEBIT",
                            rfqTimestampString = data.dateMaturity,
                            indicativeAmount = data.originalBuyAmount?.toFloat(),
                            dealHubRate = false,
                            fxRateTypeCode = "B",
                            indicativeRateText = data.outstandingBuyAmount,
                            symbol = data.toCurrency,
                            contractBuyCurrency = ""
                        )
                    }
                }
            })
    }

    override fun selectAccountItem(type: TransferStatus, item: AccountItemModel) {
        updateSelectAccountItem(type, item)
        transferfrom_account.text = item.accountNo + item.currency
        transferfrom_account_desp.text = item.name
    }

    override fun showErrorMessage(error: String?) {
        TODO("Not yet implemented")
    }

    override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {
    }

    override fun onTransitionChange(
        motionLayout: MotionLayout?,
        startId: Int,
        endId: Int,
        progress: Float
    ) {
        if (motionLayout == null)
            return

        if (progress < 0.5f) {
            dataBinding.headerTitleEnd.text = ""
            dataBinding.headerSendBankEnd.text = ""
            dataBinding.headerSendAmountEnd.text = ""
        } else {
            val selectFromAc = sharedViewModel.getLocalTransferData().selectFromAc
            dataBinding.headerTitleEnd.text = selectFromAc.name
            dataBinding.headerSendBankEnd.text = selectFromAc.accountNo
            dataBinding.headerSendAmountEnd.text = "${selectFromAc.amount} ${selectFromAc.currency}"
        }
    }

    override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
        when (currentId) {
            endState -> {
                dataBinding.motionLayout.transitionToEnd()
            }
            startState -> {
                dataBinding.motionLayout.transitionToStart()
            }
        }
    }

    override fun onTransitionTrigger(motionLayout: MotionLayout?, triggerId: Int, positive: Boolean, progress: Float) {
    }



}
