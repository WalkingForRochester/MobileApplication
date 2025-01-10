package com.walkingforrochester.walkingforrochester.android.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.repository.NetworkRepository
import com.walkingforrochester.walkingforrochester.android.ui.state.ForgotPasswordScreenEvent
import com.walkingforrochester.walkingforrochester.android.ui.state.ForgotPasswordScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val networkRepository: NetworkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordScreenState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ForgotPasswordScreenEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        Timber.e(throwable, "Unexpected error processing profile")

        if (!_eventFlow.tryEmit(ForgotPasswordScreenEvent.UnexpectedError)) {
            Timber.w("Failed to report error due to no listener")
        }

        _uiState.update { it.copy(loading = false) }
    }

    fun onEmailChange(newEmail: String) {
        _uiState.update { state ->
            state.copy(
                email = newEmail,
                emailValidationMessageId = 0
            )
        }
    }

    fun onCodeChange(newCode: String) {
        _uiState.update { state ->
            state.copy(
                code = newCode.filter { it.isDigit() },
                codeValidationMessageId = 0
            )
        }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { state ->
            state.copy(
                password = newPassword.filterNot { it.isWhitespace() },
                passwordValidationMessageId = 0
            )
        }
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _uiState.update { state ->
            state.copy(
                confirmPassword = newConfirmPassword.filterNot { it.isWhitespace() },
                confirmPasswordValidationMessageId = 0
            )
        }
    }

    fun requestCode() = viewModelScope.launch(context = exceptionHandler) {
        if (validateEmail()) {
            _uiState.update { it.copy(loading = true) }
            with(_uiState.value) {
                val code = networkRepository.forgotPassword(email = email)
                _uiState.update { state ->
                    state.copy(
                        internalCode = code,
                        loading = false
                    )
                }
            }
        }
    }

    fun verifyCode() = viewModelScope.launch(context = exceptionHandler) {
        with(_uiState.value) {
            if (code.isNotEmpty() && code == internalCode) {
                _uiState.update { it.copy(codeVerified = true) }
            } else {
                _uiState.update { it.copy(codeValidationMessageId = R.string.invalid_code) }
            }
        }
    }

    fun resetPassword() = viewModelScope.launch(context = exceptionHandler) {
        if (validatePassword()) {
            _uiState.update { it.copy(loading = true) }

            with(_uiState.value) {
                networkRepository.resetPassword(email = email, password = password)
                _eventFlow.emit(ForgotPasswordScreenEvent.PasswordReset)
            }

            _uiState.update { it.copy(loading = false) }
        }
    }

    private fun validateEmail(): Boolean {
        var isValid = true
        var emailValidationMessageId = 0

        val state = _uiState.value
        if (!Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            emailValidationMessageId = R.string.invalid_email
            isValid = false
        }

        _uiState.update {
            it.copy(
                emailValidationMessageId = emailValidationMessageId
            )
        }

        return isValid
    }

    private fun validatePassword(): Boolean {
        var passwordValidationMessageId = 0
        var confirmPasswordValidationMessageId = 0
        var isValid = true

        val state = _uiState.value

        if (state.password.length < 8) {
            passwordValidationMessageId = R.string.invalid_password
            isValid = false
        }
        if (state.password != state.confirmPassword) {
            confirmPasswordValidationMessageId = R.string.invalid_password_match
            isValid = false
        }

        _uiState.update {
            it.copy(
                passwordValidationMessageId = passwordValidationMessageId,
                confirmPasswordValidationMessageId = confirmPasswordValidationMessageId
            )
        }
        return isValid
    }
}