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
import com.redairship.ocbc.bb.components.views.fragments.errors.GenericMessageFragment
import com.redairship.ocbc.bb.components.views.fragments.errors.GenericMessageType
import com.redairship.ocbc.bb.components.views.textviews.BBOpenSansEditText
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
import com.redairship.ocbc.transfer.presentation.transfer.local.otp.OneTokenPinFragment
import com.redairship.ocbc.transfer.presentation.transfer.local.otp.OneTokenPinViewModel
import com.redairship.ocbc.transfer.presentation.transfer.transferfrom.TransferFromAccountListBottomSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*


interface OnIFTFxContractListener2 {
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

    val sharedViewModel: LocalTransferViewModel by activityViewModels()
    val oneTokenPinViewModel: OneTokenPinViewModel by sharedViewModel()
    val emailOtpViewModel: OTPViewModel by sharedViewModel()

    private lateinit var localTransferData: LocalTransferData

    var insufficientDialog: BBBottomSheet? = null
    private val oneTokenPinFragment by lazy {
        OneTokenPinFragment.newInstance(
            header = "OneToken PIN",
            subHeader = "Enter your OneToken PIN.",
            resendLabel = "Forgot PIN?",
            didNotReceiveLabel = "",
            timeLimit = 0,
            maxLength = 6
        )
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
            bottomSheetBehavior.peekHeight =
                (binding.body.root.measuredHeight - (amountsBinding.tvAmountError.bottom - additionalPadding)) // set the peek height to 200 pixels
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
        }
    }

    private fun setupViews() {
        detailsBinding.btNext.isEnabled = false
        amountsBinding.accountName.text = localTransferData.selectToAc.displayName
        amountsBinding.accountNumber.text =
            "${localTransferData.selectToAc.accountNumber} - OCBC Bank"

        binding.tvCompactHeaderTitle.text =
            localTransferData.selectToAc.accountName
        binding.tvCompactHeaderSub.text =
            localTransferData.selectToAc.accountNumber
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


        val selectToAc = localTransferData.selectToAc
        val selectToAnotherCurrency = localTransferData.selectToAnotherCurrency
        with(amountsBinding.sendCurrencyView) {
            val currency = selectToAc.currency
            amountsBinding.sendCurrencyView.post {
                setCurrencyView(
                    amountsBinding.sendCurrencyView,
                    currency.currencyCode
                )
            }

            var previousValidInput: CharSequence = ""
            val inputFilters = arrayOf(
                InputFilter { source, _, _, dest, dstart, dend ->
                    val hasFractions = selectToAc.amount.currency.defaultFractionDigits > 0
                    val decimalFormat = getDecimalFormat(hasFractions, selectToAc.amount.locale)
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

            binding.tvAmount.filters = inputFilters

            var isUpdating =
                false // flag to indicate whether text is being updated programmatically
            binding.tvAmount.doAfterTextChanged {
                handleAmountTextChange(binding.tvAmount, it, selectToAc, isUpdating, {
                    isUpdating = true
                }, { value ->
                    sharedViewModel.updateLocalTransferData(
                        localTransferData.copy(
                            selectToAc = selectToAc.copy(
                                amount = amount.copy(
                                    value = value
                                )
                            )
                        )
                    )
                    this@InternalFundsTransferFragment.binding.tvCompactHeaderDesc.text =
                        "${binding.tvAmount.text} ${selectToAc.amount.currency.currencyCode}"
                })
                isUpdating = false
            }

            this.binding.tvCurrency.setOnClickListener {
                openCurrencyListView(currency.currencyCode)
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
//
//            sharedViewModel.goToOneTokenVerification()
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
//                        viewModel.showGenericErrorScreen(
//                            tag = "exceeded_attempt",
//                            title = getString(R.string.ras_passportauth_otp_max_attempt_error_title),
//                            description = getString(R.string.ras_passportauth_otp_max_attempt_error_description),
//                            buttonText = getString(R.string.ras_passportauth_get_in_touch),
//                            type = GenericMessageType.Warning.id,
//                            interceptDoneAction = true,
//                            listener = this@CompleteApplicationFragment,
//                            closeListener = null
//                        )
                        return@observe
                    }
                }

                is OTPViewModel.OtpUiState.Success -> {
                    otpFragment.binding.pinEntryView.text = ""
                    childFragmentManager.popBackStackImmediate()
                    viewLifecycleOwner.lifecycleScope.launch {
                        openProcessingBottomView()
                    }
                }

                else -> {
                    oneTokenPinFragment.binding.btSubmit.isLoading = false
                }
            }
        }
    }

    private fun handleOneTokenPin() {
        oneTokenPinViewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            when (uiState) {
                is OneTokenPinViewModel.OtpUiState.Error -> {

                }

                is OneTokenPinViewModel.OtpUiState.Success -> {
                    oneTokenPinFragment.binding.pinEntryView.text = ""
                    openOtpFragment()
                }

                else -> {
                    oneTokenPinFragment.binding.btSubmit.isLoading = false
                }
            }
        }
    }

    private fun openOneTokenPin() {
        childFragmentManager.beginTransaction()
            .replace(R.id.v_root, oneTokenPinFragment)
            .addToBackStack("OneTokenPinFragment")
            .commit()
    }

    private fun openOtpFragment() {
        if (oneTokenPinFragment.isAdded) {
            childFragmentManager.popBackStackImmediate()
        }
        childFragmentManager.beginTransaction()
            .replace(R.id.v_root, otpFragment)
            .addToBackStack("OtpFragment")
            .commit()
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
                amountsBinding.tvAmountError.text = "Please enter amount more than ${
                    decimalFormat.format(
                        localTransferData.defaultMinLimit.toBigDecimal()
                            .movePointLeft(2)
                    )
                } SGD"
                amountsBinding.tvAmountError.isVisible = true
                amountsBinding.vFocusIndicator.setBackgroundColor(Color.parseColor("#EE6A71"))
                detailsBinding.btNext.isEnabled = false
            } else if (localTransferData.defaultMaxLimit.toBigDecimal() < input.toBigDecimal()) {
                amountsBinding.tvAmountError.text = "Please enter amount less than ${
                    decimalFormat.format(
                        localTransferData.defaultMaxLimit.toBigDecimal()
                            .movePointLeft(2)
                    )
                } SGD"
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
                val dollars = input
                val formattedDecimal: String = decimalFormat.format(dollars.toLong())
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
                                openInsufficientBalanceView()
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
                        setupRecipientView(it)
                    }

                    is UiState.Error -> {
                        setShowAndHideSkeletonView(false)
                    }
                }
            }
    }

    private fun setupRecipientView(uiState: UiState.Success<FxContract>) {
        val selectFromAc = localTransferData.selectFromAc
        val buyCurrency = Currency.getInstance(uiState.data.contractBuyCurrency)
        sharedViewModel.getProductTransactionLimit(localTransferData.selectedDate)
        sharedViewModel.updateLocalTransferData(
            localTransferData.copy(
                selectToAnotherCurrency = localTransferData.selectToAnotherCurrency.copy(
                    currency = buyCurrency
                ),
                fxContract = uiState.data
            )
        )
        setShowAndHideSkeletonView(false)
        amountsBinding.sendCurrencyView.binding.tvAmountLabel.text = "You send"
        with(amountsBinding.recipientCurrencyView) {
            var previousValidInput: CharSequence = ""
            val inputFilters = arrayOf(
                InputFilter { source, _, _, dest, dstart, dend ->
                    val hasFractions = selectFromAc.amount.currency.defaultFractionDigits > 0
                    val decimalFormat = getDecimalFormat(hasFractions, selectFromAc.amount.locale)
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
            binding.tvAmount.filters = inputFilters
            var isUpdating =
                false // flag to indicate whether text is being updated programmatically
            binding.tvAmount.doAfterTextChanged {
                val selectToAnotherCurrency =
                    localTransferData.selectToAnotherCurrency
                        ?: return@doAfterTextChanged
                handleAmountTextChange(binding.tvAmount, it, selectToAnotherCurrency, isUpdating, {
                    isUpdating = true
                }, { value ->
                    sharedViewModel.updateLocalTransferData(
                        localTransferData.copy(
                            selectToAnotherCurrency = selectToAnotherCurrency.copy(
                                amount = amount.copy(
                                    value = value
                                )
                            )
                        )
                    )
                })
                isUpdating = false
            }

            amount = Amount(
                value = uiState.data.indicativeAmount?.toBigDecimal() ?: BigDecimal.ZERO,
                symbol = buyCurrency.symbol,
                locale = Locale.ENGLISH,
                currency = buyCurrency
            )

            setCurrencyView(this, buyCurrency.currencyCode)

//            doAfterTextChanged { editable ->
//                selectToAnotherCurrency.amount =
//                    selectToAnotherCurrency.amount.copy(
//                        value = editable.toString().toBigDecimal()
//                    )
//
//            }
            this.binding.tvAmount.setOnEditorActionListener { textView, i, keyEvent ->
                clearFocus()
                false
            }
            this.binding.tvCurrency.setOnClickListener {
                openCurrencyListView(buyCurrency.currencyCode)
            }
        }
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

    private fun openCurrencyListView(currency: String?) {
        CurrencyListBottomSheet(currency).apply {
            onSelectedCurrencyClicked = { currency ->
                setShowAndHideSkeletonView(true)
                sharedViewModel.getCounterFX(currency.code)
            }
        }.show(childFragmentManager, "CurrencyListBottomSheet")
    }

    private fun openInsufficientBalanceView() {
        insufficientDialog = InsufficientBalanceBottomSheet().apply {
            onChangeAccountClicked = {
                TransferFromAccountListBottomSheet.newInstance(
                    TransferStatus.TransferFrom,
                    this@InternalFundsTransferFragment
                ).show(childFragmentManager, "TransferFromAccountListBottomSheet")
            }
        }
        insufficientDialog?.show(childFragmentManager, "InsufficientBalanceBottomSheet")
    }

    private fun openProcessingBottomView() {
        processingBottomSheet.apply {
            onProcessingFinish = {
                findNavController().navigate(InternalFundsTransferFragmentDirections.actiontoconfirm())
            }
        }.show(childFragmentManager, "processingBottomSheet")
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
        val selectToAc = localTransferData.selectToAc

        amountsBinding.accountName.text = selectToAc.displayName
        amountsBinding.accountNumber.text = "${selectToAc.accountNumber} - OCBC Bank"

        when (transfertype) {
            TransferStatus.TransferMakerReview -> {
                detailsBinding.detailLayout.isVisible = false
                detailsBinding.detailReview.isVisible = true

                detailsBinding.tvReviewAccountDetails.text =
                    "${detailsBinding.transferfromAccountDesp.text} - ${selectToAc.accountNumber} ${selectToAc.currency}"
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
                    localTransferData.selectToAnotherCurrency?.amount?.value.toString()
                )
                if (localTransferData.selectToAnotherCurrency?.amount?.value?.toDouble() == 0.0) {
                    val hasFractions = selectToAc.amount.currency.defaultFractionDigits > 0
                    val decimalFormat = getDecimalFormat(hasFractions, selectToAc.amount.locale)

                    sendMoney = getString(
                        R.string.send_money,
                        "${amountsBinding.sendCurrencyView.binding.tvAmount.text} ${selectToAc.amount.currency.currencyCode}"
                    )


                }
                detailsBinding.btNext.label = sendMoney
                detailsBinding.btNext.isVisible = false
                binding.body.btReviewNext.isVisible = true
                binding.body.btReviewNext.label = sendMoney
                binding.body.btReviewNext.setOnClickListener {
//                    detailsBinding.btNext.performClick()
//                    sharedViewModel.showGenericErrorScreen(
//                        "no_reviewer",
//                        "No approver set up",
//                        "Please set up an approver in Velocity to make a transfer",
//                        "Okay",
//                        GenericMessageType.Warning.id,
//                        true,
//                        object : GenericMessageFragment.Listener {
//                            override fun onGenericMessageDoneButtonClicked(fragment: GenericMessageFragment) {
//                                childFragmentManager.popBackStackImmediate()
//                            }
//                        },
//                        null
//                    )
                    if (localTransferData.selectFromAc.is1MC) {
                        openOneTokenPin()
                    } else {
                        openOtpFragment()
                    }

                }

                amountsBinding.sendCurrencyView.isEnabled = false
                amountsBinding.recipientCurrencyView.binding.tvAmount.isEnabled = false
                amountsBinding.sendCurrencyView.downImage = false
                amountsBinding.recipientCurrencyView.downImage = false
                amountsBinding.tvUsemyrate.isVisible = false
                bottomSheetBehavior.state = STATE_COLLAPSED

            }
            else -> {
                detailsBinding.detailLayout.isVisible = true
                detailsBinding.detailReview.isVisible = false

                detailsBinding.transferfromAccount.text =
                    localTransferData.selectFromAc.accountNumber + " - " + localTransferData.selectFromAc.currency
                detailsBinding.transferfromAccountDesp.text =
                    localTransferData.selectFromAc.accountName

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

    private fun setShowAndHideSkeletonView(showhide: Boolean) {
        if (showhide) {
            amountsBinding.topShimmer.startShimmer()
            amountsBinding.amountbobyViewShimmer.isVisible = true
            amountsBinding.amountbobyView.isVisible = false
        } else {
            amountsBinding.topShimmer.stopShimmer()
            amountsBinding.amountbobyViewShimmer.isVisible = false
            amountsBinding.amountbobyView.isVisible = true
            amountsBinding.secondCurrencyLine.isVisible = true
            setIndicateRate()
        }

        amountsBinding.amountbobyView.post {
            setPeekHeight()
        }
    }

    private fun setIndicateRate() {
        amountsBinding.rlIndicative.isVisible = true
        var value =
            "1 " + localTransferData.selectToAnotherCurrency.currency.currencyCode +
                    " = " +
                    localTransferData.fxContract?.fxRate.toString() + " " +
                    localTransferData.selectFromAc.currency.currencyCode

        amountsBinding.tvIndicativeRate.text = value
        amountsBinding.tvUsemyrate.setOnClickListener {
            childFragmentManager.beginTransaction()
                .add(R.id.v_root, UseMyRateFragment.newInstance(), "UseMyRateFragment")
                .addToBackStack("UseMyRateFragment")
                .commit()
//            openFxContractListView()
        }
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
        amountsBinding.tvEditfxrate.setOnClickListener {
            openFxContractListView()
        }
    }

    private fun openFxContractListView() {
//        val selectFromAc = localTransferData.selectFromAc ?: return
//        val selectToAc = localTransferData.selectToAc ?: return
//        val selectToAnotherCurrency = localTransferData.selectToAnotherCurrency ?: return
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
        insufficientDialog?.dismiss()
        updateSelectAccountItem(type, item)
        detailsBinding.transferfromAccount.text = item.accountNumber + " - " + item.currency
        detailsBinding.transferfromAccountDesp.text = item.accountName
    }

    private fun updateSelectAccountItem(type: TransferStatus, item: AccountItemModel) {
        when (type) {
            TransferStatus.TransferFrom -> {
                sharedViewModel.updateLocalTransferData(
                    localTransferData.copy(
                        selectFromAc = item
                    )
                )
            }

            TransferStatus.TransferToMyAccounts -> {
                localTransferData.copy(
                    selectToAc = item
                )
            }
        }
    }

    override fun showErrorMessage(error: String?) {

    }

}
