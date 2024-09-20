package com.walkingforrochester.walkingforrochester.android.ui.state

data class LoginScreenState(
    val emailAddress: String = "",
    val emailAddressValidationMessage: String = "",
    val password: String = "",
    val authenticationErrorMessage: String = "",
    val loading: Boolean = false,
    val socialLoading: Boolean = false,
    val firstName: String = "",
    val lastName: String = "",
    val facebookId: String = "",
)

enum class LoginScreenEvent {
    LoginComplete, LoginCompleteManual, NeedsRegistration
}