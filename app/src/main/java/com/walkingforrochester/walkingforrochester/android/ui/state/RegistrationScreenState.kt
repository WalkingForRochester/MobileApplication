package com.walkingforrochester.walkingforrochester.android.ui.state

data class RegistrationScreenState(
    val email: String = "",
    val emailValidationMessage: String = "",
    val firstName: String = "",
    val firstNameValidationMessage: String = "",
    val lastName: String = "",
    val lastNameValidationMessage: String = "",
    val phone: String = "",
    val phoneValidationMessage: String = "",
    val nickname: String = "",
    val password: String = "",
    val passwordValidationMessage: String = "",
    val confirmPassword: String = "",
    val confirmPasswordValidationMessage: String = "",
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val loading: Boolean = false,
    val communityService: Boolean = false,
    val facebookId: String? = null
)

enum class RegistrationScreenEvent {
    RegistrationComplete
}