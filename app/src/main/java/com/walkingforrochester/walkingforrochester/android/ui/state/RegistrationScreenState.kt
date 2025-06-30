package com.walkingforrochester.walkingforrochester.android.ui.state

import androidx.annotation.StringRes

data class RegistrationScreenState(
    @param:StringRes val emailValidationMessageId: Int = 0,
    @param:StringRes val firstNameValidationMessageId: Int = 0,
    @param:StringRes val lastNameValidationMessageId: Int = 0,
    @param:StringRes val phoneValidationMessageId: Int = 0,
    val password: String = "",
    @param:StringRes val passwordValidationMessageId: Int = 0,
    val confirmPassword: String = "",
    @param:StringRes val confirmPasswordValidationMessageId: Int = 0,
    val loading: Boolean = false,
)

enum class RegistrationScreenEvent {
    RegistrationComplete, UnexpectedError
}