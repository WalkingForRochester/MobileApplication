package com.walkingforrochester.walkingforrochester.android.ui.state

data class ForgotPasswordScreenState(
    var email: String = "",
    var emailValidationMessage: String = "",
    var internalCode: String = "",
    var code: String = "",
    var codeValidationMessage: String = "",
    var codeVerified: Boolean = false,
    var password: String = "",
    var passwordValidationMessage: String = "",
    var passwordVisible: Boolean = false,
    var confirmPassword: String = "",
    var confirmPasswordValidationMessage: String = "",
    var confirmPasswordVisible: Boolean = false,
    var loading: Boolean = false
)

enum class ForgotPasswordScreenEvent {
    PasswordReset
}