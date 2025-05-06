package com.walkingforrochester.walkingforrochester.android.ui.state

import androidx.annotation.StringRes

data class LoginScreenState(
    val emailAddress: String = "",
    @StringRes val emailAddressValidationMessageId: Int = 0,
    val password: String = "",
    val authenticationErrorMessage: String = "",
    val authenticationErrorMessageId: Int = 0,
    val loading: Boolean = false,
    val firstName: String = "",
    val lastName: String = "",
    val facebookId: String = "",
)

enum class LoginScreenEvent {
    LoginComplete, LoginCompleteManual, NeedsRegistration, UnexpectedError
}