package com.redairship.ocbc.transfer.presentation.transfer.local.otp

import android.content.Context
import android.hardware.Camera
import android.nfc.NfcManager
import android.os.Build
import android.os.CountDownTimer
import android.text.Html
import android.text.format.DateUtils
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.coroutineScope
import com.redairship.ocbc.bb.components.utils.DeviceUtils
import com.redairship.ocbc.bb.components.views.fragments.errors.GenericMessageFragment
import com.redairship.ocbc.bb.components.views.fragments.errors.GenericMessageType
import com.redairship.ocbc.bb.components.views.fragments.otp.OTPFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.concurrent.TimeUnit

class OneTokenPinFragment : OTPFragment(), GenericMessageFragment.Listener {

    private val viewModel: OneTokenPinViewModel by sharedViewModel()
    private val nfcManager: NfcManager by lazy { requireContext().getSystemService(Context.NFC_SERVICE) as NfcManager }
    private var hasCamera = true
    private var hasNfc = true

    companion object {
        @JvmStatic
        fun newInstance(
            header: CharSequence,
            subHeader: CharSequence,
            resendLabel: CharSequence,
            didNotReceiveLabel: CharSequence,
            timeLimit: Long,
            maxLength: Int
        ) = OneTokenPinFragment().apply {
            arguments = bundleOf(
                "header" to header,
                "subHeader" to subHeader,
                "didNotReceiveLabel" to didNotReceiveLabel,
                "resendLabel" to resendLabel,
                "timeLimit" to timeLimit,
                "maxLength" to maxLength
            )
        }
    }

    override fun registerViewModelEvents() {
        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            viewLifecycleOwner.lifecycle.coroutineScope.launch {
                when (uiState) {
                    is OneTokenPinViewModel.OtpUiState.Error -> {
                        binding.btSubmit.isLoading = false
                        binding.pinEntryView.text = ""

                        val remainingAttempts = uiState.remainingAttempts
                        if (remainingAttempts <= 0) {
                            binding.vError.isVisible = false
                            showExceededAttempt()
                            return@launch
                        }
                        binding.vError.isVisible = true

                        binding.vError.label = ""
                    }
                    is OneTokenPinViewModel.OtpUiState.Success -> {
                        binding.pinEntryView.text = ""
                    }
                    else -> {
                        binding.btSubmit.isLoading = false
                    }
                }
            }
        }
    }

    override fun attemptText(remainingAttempts: Int): CharSequence = ""

    override fun startCountdown() {
        val resendLabel = "Forgot PIN?"
        val resendHasParams = resendLabel.contains("%s")
        countdown?.cancel()
        countdown =
            object : CountDownTimer(TimeUnit.SECONDS.toMillis(timeLimit), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    binding.tvResendTimer.isVisible = !resendHasParams
                    if (resendHasParams) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                            binding.tvResendLabel.text = Html.fromHtml(getString(R.string.ras_passportauth_otp_resend, DateUtils.formatElapsedTime(millisUntilFinished / 1000)), Html.FROM_HTML_MODE_COMPACT)
                        } else {
//                            binding.tvResendLabel.text = Html.fromHtml(getString(R.string.ras_passportauth_otp_resend, DateUtils.formatElapsedTime(millisUntilFinished / 1000)))
                        }
                    } else {
                        binding.tvResendTimer.text =
                            DateUtils.formatElapsedTime(millisUntilFinished / 1000)
                    }
                }

                override fun onFinish() {
                    binding.linkResend.text = "Forgot PIN?"
                    binding.tvResendLabel.text = ""
                    binding.tvResendTimer.isVisible = false
                    binding.linkResend.isVisible = true
                }
            }.also {
                it.start()
            }
    }

    fun showExceededAttempt() {
        countdown?.cancel()
//        viewModel.showGenericErrorScreen(
//            tag = "exceeded_attempt",
//            title = getString(R.string.ras_passportauth_otp_max_attempt_error_title),
//            description = getString(R.string.ras_passportauth_otp_max_attempt_error_description),
//            buttonText = getString(R.string.ras_passportauth_get_in_touch),
//            type = GenericMessageType.Warning.id,
//            interceptDoneAction = true,
//            listener = this,
//            closeListener = null
//        )
    }

    private fun showGenericError(
        title: String,
        message: String,
        buttonLabel: String,
        type: GenericMessageType,
        interceptDone: Boolean = true
    ) {
        viewModel.showGenericErrorScreen(
            tag = "generic_error",
            title = title,
            description = message,
            buttonText = buttonLabel,
            type = type.id,
            interceptDoneAction = interceptDone,
            listener = this,
            closeListener = null
        )
    }

    override fun resend() {
        viewModel.resendOTP()
    }

    override fun verify(otp: String) {
        viewModel.verifyOTP(otp)
    }

    override fun onGenericMessageDoneButtonClicked(fragment: GenericMessageFragment) {
        if (!hasNfc) {
            viewModel.goToScheduleVideoCall()
        } else {
//            GetInTouchDialog.newInstance()
//                .show(childFragmentManager, GetInTouchDialog::class.java.simpleName)
        }
    }


}
