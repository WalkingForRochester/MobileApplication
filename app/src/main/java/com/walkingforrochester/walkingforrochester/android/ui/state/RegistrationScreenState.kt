package com.walkingforrochester.walkingforrochester.android.ui.state

import androidx.annotation.StringRes

data class RegistrationScreenState(
    @StringRes val emailValidationMessageId: Int = 0,
    @StringRes val firstNameValidationMessageId: Int = 0,
    @StringRes val lastNameValidationMessageId: Int = 0,
    @StringRes val phoneValidationMessageId: Int = 0,
    val password: String = "",
    @StringRes val passwordValidationMessageId: Int = 0,
    val confirmPassword: String = "",
    @StringRes val confirmPasswordValidationMessageId: Int = 0,
    val loading: Boolean = false,
)

enum class RegistrationScreenEvent {
    RegistrationComplete, UnexpectedError
}