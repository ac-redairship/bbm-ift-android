package com.redairship.ocbc.transfer.presentation.local

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.InputFilter
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.ocbc.transfer.*
import com.ocbc.transfer.databinding.FragmentLocalTransferToMyaccountsBinding
import com.redairship.ocbc.bb.components.extensions.toEditable
import com.redairship.ocbc.bb.components.models.Amount
import com.redairship.ocbc.transfer.*
import com.redairship.ocbc.transfer.model.AccountItemModel
import com.redairship.ocbc.transfer.model.FxOtherRate
import com.redairship.ocbc.transfer.model.TransactionPreSubmitType
import com.redairship.ocbc.transfer.presentation.SameDayTransferDialog
import com.redairship.ocbc.transfer.presentation.base.*
import com.redairship.ocbc.transfer.presentation.common.*
import com.redairship.ocbc.transfer.presentation.doAfterTextChanged
import com.redairship.ocbc.transfer.presentation.transfer.transferfrom.TransferFromAccountListBottomSheet
import com.redairship.ocbc.transfer.presentation.transfer.local.InternalFundsTransferFragmentDirections
import kotlinx.android.synthetic.main.transferto_myaccounts_detail.*
import kotlinx.android.synthetic.main.transferto_myaccounts_detail.view.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.Serializable
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*


interface OnIFTFxContractListener : Serializable {
    fun onIFTSelectFxContract(contract: FxOtherRate?, isManual: Boolean)
}

class TransferToMyAccountsFragment :
    BaseTransferFragment<FragmentLocalTransferToMyaccountsBinding>(),
    AccountListBottomInterface,
    MotionLayout.TransitionListener {
    private var hasSelectedADate: Boolean = false

    //    val config by inject<LocalTransferConfig>()
    var endState: Int? = null
    var startState: Int? = null
    var selectedBVFxOtherRate: FxOtherRate? = null

    val symbols = DecimalFormatSymbols(Locale("en", "SG"))
    val pattern = "#,###.00"
    val decimalFormat = DecimalFormat(pattern, symbols).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = FragmentLocalTransferToMyaccountsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onBindView() {
        with(binding) {
//            endState = motionLayout.endState
//            startState = motionLayout.startState
//        binding.motionLayout?.transitionToEnd()
            Handler().postDelayed({
                motionLayout.apply {
                    reSizeScrollView(body_detail_lly, headerLlyEnd)
                    setTransitionListener(this@TransferToMyAccountsFragment)
                    sendCurrencyView.binding.tvCurrency.requestFocus()
                    vScroll.isVisible = false
                }
            }, 50)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (localTransferData.selectedDate.isEmpty()) {
            sharedViewModel.updateLocalTransferData(localTransferData.copy(
                selectedDate = Date().time.toString()
            ))
        }

        super.onViewCreated(view, savedInstanceState)
        sharedViewModel.getProductTransactionLimit(localTransferData.selectedDate)
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            binding.sendCurrencyView.binding.tvAmount.run {
                requestFocus()
                showKeyboard()
                setSelection(text?.length ?: 0)
            }
        }
    }

    private fun View.showKeyboard() {
        requestFocus()
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.run {
            showSoftInput(this@showKeyboard, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }
    }

    override fun setupViewEvents() {

        binding.headerLlyStart.setOnClickListener{
            backStack()
        }

        binding.sendCurrencyView.binding.tvAmount.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                val inputMethodManager = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val transition: Transition = Slide(Gravity.BOTTOM)
            transition.interpolator = DecelerateInterpolator()
            transition.addTarget(binding.vScroll.v_transferto_myaccounts_detail_root)
            transition.duration = 600
            TransitionManager.beginDelayedTransition(
                binding.motionLayout as ViewGroup,
                transition
            )

            if (!binding.vScroll.v_transferto_myaccounts_detail_root.isVisible) binding.vScroll.v_transferto_myaccounts_detail_root.isVisible = !isKeyboardVisible
            insets
        }

        var isUpdating = false // flag to indicate whether text is being updated programmatically
        val selectToAc = localTransferData.selectToAc

        binding.sendCurrencyView.apply {
            val currency = selectToAc.currency
            setCurrencyView(this, currency.currencyCode)
            var previousValidInput: CharSequence = ""
            val inputFilters = arrayOf(
                InputFilter { source, _, _, dest, dstart, dend ->
                    val hasFractions = false // selectToAc.amount.currency.defaultFractionDigits > 0
                    val decimalFormat = getDecimalFormat(hasFractions, selectToAc.amount.locale)
                    source?.let {
                        if (hasFractions) {
                            val cents = it.split(".").getOrNull(1)
                            if (cents == null || cents.length >= 3) {
                                return@let source
                            }
                        }
                        val filteredInput = it.toString().replace(",", "") // Remove commas from input
                            .filter { c -> c.isDigit() }
                        if (filteredInput.length <= 12) {
                            val output = dest?.removeRange(dstart, dend)?.replaceRange(dstart, dstart, filteredInput)
                            (output?.let {
                                if (it.length > 12) {
                                    decimalFormat.format(it.substring(0, 12)) // Truncate the output if it's too long
                                } else {
                                    source
                                }
                            } ?: filteredInput) // Accept the filtered input if there's no destination
                                .also {
                                    previousValidInput = it
                                }
                        } else {
                            previousValidInput
                        }
                    }
                }
            )
            binding.tvAmount.filters = inputFilters
            binding.tvAmount.doAfterTextChanged {
                val hasFractions = selectToAc.amount.currency.defaultFractionDigits > 0
                if (it.isNullOrBlank()) {
                    binding.tvAmount.text = "0".toEditable()
                    binding.tvAmount.setSelection(binding.tvAmount.text!!.length) // move the cursor to the end
                    sharedViewModel.updateLocalTransferData(
                        localTransferData.copy(
                            selectToAc = selectToAc.copy(
                                amount = selectToAc.amount.copy(value = BigDecimal.ZERO)
                            )
                        )
                    )
                    return@doAfterTextChanged
                }

                if (!isUpdating) {
                    isUpdating = true
                    val input = it.toString().replace("[^\\d]".toRegex(), "")
                    sharedViewModel.updateLocalTransferData(
                        localTransferData.copy(
                            selectToAc = selectToAc.copy(
                                amount = selectToAc.amount.copy(value = input.toBigDecimal())
                            )
                        )
                    )
                    val decimalFormat = getDecimalFormat(hasFractions, selectToAc.amount.locale)
                    if (localTransferData.defaultMinLimit.toBigDecimal() > input.toBigDecimal()) {
                        this@TransferToMyAccountsFragment.binding.tvAmountError.text = "Please enter amount more than ${decimalFormat.format(localTransferData.defaultMinLimit)} SGD"
                        this@TransferToMyAccountsFragment.binding.tvAmountError.isVisible = true
                    } else if (localTransferData.defaultMaxLimit.toBigDecimal() < input.toBigDecimal()) {
                        this@TransferToMyAccountsFragment.binding.tvAmountError.text = "Please enter amount less than ${decimalFormat.format(localTransferData.defaultMaxLimit)} SGD"
                        this@TransferToMyAccountsFragment.binding.tvAmountError.isVisible = true
                    } else {
                        this@TransferToMyAccountsFragment.binding.tvAmountError.isVisible = false
                    }

                    if (hasFractions && input.length >= 3) {
                        val cents = input.substring(input.length - 2) // get the last two digits
                        val dollars = input.substring(0, input.length - 2) // get the rest of the digits
                        val formattedDecimal: String = decimalFormat.format(dollars.toLong())

                        binding.tvAmount.text = "$formattedDecimal.$cents".toEditable()
                    }

                    if (!hasFractions) {
                        val dollars = input
                        val formattedDecimal: String = decimalFormat.format(dollars.toLong())
                        binding.tvAmount.text = "$formattedDecimal".toEditable()
                    }

                    binding.tvAmount.setSelection(binding.tvAmount.text!!.length) // move the cursor to the end
                    isUpdating = false
                }

            }

            this.binding.tvCurrency.setOnClickListener {
                openCurrencyListView(currency.currencyCode)
            }
            this.binding.tvAmount.setOnEditorActionListener { textView, i, keyEvent ->
                clearFocus()
                false
            }
        }
        tf_remarks.apply {
            helperText = "0 / 140"
            binding.etInput.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(140))
            doAfterTextChanged {
                sharedViewModel.updateLocalTransferData(
                    localTransferData.copy(
                        remarks = it
                    )
                )
                helperText = "${it.length} / 140"
            }
        }
        tf_reference_number.apply {
            helperText = "0 / 16"
            binding.etInput.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(16))
            doAfterTextChanged {
                sharedViewModel.updateLocalTransferData(
                    localTransferData.copy(
                        referenceNumber = it
                    )
                )
                helperText = "${it.length} / 16"
            }
        }
        transferfrom_change.setOnClickListener {
            TransferFromAccountListBottomSheet.newInstance(
                TransferStatus.TransferFrom,
                this@TransferToMyAccountsFragment
            ).show(childFragmentManager, null)
        }

        tvValueDate.apply {
            text = convertStrToDateFormat(localTransferData.selectedDate)
            setOnClickListener {
                openScrollCalendar()
            }
        }

//        lly_addaspayee.setOnClickListener {
//            localTransferData.addAspayee =
//                !localTransferData.addAspayee
//            chk_addaspayee.setImageResource(
//                if (localTransferData.addAspayee) {
//                    R.drawable.ras_passportauth_ic_checkbox_checked
//                } else {
//                    R.drawable.ras_passportauth_ic_checkbox_unchecked
//                }
//            )
//        }

        bt_next.setOnClickListener {
            when (transferStatus) {
                TransferStatus.TransferMakerReview -> {
                    openProcessingBottomView()
                }
                else -> {
                    sharedViewModel.doPreSubmit(TransactionPreSubmitType.PREVIEW)
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
                                when (it.throwable) {
                                    is InsufficientBalanceException -> {
                                        openInsufficientBalanceView()
                                    }
                                    is AcceptSameDayTransferFee -> {
                                        if (hasSelectedADate) {
                                            transferStatus = TransferStatus.TransferMakerReview
                                            setViewData(TransferStatus.TransferMakerReview)
                                            return@collectLatest
                                        }
                                        SameDayTransferDialog.newInstance(object : SameDayTransferDialog.ResponseListener {
                                            override fun dismiss() {

                                            }

                                            override fun response(result: SameDayTransferDialog.SameDayTransferResult) {
                                                bt_next.isLoading = false
                                                when (result) {
                                                    is SameDayTransferDialog.SameDayTransferResult.Proceed -> {
                                                        transferStatus = TransferStatus.TransferMakerReview
                                                        setViewData(TransferStatus.TransferMakerReview)
                                                    }
                                                    is SameDayTransferDialog.SameDayTransferResult.Cancel -> {
                                                        openScrollCalendar()
                                                    }
                                                }

                                            }

                                        }).show(childFragmentManager, "SameDayTransferDialog")
                                    }
                                    is UnexpectedException,
                                    is NetworkException -> {
                                        (activity as TransferActivity).showGenericServerErrorScreen(
                                            it.throwable.message
                                        )
                                    }
                                }
                            }
                        }
                    }
            }
        }

        launchWhenCreated {
            launch {
                sharedViewModel.checkValueDate
                    .collectLatest {
                        if (it is UiState.Success) {
                            sharedViewModel.updateLocalTransferData(
                                localTransferData.copy(
                                    selectedDate = it.data.nextValueDate
                                )
                            )
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
                                val buyCurrency = Currency.getInstance(it.data.contractBuyCurrency)
                                sharedViewModel.getProductTransactionLimit(localTransferData.selectedDate)
                                sharedViewModel.updateLocalTransferData(
                                    localTransferData.copy(
                                        selectToAnotherCurrency = localTransferData.selectToAnotherCurrency.copy(
                                            currency = buyCurrency
                                        ),
                                        fxContract = it.data
                                    )
                                )
                                setShowAndHideSkeletonView(false)
                                binding.recipientCurrencyView.apply {
                                    amount = Amount(
                                        value = it.data.indicativeAmount?.toBigDecimal() ?: BigDecimal.ZERO,
                                        symbol = buyCurrency.symbol,
                                        locale = Locale.ENGLISH,
                                        currency = buyCurrency
                                    )
//                                    amount.setText(it.data.indicativeAmount.toString())

                                    setCurrencyView(this, buyCurrency.currencyCode)
                                    doAfterTextChanged { editable ->
                                        val selectToAnotherCurrency = localTransferData.selectToAnotherCurrency ?: return@doAfterTextChanged
                                        sharedViewModel.updateLocalTransferData(
                                            localTransferData.copy(
                                                selectToAnotherCurrency = selectToAnotherCurrency.copy(
                                                    amount = selectToAnotherCurrency.amount.copy(
                                                        value = editable.toString().toBigDecimal()
                                                    )
                                                )
                                            )
                                        )
                                    }
                                    this.binding.tvAmount.setOnEditorActionListener { textView, i, keyEvent ->
                                        clearFocus()
                                        false
                                    }
                                    this.binding.tvCurrency.setOnClickListener {
                                        openCurrencyListView(buyCurrency.currencyCode)
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

    private fun getDecimalFormat(hasFractions: Boolean, locale: Locale): DecimalFormat {
        val symbols = DecimalFormatSymbols(locale)
        val pattern = if (hasFractions) "#,##0.##" else "#,##0"
        return DecimalFormat(pattern, symbols)
    }

    private fun openCurrencyListView(currency: String?) {
        CurrencyListBottomSheet(currency).apply {
            onSelectedCurrencyClicked = { currency ->
                setShowAndHideSkeletonView(true)
                sharedViewModel.getCounterFX(currency.code)
            }
        }.show(childFragmentManager, "")
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
        ProcessingBottomSheet().apply {
            onProcessingFinish = {
                findNavController().navigate(InternalFundsTransferFragmentDirections.actiontoconfirm())
            }
        }.show(childFragmentManager, "")
    }

    private fun openScrollCalendar() {
        ScrollCalendarBottomSheet(localTransferData.selectedDate).apply {
            onSelectedDateClicked = { date ->
                hasSelectedADate = date.after(Date())
                val selectedDateString = convertDateToString(date)
                sharedViewModel.updateLocalTransferData(
                    localTransferData.copy(
                        selectedDate = selectedDateString
                    )
                )
                updateValueDate(selectedDateString)
            }
        }.show(childFragmentManager, null)
    }

    private fun updateValueDate(dateString: String) {
        tvValueDate.text = convertStrToDateFormat(dateString)
    }

    override fun setViewData(transfertype: TransferStatus?) {
        binding.accountName.text = localTransferData.selectToAc.displayName
        binding.accountNumber.text = localTransferData.selectToAc.accountNumber

        when (transfertype) {
            TransferStatus.TransferMakerReview -> {
                detail_layout.isVisible = false
                detail_review.isVisible = true

                review_from.description =
                    localTransferData.selectFromAc.accountNumber

                review_valuedate.description =
                    convertStrToDateFormat(localTransferData.selectedDate)
                review_remarks.description = localTransferData.remarks
                review_reference.description = localTransferData.referenceNumber

                var sendMoney = getString(
                    R.string.send_money,
                    localTransferData.selectToAnotherCurrency.amount.value.toString()
                )
                if (localTransferData.selectToAnotherCurrency.amount.value.toDouble() == 0.0) {
                    sendMoney = getString(
                        R.string.send_money,
                        localTransferData.selectToAc.amount.value.toString()
                    )
                }
                bt_next.label = sendMoney

                binding.sendCurrencyView.isEnabled = false
                binding.recipientCurrencyView.binding.tvAmount.isEnabled = false
                binding.sendCurrencyView.downImage = false
                binding.recipientCurrencyView.downImage = false
                binding.tvUsemyrate.isVisible = false
            }
            else -> {
                detail_layout.isVisible = true
                detail_review.isVisible = false

                transferfrom_account.text =
                    localTransferData.selectFromAc?.accountName
                transferfrom_account_desp.text =
                    localTransferData.selectFromAc?.displayName

                updateValueDate(localTransferData.selectedDate)
                tf_remarks.text = localTransferData.remarks
                tf_reference_number.text = localTransferData.referenceNumber

                binding.sendCurrencyView.downImage = true
                binding.recipientCurrencyView.downImage = true

                binding.sendCurrencyView.binding.tvAmountLabel.text = "Amount"
                binding.recipientCurrencyView.binding.tvAmountLabel.text = "Recipient gets"
                bt_next.label = getString(R.string.next_text)
            }
        }
    }

    private fun reSizeScrollView(scrollView: View, topView: View) {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)

        val headerHeight = resources.getDimension(R.dimen.ras_components_bb_spacing_normal)
        val height =
            displayMetrics.heightPixels - headerHeight + getStatusBarHeight(requireActivity())
        val layoutParams = scrollView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.height = height.toInt()
        scrollView.layoutParams = layoutParams
    }

    private fun setCurrencyView(view: AmountWithCurrencyTextView, currency: String) {
        val id = context?.let { getFlagResouce(currency, it) }
        view.visibility = View.VISIBLE
        view.binding.tvCurrency.text = currency
        view.binding.tvCurrency.setCompoundDrawablesRelativeWithIntrinsicBounds(id ?: 0, 0, R.drawable.ras_components_ic_bb_chevron_down_thick_light, 0);
        view.requestLayout()
    }

    private fun setShowAndHideSkeletonView(showhide: Boolean) {
        if (showhide) {
            binding.topShimmer.startShimmer()
            binding.amountbobyViewShimmer.isVisible = true
            binding.amountbobyView.isVisible = false
        } else {
            binding.topShimmer.stopShimmer()
            binding.amountbobyViewShimmer.isVisible = false
            binding.amountbobyView.isVisible = true
            binding.secondCurrencyLine.isVisible = true
            setIndicateRate()
        }
    }

    private fun setIndicateRate() {
        binding.rlIndicative.isVisible = true
        var value =
            "1 " + localTransferData.selectToAnotherCurrency.currency.currencyCode +
                    " = " +
                    localTransferData.fxContract?.fxRate.toString() + " " +
                    localTransferData.selectFromAc.currency.currencyCode

        binding.tvIndicativeRate.text = value
        binding.tvUsemyrate.setOnClickListener {
            childFragmentManager.beginTransaction()
                .add(R.id.v_root, UseMyRateFragment.newInstance(), "UseMyRateFragment")
                .addToBackStack("UseMyRateFragment")
                .commit()
//            openFxContractListView()
        }
    }

    private fun setFxRate(contract: FxOtherRate?) {
        binding.rlIndicative.isVisible = false
        binding.rlFxRate.isVisible = true

        var value = "1 " + contract?.toCurrency +
                " = " +
                contract?.rateTier1 + " " +
                contract?.fromCurrency

        binding.tvfxrate.text = value
        binding.tvContractNo.text = contract?.contractNo
        binding.tvEditfxrate.setOnClickListener {
            openFxContractListView()
        }
    }

    private fun openFxContractListView() {
        val selectFromAc = localTransferData.selectFromAc ?: return
        val selectToAc = localTransferData.selectToAc ?: return
        val selectToAnotherCurrency = localTransferData.selectToAnotherCurrency ?: return
//        config.openFxContractListView(
//            requireContext(),
//            localTransferData.ownAccountsList,
//            selectFromAc,
//            selectedBVFxOtherRate,
//            selectToAc.amount.value.toString(),
//            selectToAnotherCurrency.currency.currencyCode,
//            true,
//            localTransferData.fucntionCode,
//            "Select",
//            object : OnIFTFxContractListener {
//                override fun onIFTSelectFxContract(data: FxOtherRate?, isManual: Boolean) {
//                    selectedBVFxOtherRate = data
//                    setFxRate(data)
//                    data?.let {
//                        localTransferData.fxContract = FxContract(
//                            fxRate = data.rateTier1?.toFloat(),
//                            fxDate = data.dateMaturity,
//                            conversionType = "DEBIT",
//                            rfqTimestampString = data.dateMaturity,
//                            indicativeAmount = data.originalBuyAmount?.toFloat(),
//                            dealHubRate = false,
//                            fxRateTypeCode = "B",
//                            indicativeRateText = data.outstandingBuyAmount,
//                            symbol = data.toCurrency,
//                            contractBuyCurrency = ""
//                        )
//                    }
//                }
//            })
    }

    override fun selectAccountItem(type: TransferStatus, item: AccountItemModel) {
        updateSelectAccountItem(type, item)
        transferfrom_account.text = item.accountNumber + item.currency
        transferfrom_account_desp.text = item.accountName
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
            binding.headerTitleEnd.text = ""
            binding.headerSendBankEnd.text = ""
            binding.headerSendAmountEnd.text = ""
        } else {
            val selectFromAc = localTransferData.selectFromAc ?: return
            binding.headerTitleEnd.text = selectFromAc.accountName
            binding.headerSendBankEnd.text = selectFromAc.accountNumber
            binding.headerSendAmountEnd.text = "${selectFromAc.amount.value} ${selectFromAc.currency}"
        }
    }

    override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
        when (currentId) {
            endState -> {
                binding.motionLayout.transitionToEnd()
            }
            startState -> {
                binding.motionLayout.transitionToStart()
            }
        }
    }

    override fun onTransitionTrigger(
        motionLayout: MotionLayout?,
        triggerId: Int,
        positive: Boolean,
        progress: Float
    ) {
    }
}
