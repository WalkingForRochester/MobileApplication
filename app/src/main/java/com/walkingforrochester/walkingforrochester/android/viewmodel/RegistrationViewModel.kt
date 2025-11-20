package com.walkingforrochester.walkingforrochester.android.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.model.AccountProfile
import com.walkingforrochester.walkingforrochester.android.repository.NetworkRepository
import com.walkingforrochester.walkingforrochester.android.repository.PreferenceRepository
import com.walkingforrochester.walkingforrochester.android.ui.state.RegistrationScreenEvent
import com.walkingforrochester.walkingforrochester.android.ui.state.RegistrationScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val networkRepository: NetworkRepository,
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationScreenState())
    val uiState = _uiState.asStateFlow()

    private val _registrationProfile = MutableStateFlow(AccountProfile.DEFAULT_PROFILE)
    val registrationProfile = _registrationProfile.asStateFlow()

    private val _eventFlow = MutableSharedFlow<RegistrationScreenEvent>(
        // Using capacity of one to allow exception handler to emit outside of coroutine
        extraBufferCapacity = 1
    )
    val eventFlow = _eventFlow.asSharedFlow()

    fun prefill(profile: AccountProfile) = _registrationProfile.update { profile }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable, "Unexpected error registering account")

        if (!_eventFlow.tryEmit(RegistrationScreenEvent.UnexpectedError)) {
            Timber.w("Failed to report error due to no listener")
        }

        _uiState.update { it.copy(loading = false) }
    }

    fun onProfileChange(profile: AccountProfile) {
        val currentProfile = _registrationProfile.value

        if (profile.email != currentProfile.email) {
            _uiState.update {
                it.copy(emailValidationMessageId = 0)
            }
        }

        if (profile.firstName != currentProfile.firstName) {
            _uiState.update { it.copy(firstNameValidationMessageId = 0) }
        }

        if (profile.lastName != currentProfile.lastName) {
            _uiState.update { it.copy(lastNameValidationMessageId = 0) }
        }

        if (profile.phoneNumber != currentProfile.phoneNumber) {
            _uiState.update { it.copy(phoneValidationMessageId = 0) }
        }

        _registrationProfile.update {
            it.copy(
                email = profile.email.trim(),
                firstName = profile.firstName.filter { it != '\n' },
                lastName = profile.lastName.filter { it != '\n' },
                phoneNumber = profile.phoneNumber.filter { it.isDigit() },
                nickname = profile.nickname.filter { it != '\n' },
                communityService = profile.communityService
            )
        }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { state ->
            state.copy(
                password = newPassword.filterNot { it.isWhitespace() },
                passwordValidationMessageId = 0
            )
        }
    }

    fun onPasswordConfirmationChange(newConfirmPassword: String) {
        _uiState.update { state ->
            state.copy(
                confirmPassword = newConfirmPassword.filterNot { it.isWhitespace() },
                confirmPasswordValidationMessageId = 0
            )
        }
    }

    fun onSignUp() = viewModelScope.launch(context = exceptionHandler) {
        _uiState.update { it.copy(loading = true) }

        if (validateForm()) {
            val profile = _registrationProfile.value
            val password = _uiState.value.password

            val accountId = networkRepository.registerAccount(profile, password)

            if (accountId != AccountProfile.NO_ACCOUNT) {
                completeRegistration(accountId)
            }
        }

        _uiState.update { it.copy(loading = false) }
    }

    private suspend fun validateForm(): Boolean {
        var isValid = true
        var emailValidationMessageId = 0
        var firstNameValidationMessageId = 0
        var lastNameValidationMessageId = 0
        var phoneValidationMessageId = 0
        var passwordValidationMessageId = 0
        var confirmPasswordValidationMessageId = 0

        val localState = _uiState.value
        val profile = _registrationProfile.value

        with(profile) {
            if (firstName.isEmpty()) {
                firstNameValidationMessageId = R.string.invalid_field_empty
                isValid = false
            }
            if (lastName.isEmpty()) {
                lastNameValidationMessageId = R.string.invalid_field_empty
                isValid = false
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailValidationMessageId = R.string.invalid_email
                isValid = false
            } else if (networkRepository.isEmailInUse(email)) {
                emailValidationMessageId = R.string.email_already_registered
                isValid = false
            }
            if (phoneNumber.length != 10) {
                phoneValidationMessageId = R.string.invalid_phone
                isValid = false
            }
        }

        with(localState) {
            if (password.length < 8) {
                passwordValidationMessageId = R.string.invalid_password
                isValid = false
            }
            if (password != confirmPassword) {
                confirmPasswordValidationMessageId = R.string.invalid_password_match
                isValid = false
            }
        }

        _uiState.update {
            it.copy(
                emailValidationMessageId = emailValidationMessageId,
                firstNameValidationMessageId = firstNameValidationMessageId,
                lastNameValidationMessageId = lastNameValidationMessageId,
                phoneValidationMessageId = phoneValidationMessageId,
                passwordValidationMessageId = passwordValidationMessageId,
                confirmPasswordValidationMessageId = confirmPasswordValidationMessageId
            )
        }
        return isValid
    }

    private fun completeRegistration(accountId: Long) = viewModelScope.launch {
        preferenceRepository.updateAccountId(accountId)
        _eventFlow.emit(RegistrationScreenEvent.RegistrationComplete)
    }

}