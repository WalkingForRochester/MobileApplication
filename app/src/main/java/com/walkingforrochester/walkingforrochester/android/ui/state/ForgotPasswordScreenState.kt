package com.walkingforrochester.walkingforrochester.android.ui.state

import androidx.annotation.StringRes

data class ForgotPasswordScreenState(
    val email: String = "",
    @param:StringRes val emailValidationMessageId: Int = 0,
    val internalCode: String = "",
    val code: String = "",
    @param:StringRes val codeValidationMessageId: Int = 0,
    val codeVerified: Boolean = false,
    val password: String = "",
    @param:StringRes val passwordValidationMessageId: Int = 0,
    val confirmPassword: String = "",
    @param:StringRes val confirmPasswordValidationMessageId: Int = 0,
    val loading: Boolean = false
)

enum class ForgotPasswordScreenEvent {
    PasswordReset, UnexpectedError
}