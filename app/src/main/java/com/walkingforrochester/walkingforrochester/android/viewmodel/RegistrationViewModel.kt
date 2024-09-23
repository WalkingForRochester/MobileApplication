package com.walkingforrochester.walkingforrochester.android.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.network.RestApiService
import com.walkingforrochester.walkingforrochester.android.network.request.EmailAddressRequest
import com.walkingforrochester.walkingforrochester.android.network.request.RegisterRequest
import com.walkingforrochester.walkingforrochester.android.ui.state.RegistrationScreenEvent
import com.walkingforrochester.walkingforrochester.android.ui.state.RegistrationScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val restApiService: RestApiService,
    @ApplicationContext private val context: Context,
    private val sharedPreferences: SharedPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationScreenState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<RegistrationScreenEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun prefill(initState: RegistrationScreenState) = _uiState.update { initState }

    fun onEmailChange(newEmail: String) =
        _uiState.update { it.copy(email = newEmail, emailValidationMessageId = 0) }

    fun onFirstNameChange(newFirstName: String) =
        _uiState.update { state ->
            state.copy(
                firstName = newFirstName.filter { it != '\n' },
                firstNameValidationMessageId = 0
            )
        }

    fun onLastNameChange(newLastName: String) =
        _uiState.update { state ->
            state.copy(
                lastName = newLastName.filter { it != '\n' },
                lastNameValidationMessageId = 0
            )
        }

    fun onPhoneChange(newPhone: String) =
        _uiState.update { state ->
            state.copy(
                phone = newPhone.filter { it.isDigit() },
                phoneValidationMessageId = 0
            )
        }

    fun onNicknameChange(newNickname: String) =
        _uiState.update { state -> state.copy(nickname = newNickname.filter { it != '\n' }) }

    fun onPasswordChange(newPassword: String) =
        _uiState.update { state ->
            state.copy(
                password = newPassword.filter { it != '\n' },
                passwordValidationMessageId = 0
            )
        }

    fun onPasswordConfirmationChange(newConfirmPassword: String) =
        _uiState.update { state ->
            state.copy(
                confirmPassword = newConfirmPassword.filter { it != '\n' },
                confirmPasswordValidationMessageId = 0
            )
        }

    fun onCommunityServiceChange(newCommunityService: Boolean) =
        _uiState.update { it.copy(communityService = newCommunityService) }

    fun onSignUp() = viewModelScope.launch {
        _uiState.update { it.copy(loading = true) }
        try {
            if (validateForm()) {
                with(_uiState.value) {
                    val result = restApiService.registerAccount(
                        RegisterRequest(
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            phone = phone,
                            nickname = nickname,
                            dateOfBirth = LocalDate.now(),
                            password = password,
                            communityService = communityService,
                            facebookId = facebookId
                        )
                    )

                    if (result.accountId != null) {
                        completeRegistration(result.accountId)
                    } else {
                        throw RuntimeException(result.error)
                    }
                }
            }
        } catch (t: Throwable) {
            Timber.e(t, "Sign up request failed")
            _eventFlow.emit(RegistrationScreenEvent.UnexpectedError)
        } finally {
            _uiState.update { it.copy(loading = false) }
        }
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

        with(localState) {
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
            } else if (restApiService.accountByEmail(EmailAddressRequest(email = email)).accountId != null) {
                emailValidationMessageId = R.string.email_already_registered
                isValid = false
            }
            if (phone.length != 10) {
                phoneValidationMessageId = R.string.invalid_phone
                isValid = false
            }
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
        sharedPreferences.edit().putLong(context.getString(R.string.wfr_account_id), accountId)
            .apply()
        _eventFlow.emit(RegistrationScreenEvent.RegistrationComplete)
    }

}