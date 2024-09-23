package com.walkingforrochester.walkingforrochester.android.ui.state

import androidx.annotation.StringRes

data class ForgotPasswordScreenState(
    val email: String = "",
    @StringRes val emailValidationMessageId: Int = 0,
    val internalCode: String = "",
    val code: String = "",
    @StringRes val codeValidationMessageId: Int = 0,
    val codeVerified: Boolean = false,
    val password: String = "",
    @StringRes val passwordValidationMessageId: Int = 0,
    val confirmPassword: String = "",
    @StringRes val confirmPasswordValidationMessageId: Int = 0,
    val loading: Boolean = false
)

enum class ForgotPasswordScreenEvent {
    PasswordReset, UnexpectedError
}