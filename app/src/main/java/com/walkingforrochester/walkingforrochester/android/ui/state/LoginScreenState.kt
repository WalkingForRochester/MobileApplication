package com.walkingforrochester.walkingforrochester.android.ui.state

data class LoginScreenState(
    var emailAddress: String = "",
    var emailAddressValidationMessage: String = "",
    var password: String = "",
    var passwordValidationMessage: String = "",
    var passwordVisible: Boolean = false,
    var authenticationErrorMessage: String = "",
    val loading: Boolean = false,
    val socialLoading: Boolean = false,
    var registrationScreenState: RegistrationScreenState = RegistrationScreenState()
)

enum class LoginScreenEvent {
    LoginComplete, NeedsRegistration
}