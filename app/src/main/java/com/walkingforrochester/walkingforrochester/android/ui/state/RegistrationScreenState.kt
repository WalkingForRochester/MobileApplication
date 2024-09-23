package com.walkingforrochester.walkingforrochester.android.ui.state

import androidx.annotation.StringRes

data class RegistrationScreenState(
    val email: String = "",
    @StringRes val emailValidationMessageId: Int = 0,
    val firstName: String = "",
    @StringRes val firstNameValidationMessageId: Int = 0,
    val lastName: String = "",
    @StringRes val lastNameValidationMessageId: Int = 0,
    val phone: String = "",
    @StringRes val phoneValidationMessageId: Int = 0,
    val nickname: String = "",
    val password: String = "",
    @StringRes val passwordValidationMessageId: Int = 0,
    val confirmPassword: String = "",
    @StringRes val confirmPasswordValidationMessageId: Int = 0,
    val loading: Boolean = false,
    val communityService: Boolean = false,
    val facebookId: String? = null
)

enum class RegistrationScreenEvent {
    RegistrationComplete, UnexpectedError
}