package com.redairship.ocbc.transfer.presentation.transfer.local.otp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redairship.ocbc.bb.components.views.fragments.errors.GenericMessageFragment
import com.redairship.ocbc.transfer.LocalTransferCoordinatorInterface
import com.redairship.ocbc.transfer.utils.DomainException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val ERROR_SHOWING_DELAY = 1000L
private const val DEFAULT_REMAINING_ATTEMPTS = 3

class OneTokenPinViewModel(
    private val coordinator: LocalTransferCoordinatorInterface
) : ViewModel() {

    private val _uiState = MutableLiveData<OtpUiState>()
    val uiState: LiveData<OtpUiState> = _uiState

    private var remainingAttempts: Int = DEFAULT_REMAINING_ATTEMPTS

    fun verifyOTP(otp: String) {
        coordinator.verifyOtp(otp)
            .catch { error ->
                if (error is DomainException) {
                    // Server Error scenario
                    viewModelScope.launch {

                    }
                }
            }
            .onEach { twoFactorResult ->
                _uiState.value = OtpUiState.Success
            }
            .launchIn(viewModelScope)

    }

    fun goToScanBarcode() {

    }

    fun goToScheduleVideoCall() {

    }

    private suspend fun onVerifyOtpError(errorMessage: String, remainingAttempts: Int) {
        delay(ERROR_SHOWING_DELAY)
        _uiState.value = OtpUiState.Error(errorMessage, remainingAttempts)
    }

    fun getOTPCountdown() = 60

    fun resendOTP() {
//        coordinator.resendOtp()
//            .catch {
//
//            }
//            .launchIn(viewModelScope)
    }

    fun getMobileNumber() = ""//passportAuthCoordinatorInterface.otpInfo?.mobileNumber ?: ""

    fun maxOTPAttemptError() {
//        passportAuthCoordinatorInterface.goToMaxOTPAttemptError()
    }

    fun resetUiState() {
//        _uiState.postValue(null)
    }

    fun showGenericErrorScreen(
        tag: String,
        title: String,
        description: String,
        buttonText: String,
        type: Int,
        interceptDoneAction: Boolean = false,
        listener: GenericMessageFragment.Listener?,
        closeListener: GenericMessageFragment.CloseListener?,
        hasCloseIcon: Boolean = false,
        interceptCloseAction: Boolean = false
    ) {
//        coordinator.showGenericErrorScreen(
//            tag = tag,
//            title = title,
//            description = description,
//            buttonText = buttonText,
//            type = type,
//            interceptDoneAction = interceptDoneAction,
//            listener = listener,
//            closeListener = closeListener,
//            hasCloseIcon = hasCloseIcon,
//            interceptCloseAction = interceptCloseAction
//        )
    }

    sealed class OtpUiState {
        object Success : OtpUiState()
        data class Error(val errorMessage: String, val remainingAttempts: Int) : OtpUiState()
    }
}