package com.walkingforrochester.walkingforrochester.android.ui.state

import java.time.LocalDate

data class RegistrationScreenState constructor(
    var email: String = "",
    var emailValidationMessage: String = "",
    var firstName: String = "",
    var firstNameValidationMessage: String = "",
    var lastName: String = "",
    var lastNameValidationMessage: String = "",
    var phone: String = "",
    var phoneValidationMessage: String = "",
    var nickname: String = "",
    var dateOfBirth: LocalDate = LocalDate.now(),
    var dateOfBirthValidationMessage: String = "",
    var password: String = "",
    var passwordValidationMessage: String = "",
    var confirmPassword: String = "",
    var confirmPasswordValidationMessage: String = "",
    var passwordVisible: Boolean = false,
    var confirmPasswordVisible: Boolean = false,
    var showDatePicker: Boolean = false,
    var loading: Boolean = false,
    var communityService: Boolean = false
)

enum class RegistrationScreenEvent {
    RegistrationComplete
}