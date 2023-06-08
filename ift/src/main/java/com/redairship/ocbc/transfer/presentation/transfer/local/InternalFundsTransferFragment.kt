package com.redairship.ocbc.transfer.presentation.transfer.local

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.ocbc.transfer.*
import com.ocbc.transfer.databinding.BottomSheetInternalFundsTransferDetailsBinding
import com.ocbc.transfer.databinding.FragmentInternalFundsTransferBinding
import com.ocbc.transfer.databinding.LayoutInternalTransferAmountsBinding
import com.redairship.ocbc.bb.components.extensions.toEditable
import com.redairship.ocbc.bb.components.extensions.toPx
import com.redairship.ocbc.bb.components.models.Amount
import com.redairship.ocbc.bb.components.views.bottomsheet.BBBottomSheet
import com.redairship.ocbc.bb.components.views.fragments.BaseFragment
import com.redairship.ocbc.bb.components.views.textviews.BBOpenSansEditText
import com.redairship.ocbc.onetokenoffline.features.offlinemode.OneTokenPinFragment
import com.redairship.ocbc.onetokenoffline.offlinemode.OneTokenPinViewModel
import com.redairship.ocbc.transfer.AcceptSameDayTransferFee
import com.redairship.ocbc.transfer.InsufficientBalanceException
import com.redairship.ocbc.transfer.LocalTransferData
import com.redairship.ocbc.transfer.NetworkException
import com.redairship.ocbc.transfer.UiState
import com.redairship.ocbc.transfer.UnexpectedException
import com.redairship.ocbc.transfer.model.AccountItemModel
import com.redairship.ocbc.transfer.model.FxContract
import com.redairship.ocbc.transfer.model.FxOtherRate
import com.redairship.ocbc.transfer.model.TransactionPreSubmitType
import com.redairship.ocbc.transfer.presentation.SameDayTransferDialog
import com.redairship.ocbc.transfer.presentation.base.*
import com.redairship.ocbc.transfer.presentation.common.*
import com.redairship.ocbc.transfer.presentation.doAfterTextChanged
import com.redairship.ocbc.transfer.presentation.local.LocalTransferViewModel
import com.redairship.ocbc.transfer.presentation.local.TransferActivity
import com.redairship.ocbc.transfer.presentation.transfer.local.otp.EmailOTPFragment
import com.redairship.ocbc.transfer.presentation.transfer.local.otp.OTPViewModel
import com.redairship.ocbc.transfer.presentation.transfer.transferfrom.TransferFromAccountListBottomSheet
import com.redairship.onetokenoffline.domain.models.GenerateOtpFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*


interface OnIFTFxContractListener {
    fun onIFTSelectFxContract(contract: FxOtherRate?, isManual: Boolean)
}

class InternalFundsTransferFragment : BaseFragment(), AccountListBottomInterface {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var binding: FragmentInternalFundsTransferBinding
    private lateinit var amountsBinding: LayoutInternalTransferAmountsBinding
    private lateinit var detailsBinding: BottomSheetInternalFundsTransferDetailsBinding
    private var hasSelectedADate: Boolean = false
    private var transferStatus: TransferStatus? = null
    private var isKeyboardVisible: Boolean = false
    private var isRecipientViewSetUp: Boolean = false
    val sharedViewModel: LocalTransferViewModel by activityViewModels()
    val oneTokenPinViewModel: OneTokenPinViewModel by sharedViewModel()
    val emailOtpViewModel: OTPViewModel by sharedViewModel()

    private lateinit var localTransferData: LocalTransferData

    private var exchangeRate = 1f

    var insufficientDialog: BBBottomSheet? = null
    private val oneTokenPinFragment by lazy {
        OneTokenPinFragment.newInstance(flow = GenerateOtpFlow.ApproveRequest)
    }
    private val otpFragment by lazy {
        EmailOTPFragment.newInstance(
            header = "Email Verification Code",
            subHeader = "Enter the code we sent to\n" +
                    " vin*****n@o**c.com.",
            resendLabel = "Send a new OTP in ",
            didNotReceiveLabel = "",
            timeLimit = 10,
            maxLength = 6
        )
    }

    private val processingBottomSheet by lazy { ProcessingBottomSheet.newInstance() }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInternalFundsTransferBinding.inflate(inflater, container, false)
        amountsBinding = binding.body.vAmounts
        detailsBinding = binding.body.vDetails
        bottomSheetBehavior = from(binding.body.vDetails.root)

        return binding.root
    }

    private fun setPeekHeight() {
        val additionalPadding = if (!amountsBinding.tvAmountError.isVisible) 24.toPx else 0
        if (amountsBinding.recipientCurrencyView.isVisible) {
            bottomSheetBehavior.peekHeight =
                (binding.body.root.measuredHeight - (amountsBinding.rlIndicative.bottom)) // set the peek height to 200 pixels
        } else {
            if (amountsBinding.amountBodyViewShimmer.isVisible) {
                bottomSheetBehavior.peekHeight = 375.toPx
            } else {
                bottomSheetBehavior.peekHeight =
                    (binding.body.root.measuredHeight - (amountsBinding.tvAmountError.bottom - additionalPadding)) // set the peek height to 200 pixels
            }


        }

        bottomSheetBehavior.state = STATE_COLLAPSED
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            setupEvents()
            setupViews()
            setupViewEvents()
            setViewData(transferStatus)
        }
    }

    private fun setupEvents() {
        sharedViewModel.localTransferData.value?.let {
            localTransferData = it
        }
        sharedViewModel.localTransferData.observe(viewLifecycleOwner) {
            localTransferData = it
            setIndicativeRate()
        }
    }

    private fun setupViews() {
        detailsBinding.btNext.isEnabled = false
        amountsBinding.accountName.text = localTransferData.recipientAccountData.displayName
        amountsBinding.accountNumber.text =
            "${localTransferData.recipientAccountData.accountNumber} - OCBC Bank"

        binding.tvCompactHeaderTitle.text =
            localTransferData.recipientAccountData.accountName
        binding.tvCompactHeaderSub.text =
            localTransferData.recipientAccountData.accountNumber
        binding.tvCompactHeaderDesc.text = "1,450.00 SGD"
    }

    private fun EditText.showKeyboard() {
        requestFocus()
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.run {
            showSoftInput(this@showKeyboard, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }
    }

    private fun setupViewEvents() {
        binding.root.setOnApplyWindowInsetsListener { _, windowInsets ->
            if (!amountsBinding.sendCurrencyView.binding.tvAmount.isFocused && !amountsBinding.recipientCurrencyView.binding.tvAmount.isFocused) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    isKeyboardVisible = windowInsets.isVisible(WindowInsetsCompat.Type.ime())
                    if (isKeyboardVisible) bottomSheetBehavior.state = STATE_EXPANDED
                    val imeHeight = windowInsets.getInsets(WindowInsets.Type.ime()).bottom
                    binding.root.setPadding(
                        0,
                        requireView().paddingTop + getStatusBarHeight(),
                        0,
                        imeHeight
                    )
                }
            }
            windowInsets
        }

        setPeekHeight()

        with(amountsBinding.sendCurrencyView.binding.tvAmount) {
            showKeyboard()
            setSelection(text?.length ?: 0)
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    STATE_HIDDEN -> {}
                    STATE_COLLAPSED -> {
                        if (isKeyboardVisible) return
                        binding.tvCompactHeaderTitle.alpha = 0f
                        binding.tvCompactHeaderSub.alpha = 0f
                        binding.tvCompactHeaderDesc.alpha = 0f
                        amountsBinding.root.alpha = 1f
                    }

                    STATE_EXPANDED -> {
                        binding.tvCompactHeaderTitle.alpha = 1f
                        binding.tvCompactHeaderSub.alpha = 1f
                        binding.tvCompactHeaderDesc.alpha = 1f
                        amountsBinding.root.alpha = 0f
                    }

                    STATE_HALF_EXPANDED -> {}
                    STATE_DRAGGING -> {}
                    STATE_SETTLING -> {}
                    else -> {}
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (bottomSheetBehavior.state != STATE_EXPANDED && slideOffset != 0f) {
                    binding.tvCompactHeaderTitle.alpha = slideOffset
                    binding.tvCompactHeaderSub.alpha = slideOffset
                    binding.tvCompactHeaderDesc.alpha = slideOffset
                    amountsBinding.root.alpha = 1f - slideOffset
                }
            }

        })

        binding.ivStartIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        amountsBinding.sendCurrencyView.binding.tvAmount.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.clearFocus()
                true
            } else {
                false
            }
        }

        amountsBinding.sendCurrencyView.binding.tvAmount.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                val inputMethodManager =
                    requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            }

            amountsBinding.vFocusIndicator.isVisible = isFocused

            view.postDelayed({
                if (!binding.body.vDetails.root.isVisible) {
                    binding.body.vDetails.root.isVisible = !isFocused
                }
            }, 0)

        }


        var recipientAccountData = localTransferData.recipientAccountData
        var senderAccountData = localTransferData.senderAccountData
        with(amountsBinding.sendCurrencyView) {
            val currency = recipientAccountData.currency
            amountsBinding.sendCurrencyView.post {
                setCurrencyView(
                    amountsBinding.sendCurrencyView,
                    currency.currencyCode
                )
            }

            binding.tvAmount.filters = getInputFilters(recipientAccountData)

            var isUpdating = false // flag to indicate whether text is being updated programmatically
            binding.tvAmount.doAfterTextChanged {
                recipientAccountData = localTransferData.recipientAccountData
                senderAccountData = localTransferData.senderAccountData
                handleAmountTextChange(binding.tvAmount, it, senderAccountData, isUpdating, {
                    isUpdating = true
                }, { value ->
                    val decimalFormat = getDecimalFormat(senderAccountData.currency.defaultFractionDigits > 0, recipientAccountData.amount.locale)
                    val exchangedValue = value.movePointLeft(senderAccountData.currency.defaultFractionDigits) * exchangeRate.toBigDecimal()
                    if (binding.tvAmount.isFocused) {
                        amountsBinding.recipientCurrencyView.binding.tvAmount.setText(decimalFormat.format(exchangedValue))
                    }

                    if (localTransferData.defaultMinLimit.toBigDecimal() > value) {
                        amountsBinding.tvAmountError.text = "Please enter amount more than ${
                            decimalFormat.format(
                                localTransferData.defaultMinLimit.toBigDecimal().movePointLeft(2)
                            )
                        } SGD"
                    } else if (localTransferData.defaultMaxLimit.toBigDecimal() < value) {
                        amountsBinding.tvAmountError.text = "Please enter amount less than ${
                            decimalFormat.format(
                                localTransferData.defaultMaxLimit.toBigDecimal().movePointLeft(2)
                            )
                        } SGD"
                    }

                    when (sharedViewModel.responseFlow) {
                        LocalTransferViewModel.ResponseFlow.SENDER -> {
                            sharedViewModel.updateLocalTransferData(
                                localTransferData.copy(
                                    senderAccountData = senderAccountData.copy(
                                        amount = amount.copy(
                                            value = value
                                        )
                                    )
                                )
                            )
                        }
                        LocalTransferViewModel.ResponseFlow.RECIPIENT -> {
                            sharedViewModel.updateLocalTransferData(
                                localTransferData.copy(
                                    recipientAccountData = recipientAccountData.copy(
                                        amount = amount.copy(
                                            value = value
                                        )
                                    )
                                )
                            )
                        }
                    }

                    this@InternalFundsTransferFragment.binding.tvCompactHeaderDesc.text = "${binding.tvAmount.text} ${recipientAccountData.amount.currency.currencyCode}"
                })
                isUpdating = false
            }

            this.binding.tvCurrency.setOnClickListener {
                openCurrencyListView(true, currency.currencyCode)
            }
        }

        with(amountsBinding.recipientCurrencyView) {
            var isUpdating = false

            binding.tvAmount.setOnEditorActionListener { textView, i, keyEvent ->
                clearFocus()
                false
            }
            binding.tvCurrency.setOnClickListener {
                openCurrencyListView(false, null)
            }

            binding.tvAmount.doAfterTextChanged {
                val selectToAnotherCurrency = localTransferData.recipientAccountData
                handleAmountTextChange(binding.tvAmount, it, selectToAnotherCurrency, isUpdating, {
                    isUpdating = true
                }, { value ->
                    val decimalFormat = getDecimalFormat(selectToAnotherCurrency.currency.defaultFractionDigits > 0, recipientAccountData.amount.locale)
                    val exchangedValue = value.movePointLeft(selectToAnotherCurrency.currency.defaultFractionDigits) / exchangeRate.toBigDecimal()
                    if (binding.tvAmount.isFocused) {
                        amountsBinding.sendCurrencyView.binding.tvAmount.setText(decimalFormat.format(exchangedValue))
                    }

                    sharedViewModel.updateLocalTransferData(
                        localTransferData.copy(
                            recipientAccountData = selectToAnotherCurrency.copy(
                                amount = amount.copy(
                                    value = value
                                )
                            )
                        )
                    )
                })
                isUpdating = false
            }
        }


        detailsBinding.tfRemarks.apply {
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
        detailsBinding.tfReferenceNumber.apply {
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
        detailsBinding.transferfromChange.setOnClickListener {
            TransferFromAccountListBottomSheet.newInstance(
                TransferStatus.TransferFrom,
                this@InternalFundsTransferFragment
            ).show(childFragmentManager, null)
        }

        detailsBinding.tvValueDate.apply {
            text = convertStrToDateFormat(localTransferData.selectedDate)
            setOnClickListener {
                openScrollCalendar()
            }
        }

        detailsBinding.btNext.setOnClickListener {
            sharedViewModel.doPreSubmit(TransactionPreSubmitType.PREVIEW)
        }

        amountsBinding.tvUseMyRate.setOnClickListener {
            sharedViewModel.goToUseMyRate()
        }

        amountsBinding.tvEditFxRate.setOnClickListener {
            sharedViewModel.goToUseMyRate()
        }

        handleFxContract()
        handlePresubmit()
        handleTransactionLimit()
        handleOneTokenPin()
        handleEmailVerification()
    }

    private fun handleEmailVerification() {
        emailOtpViewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            when (uiState) {
                is OTPViewModel.OtpUiState.Error -> {
                    otpFragment.binding.btSubmit.isLoading = false
                    otpFragment.binding.pinEntryView.text = ""

                    val remainingAttempts = uiState.remainingAttempts
                    otpFragment.setAttempts(remainingAttempts)
                    if (remainingAttempts <= 0) {
                        return@observe
                    }
                }

                is OTPViewModel.OtpUiState.Success -> {
                    openProcessingBottomView()
                }

                else -> {
                    oneTokenPinFragment.binding.btSubmit.isLoading = false
                }
            }
        }
    }


    private fun handleOneTokenPin() {
//        oneTokenPinViewModel.uiState.observe(viewLifecycleOwner) { uiState ->
//            when (uiState) {
//                is OneTokenPinViewModel.OtpUiState.Error -> {
//
//                }
//
//                is OneTokenPinViewModel.OtpUiState.Success -> {
//                    oneTokenPinFragment.binding.pinEntryView.text = ""
//                    openOtpFragment()
//                }
//
//                else -> {
//                    oneTokenPinFragment.binding.btSubmit.isLoading = false
//                }
//            }
//        }
    }

    private fun openOneTokenPin() {
        sharedViewModel.goToOneTokenVerification()
    }

    private fun openOtpFragment() {
        if (oneTokenPinFragment.isAdded) {
            childFragmentManager.popBackStackImmediate()
        }
        sharedViewModel.goToEmailVerification(otpFragment)
    }

    private fun handleAmountTextChange(
        editText: BBOpenSansEditText,
        newText: Editable?,
        account: AccountItemModel,
        isUpdating: Boolean,
        updatingCallback: () -> Unit,
        updatedCallback: (value: BigDecimal) -> Unit
    ) {
        val hasFractions = account.amount.currency.defaultFractionDigits > 0
        if (newText.isNullOrBlank()) {
            editText.text = "0".toEditable()
            editText.setSelection(editText.text!!.length) // move the cursor to the end
            updatedCallback(BigDecimal.ZERO)
            return
        }

        if (!isUpdating) {
            updatingCallback()
            val input = newText.toString().replace("[^\\d]".toRegex(), "")
            val decimalFormat = getDecimalFormat(hasFractions, account.amount.locale)
            if (localTransferData.defaultMinLimit.toBigDecimal() > input.toBigDecimal()) {
                amountsBinding.tvAmountError.isVisible = true
                amountsBinding.vFocusIndicator.setBackgroundColor(Color.parseColor("#EE6A71"))
                detailsBinding.btNext.isEnabled = false
            } else if (localTransferData.defaultMaxLimit.toBigDecimal() < input.toBigDecimal()) {
                amountsBinding.vFocusIndicator.setBackgroundColor(Color.parseColor("#EE6A71"))
                amountsBinding.tvAmountError.isVisible = true
                detailsBinding.btNext.isEnabled = false
            } else {
                amountsBinding.vFocusIndicator.setBackgroundColor(Color.WHITE)
                amountsBinding.tvAmountError.isVisible = false
                detailsBinding.btNext.isEnabled = true
            }

            setPeekHeight()

            if (hasFractions && input.length >= 3) {
                val cents = input.substring(input.length - 2) // get the last two digits
                val dollars = input.substring(0, input.length - 2) // get the rest of the digits
                val formattedDecimal: String = decimalFormat.format(dollars.toLong())

                editText.text = "$formattedDecimal.$cents".toEditable()
            }

            if (!hasFractions) {
                val formattedDecimal: String = decimalFormat.format(input.toLong())
                editText.text = "$formattedDecimal".toEditable()
            }

            updatedCallback(input.toBigDecimal())
            editText.setSelection(editText.text!!.length) // move the cursor to the end
        }
    }

    private fun handleTransactionLimit() = launchWhenCreated {
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

    private fun handlePresubmit() = launchWhenCreated {
        sharedViewModel.insufficientBalanceOutcome
            .collectLatest {
                when (it) {
                    is UiState.Loading -> {
                        detailsBinding.btNext.isLoading = true
                    }

                    is UiState.Success -> {
                        detailsBinding.btNext.isLoading = false
                        transferStatus = TransferStatus.TransferMakerReview

                        setViewData(TransferStatus.TransferMakerReview)
                    }

                    is UiState.Error -> {
                        detailsBinding.btNext.isLoading = false
                        when (it.throwable) {
                            is InsufficientBalanceException -> {
                                if (transferStatus != TransferStatus.TransferMakerReview) openInsufficientBalanceView()
                            }

                            is AcceptSameDayTransferFee -> {
                                if (hasSelectedADate) {
                                    transferStatus = TransferStatus.TransferMakerReview
                                    setViewData(TransferStatus.TransferMakerReview)
                                    return@collectLatest
                                }
                                SameDayTransferDialog.newInstance(object :
                                    SameDayTransferDialog.ResponseListener {
                                    override fun dismiss() {

                                    }

                                    override fun response(result: SameDayTransferDialog.SameDayTransferResult) {
                                        detailsBinding.btNext.isLoading = false
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

    private fun handleFxContract() = launchWhenCreated {
        sharedViewModel.fxContract
            .collectLatest {
                when (it) {
                    is UiState.Success -> {
                        setupExchangeView(sharedViewModel.responseFlow, it)
                        amountsBinding.rlIndicative.isVisible = true
                    }

                    is UiState.Error -> {
                        setShowAndHideSkeletonView(false, sharedViewModel.fxContract.value.data?.isUserRate ?: false)
                    }
                }
            }
    }

    private fun setupExchangeView(responseFlow: LocalTransferViewModel.ResponseFlow, uiState: UiState.Success<FxContract>) {
        val senderAccountData = localTransferData.senderAccountData
        val recipientAccountData = localTransferData.recipientAccountData
        val buyCurrency = Currency.getInstance(uiState.data.contractBuyCurrency)
        sharedViewModel.getProductTransactionLimit(localTransferData.selectedDate)

        if (isRecipientViewSetUp && responseFlow == LocalTransferViewModel.ResponseFlow.SENDER) {
            sharedViewModel.updateLocalTransferData(
                localTransferData.copy(
                    senderAccountData = senderAccountData.copy(
                        currency = buyCurrency
                    ),
                    fxContract = uiState.data
                )
            )
            with(amountsBinding.sendCurrencyView) {
                binding.tvAmount.filters = getInputFilters(senderAccountData)
                exchangeRate = uiState.data.fxRate ?: 1.0f

                amount = Amount(
                    value = BigDecimal.ZERO,
                    symbol = buyCurrency.symbol,
                    locale = Locale.ENGLISH,
                    currency = buyCurrency
                )

                setCurrencyView(this, buyCurrency.currencyCode)

            }
        }

        if (!isRecipientViewSetUp || responseFlow == LocalTransferViewModel.ResponseFlow.RECIPIENT) {
            sharedViewModel.updateLocalTransferData(
                localTransferData.copy(
                    recipientAccountData = localTransferData.recipientAccountData.copy(
                        currency = buyCurrency
                    ),
                    fxContract = uiState.data
                )
            )

            with(amountsBinding.recipientCurrencyView) {
                binding.tvAmount.filters = getInputFilters(recipientAccountData)
                exchangeRate = uiState.data.fxRate ?: 1.0f

                amount = Amount(
                    value = uiState.data.indicativeAmount?.toBigDecimal() ?: BigDecimal.ZERO,
                    symbol = buyCurrency.symbol,
                    locale = Locale.ENGLISH,
                    currency = buyCurrency
                )

                setCurrencyView(this, buyCurrency.currencyCode)

            }
        }


        setShowAndHideSkeletonView(false, uiState.data.isUserRate)
        if (uiState.data.isUserRate) {
            amountsBinding.tvContractValue.text = uiState.data.quoteId
        }
        amountsBinding.sendCurrencyView.binding.tvAmountLabel.text = "You send"

        isRecipientViewSetUp = true
    }

    private fun getInputFilters(account: AccountItemModel): Array<InputFilter>? {
        val hasFractions = account.amount.currency.defaultFractionDigits > 0
        val decimalFormat = getDecimalFormat(hasFractions, account.amount.locale)
        var previousValidInput: CharSequence = ""
        return arrayOf(
            InputFilter.LengthFilter(if (!hasFractions) 15 else 16),
            InputFilter { source, _, _, dest, dstart, dend ->
                filterInput(
                    source,
                    hasFractions,
                    decimalFormat,
                    previousValidInput,
                    dest,
                    dstart,
                    dend
                )?.also {
                    previousValidInput = it
                }
            }
        )
    }

    private fun filterInput(
        source: CharSequence?,
        hasFractions: Boolean,
        decimalFormat: DecimalFormat,
        previousValidInput: CharSequence,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        return source?.let {
            if (hasFractions) {
                val cents = it.split(".").getOrNull(1)
                if (cents == null || cents.length >= 3) {
                    return@let source
                }
            }
            val filteredInput =
                it.toString().replace(",", "") // Remove commas from input
                    .filter { c -> c.isDigit() }
            if (filteredInput.length <= 12) {
                val output = dest?.removeRange(dstart, dend)
                    ?.replaceRange(dstart, dstart, filteredInput)
                (output?.let {
                    if (it.length > 12) {
                        decimalFormat.format(
                            it.substring(
                                0,
                                12
                            )
                        ) // Truncate the output if it's too long
                    } else {
                        source
                    }
                } ?: filteredInput) // Accept the filtered input if there's no destination
            } else {
                previousValidInput
            }
        }
    }

    private fun getDecimalFormat(hasFractions: Boolean, locale: Locale): DecimalFormat {
        val symbols = DecimalFormatSymbols(locale)
        val pattern = if (hasFractions) "#,##0.##" else "#,##0"
        return DecimalFormat(pattern, symbols)
    }

    private fun openCurrencyListView(isSender: Boolean, currency: String?) {
        CurrencyListBottomSheet(currency ?: sharedViewModel.fxContract.value.data?.contractBuyCurrency).apply {
            onSelectedCurrencyClicked = { currency ->
                setShowAndHideSkeletonView(true, sharedViewModel.fxContract.value.data?.isUserRate ?: false)
                sharedViewModel.getCounterFX(isSender, currency.code)
            }
        }.show(childFragmentManager, "CurrencyListBottomSheet")
    }

    private fun openInsufficientBalanceView() {
        insufficientDialog = InsufficientBalanceBottomSheet().apply {
            onChangeAccountClicked = { changeAccountClicked ->
                if (changeAccountClicked) {
                    TransferFromAccountListBottomSheet.newInstance(
                        TransferStatus.TransferFrom,
                        this@InternalFundsTransferFragment
                    ).show(childFragmentManager, "TransferFromAccountListBottomSheet")
                } else {
                    transferStatus = TransferStatus.TransferMakerReview
                    setViewData(TransferStatus.TransferMakerReview)
                }
            }
        }
        insufficientDialog?.show(childFragmentManager, "InsufficientBalanceBottomSheet")
    }

    private fun openProcessingBottomView() {
        Log.d("IFT Frag", "openProcessingBottomView")

        viewLifecycleOwner.lifecycleScope.launch {
            requireActivity().supportFragmentManager.popBackStackImmediate(otpFragment::class.java.simpleName, POP_BACK_STACK_INCLUSIVE)

            processingBottomSheet.apply {
                onProcessingFinish = {
                    if (transferStatus == TransferStatus.TransferMakerReview) {
                        processingBottomSheet.dismiss()
                        sharedViewModel.goToTransactionSummary()
//                        findNavController().navigate(InternalFundsTransferFragmentDirections.actiontoconfirm())
                    }
                }
            }
            processingBottomSheet.show(requireActivity().supportFragmentManager, "processingBottomSheet")
            sharedViewModel.doPreSubmit(TransactionPreSubmitType.SUBMIT)
        }

    }

    private fun openScrollCalendar() {
        ScrollCalendarBottomSheet(localTransferData.selectedDate).apply {
            onSelectedDateClicked = { date ->
                hasSelectedADate = date.after(Date())
                sharedViewModel.updateLocalTransferData(
                    localTransferData.copy(
                        selectedDate = convertDateToString(date)
                    )
                )
                updateValueDate(localTransferData.selectedDate)
            }
        }.show(childFragmentManager, null)
    }

    private fun updateValueDate(dateString: String) {
        detailsBinding.tvValueDate.text = convertStrToDateFormat(dateString)
    }

    fun setViewData(transfertype: TransferStatus?) {
        val recipientAccountData = localTransferData.recipientAccountData

        amountsBinding.accountName.text = recipientAccountData.displayName
        amountsBinding.accountNumber.text = "${recipientAccountData.accountNumber} - OCBC Bank"

        when (transfertype) {
            TransferStatus.TransferMakerReview -> {
                detailsBinding.detailLayout.isVisible = false
                detailsBinding.detailReview.isVisible = true

                detailsBinding.tvReviewAccountDetails.text =
                    "${detailsBinding.transferfromAccountDesp.text} - ${recipientAccountData.accountNumber} ${recipientAccountData.currency}"
                detailsBinding.tvReviewValueDate.text =
                    convertStrToDateFormat(localTransferData.selectedDate)

                if (localTransferData.remarks.isEmpty()) {
                    detailsBinding.tvReviewNoteForPayeeLabel.isVisible = false
                    detailsBinding.tvReviewNoteForPayeeValue.isVisible = false
                } else {
                    detailsBinding.tvReviewNoteForPayeeLabel.isVisible = true
                    detailsBinding.tvReviewNoteForPayeeValue.isVisible = true
                }
                if (localTransferData.referenceNumber.isEmpty()) {
                    detailsBinding.tvReviewReferenceLabel.isVisible = false
                    detailsBinding.tvReviewReferenceValue.isVisible = false
                } else {
                    detailsBinding.tvReviewReferenceLabel.isVisible = true
                    detailsBinding.tvReviewReferenceValue.isVisible = true
                }
                detailsBinding.tvReviewNoteForPayeeValue.text =
                    localTransferData.remarks
                detailsBinding.tvReviewReferenceValue.text =
                    localTransferData.referenceNumber

                var sendMoney = getString(
                    R.string.send_money,
                    localTransferData.recipientAccountData?.amount?.value.toString()
                )
                if (localTransferData.recipientAccountData?.amount?.value?.toDouble() == 0.0) {
                    val hasFractions = recipientAccountData.amount.currency.defaultFractionDigits > 0
                    val decimalFormat = getDecimalFormat(hasFractions, recipientAccountData.amount.locale)

                    sendMoney = getString(
                        R.string.send_money,
                        "${amountsBinding.sendCurrencyView.binding.tvAmount.text} ${recipientAccountData.amount.currency.currencyCode}"
                    )


                }
                detailsBinding.btNext.label = sendMoney
                detailsBinding.btNext.isVisible = false
                binding.body.btReviewNext.isVisible = true
                binding.body.btReviewNext.label = sendMoney
                binding.body.btReviewNext.setOnClickListener {
                    if (localTransferData.senderAccountData.is1MC) {
                        openOneTokenPin()
                    } else {
                        openOtpFragment()
                    }

                }

                amountsBinding.sendCurrencyView.isEnabled = false
                amountsBinding.recipientCurrencyView.binding.tvAmount.isEnabled = false
                amountsBinding.sendCurrencyView.downImage = false
                amountsBinding.recipientCurrencyView.downImage = false
                amountsBinding.tvUseMyRate.isVisible = false
                bottomSheetBehavior.state = STATE_COLLAPSED

            }
            else -> {
                detailsBinding.detailLayout.isVisible = true
                detailsBinding.detailReview.isVisible = false

                detailsBinding.transferfromAccount.text =
                    localTransferData.senderAccountData.accountNumber + " - " + localTransferData.senderAccountData.currency
                detailsBinding.transferfromAccountDesp.text =
                    localTransferData.senderAccountData.accountName

                updateValueDate(localTransferData.selectedDate)
                detailsBinding.tfRemarks.text = localTransferData.remarks
                detailsBinding.tfReferenceNumber.text =
                    localTransferData.referenceNumber

                amountsBinding.sendCurrencyView.downImage = true
                amountsBinding.recipientCurrencyView.downImage = true

                amountsBinding.sendCurrencyView.binding.tvAmountLabel.text = "Amount"
                amountsBinding.recipientCurrencyView.binding.tvAmountLabel.text = "Recipient gets"
                detailsBinding.btNext.label = getString(R.string.next_text)
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
        view.binding.tvCurrency.setCompoundDrawablesRelativeWithIntrinsicBounds(
            id ?: 0,
            0,
            R.drawable.ras_components_ic_bb_chevron_down_thick_light,
            0
        )
        view.requestLayout()
    }

    private fun setShowAndHideSkeletonView(shouldShow: Boolean, userRate: Boolean = false) {
        if (shouldShow) {
            if (amountsBinding.recipientCurrencyView.isVisible) {
                amountsBinding.vIndicativeRateShimmer.startShimmer()
                amountsBinding.vIndicativeRateShimmer.isVisible = true
                amountsBinding.labelIndicativeRate.isVisible = false
                amountsBinding.tvIndicativeRate.isVisible = false
                amountsBinding.tvUseMyRate.isVisible = false
                amountsBinding.tvEditFxRate.isVisible = false
                amountsBinding.tvContractValue.isVisible = false
                amountsBinding.tvContractLabel.isVisible = false
            } else {
                amountsBinding.topShimmer.startShimmer()
                amountsBinding.amountBodyViewShimmer.isVisible = true
                amountsBinding.amountBodyView.isVisible = false
            }

        } else {
            amountsBinding.topShimmer.stopShimmer()
            amountsBinding.amountBodyViewShimmer.isVisible = false
            amountsBinding.amountBodyView.isVisible = true
            amountsBinding.secondCurrencyLine.isVisible = true

            if (amountsBinding.vIndicativeRateShimmer.isVisible) {
                amountsBinding.vIndicativeRateShimmer.stopShimmer()
                amountsBinding.labelIndicativeRate.isVisible = true
                amountsBinding.tvIndicativeRate.isVisible = true
                amountsBinding.vIndicativeRateShimmer.isVisible = false
            }

            if (userRate) {
                amountsBinding.tvEditFxRate.isVisible = true
                amountsBinding.tvUseMyRate.isVisible = false
                amountsBinding.tvContractLabel.isVisible = true
                amountsBinding.tvContractValue.isVisible = true
            } else {
                amountsBinding.tvEditFxRate.isVisible = false
                amountsBinding.tvUseMyRate.isVisible = true
                amountsBinding.tvContractLabel.isVisible = false
                amountsBinding.tvContractValue.isVisible = false
            }
        }

        amountsBinding.amountBodyView.post {
            setPeekHeight()
        }
    }

    private fun setIndicativeRate() {
        val value =
            "1 " + localTransferData.senderAccountData.amount.currency.currencyCode +
                    " = " +
                    localTransferData.fxContract?.fxRate.toString() + " " +
                    localTransferData.recipientAccountData.amount.currency.currencyCode

        amountsBinding.tvIndicativeRate.text = value
    }

    private fun setFxRate(contract: FxOtherRate?) {
        amountsBinding.rlIndicative.isVisible = false
        amountsBinding.rlFxRate.isVisible = true

        var value = "1 " + contract?.toCurrency +
                " = " +
                contract?.rateTier1 + " " +
                contract?.fromCurrency

        amountsBinding.tvfxrate.text = value
        amountsBinding.tvContractNo.text = contract?.contractNo
        amountsBinding.tvEditFxRate.setOnClickListener {
            sharedViewModel.goToUseMyRate()
        }
    }

    private fun openFxContractListView() {

    }

    override fun selectAccountItem(type: TransferStatus, item: AccountItemModel) {
        insufficientDialog?.dismiss()
        updateSelectAccountItem(item)
        detailsBinding.transferfromAccount.text = item.accountNumber + " - " + item.amount.currency.currencyCode
        detailsBinding.transferfromAccountDesp.text = item.accountName
    }

    private fun updateSelectAccountItem(item: AccountItemModel) {
        sharedViewModel.updateLocalTransferData(
            localTransferData.copy(
                senderAccountData = item
            )
        )

        with(amountsBinding.sendCurrencyView) {
            binding.tvAmount.filters = getInputFilters(item)

            amount = Amount(
                value = BigDecimal.ZERO,
                symbol = item.amount.symbol,
                locale = Locale.ENGLISH,
                currency = item.amount.currency
            )

            setCurrencyView(this, item.amount.currency.currencyCode)

        }
    }

    override fun showErrorMessage(error: String?) {

    }

}
