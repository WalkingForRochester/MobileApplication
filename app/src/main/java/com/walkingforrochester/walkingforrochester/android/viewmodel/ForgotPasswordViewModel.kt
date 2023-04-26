package com.walkingforrochester.walkingforrochester.android.viewmodel

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.network.RestApiService
import com.walkingforrochester.walkingforrochester.android.network.request.EmailAddressRequest
import com.walkingforrochester.walkingforrochester.android.network.request.LoginRequest
import com.walkingforrochester.walkingforrochester.android.showUnexpectedErrorToast
import com.walkingforrochester.walkingforrochester.android.ui.state.ForgotPasswordScreenEvent
import com.walkingforrochester.walkingforrochester.android.ui.state.ForgotPasswordScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val restApiService: RestApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordScreenState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ForgotPasswordScreenEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEmailChange(newEmail: String) =
        _uiState.update { state ->
            state.copy(
                email = newEmail.filter { it != '\n' },
                emailValidationMessage = ""
            )
        }

    private fun setInternalCode(internalCode: String) =
        _uiState.update { state -> state.copy(internalCode = internalCode) }

    fun onCodeChange(newCode: String) =
        _uiState.update { state ->
            state.copy(
                code = newCode.filter { it.isDigit() },
                codeValidationMessage = ""
            )
        }

    fun onPasswordChange(newPassword: String) =
        _uiState.update { state ->
            state.copy(
                password = newPassword.filter { it != '\n' },
                passwordValidationMessage = ""
            )
        }

    fun onConfirmPasswordChange(newConfirmPassword: String) = _uiState.update { state ->
        state.copy(
            confirmPassword = newConfirmPassword.filter { it != '\n' },
            confirmPasswordValidationMessage = ""
        )
    }

    fun onPasswordVisibilityChange() =
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun onConfirmPasswordVisibilityChange() =
        _uiState.update { it.copy(confirmPasswordVisible = !it.confirmPasswordVisible) }

    fun sendCode() = flow<Nothing> {
        _uiState.update { it.copy(loading = true) }
        if (validateEmail()) {
            with(_uiState.value) {
                val result = restApiService.forgotPassword(EmailAddressRequest(email = email))

                setInternalCode(result.code)
            }
        }
    }.catch {
        Timber.e(it, "Unable to send password reset code")
        showUnexpectedErrorToast(context)
    }.onCompletion {
        _uiState.update { it.copy(loading = false) }
    }.launchIn(viewModelScope)

    fun verifyCode() = flow<Nothing> {
        with(_uiState.value) {
            if (code.isNotEmpty() && code == internalCode) {
                _uiState.update { it.copy(codeVerified = true) }
            } else {
                _uiState.update { it.copy(codeValidationMessage = context.getString(R.string.invalid_code)) }
            }
        }
    }.launchIn(viewModelScope)

    fun resetPassword() = flow<Nothing> {
        _uiState.update { it.copy(loading = true) }
        if (validatePassword()) {
            with(_uiState.value) {
                restApiService.resetPassword(LoginRequest(email, password))

                _eventFlow.emit(ForgotPasswordScreenEvent.PasswordReset)
                Toast.makeText(context, R.string.password_reset_done, Toast.LENGTH_LONG).show()
            }
        }
    }.catch {
        Timber.e(it, "Unable to reset password")
        showUnexpectedErrorToast(context)
    }.onCompletion {
        _uiState.update { it.copy(loading = false) }
    }.launchIn(viewModelScope)

    private fun validateEmail(): Boolean {
        var isValid = true
        _uiState.update {
            it.copy(
                emailValidationMessage = ""
            )
        }

        with(_uiState.value) {
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _uiState.update { it.copy(emailValidationMessage = context.getString(R.string.invalid_email)) }
                isValid = false
            }
        }

        return isValid
    }

    private fun validatePassword(): Boolean {
        var isValid = true
        _uiState.update {
            it.copy(
                passwordValidationMessage = "",
                confirmPasswordValidationMessage = ""
            )
        }
        val localState = _uiState.value.copy()

        with(localState) {
            if (password.length < 6) {
                passwordValidationMessage = context.getString(R.string.invalid_password)
                isValid = false
            }
            if (password != confirmPassword) {
                confirmPasswordValidationMessage =
                    context.getString(R.string.invalid_password_match)
                isValid = false
            }
        }

        _uiState.update { localState }
        return isValid
    }
}