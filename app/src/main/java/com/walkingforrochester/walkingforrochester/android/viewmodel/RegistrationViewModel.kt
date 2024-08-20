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
import com.walkingforrochester.walkingforrochester.android.showUnexpectedErrorToast
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
        _uiState.update { it.copy(email = newEmail, emailValidationMessage = "") }

    fun onFirstNameChange(newFirstName: String) =
        _uiState.update { state ->
            state.copy(
                firstName = newFirstName.filter { it != '\n' },
                firstNameValidationMessage = ""
            )
        }

    fun onLastNameChange(newLastName: String) =
        _uiState.update { state ->
            state.copy(
                lastName = newLastName.filter { it != '\n' },
                lastNameValidationMessage = ""
            )
        }

    fun onPhoneChange(newPhone: String) =
        _uiState.update { state ->
            state.copy(
                phone = newPhone.filter { it.isDigit() },
                phoneValidationMessage = ""
            )
        }

    fun onNicknameChange(newNickname: String) =
        _uiState.update { state -> state.copy(nickname = newNickname.filter { it != '\n' }) }

    fun onPasswordChange(newPassword: String) =
        _uiState.update { state ->
            state.copy(
                password = newPassword.filter { it != '\n' },
                passwordValidationMessage = ""
            )
        }

    fun onPasswordConfirmationChange(newConfirmPassword: String) =
        _uiState.update { state ->
            state.copy(
                confirmPassword = newConfirmPassword.filter { it != '\n' },
                confirmPasswordValidationMessage = ""
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
            showUnexpectedErrorToast(context)
        } finally {
            _uiState.update { it.copy(loading = false) }
        }
    }

    fun onPasswordVisibilityChange() =
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun onConfirmPasswordVisibilityChange() =
        _uiState.update { it.copy(confirmPasswordVisible = !it.confirmPasswordVisible) }

    private suspend fun validateForm(): Boolean {
        var isValid = true
        var emailValidationMessage = ""
        var firstNameValidationMessage = ""
        var lastNameValidationMessage = ""
        var phoneValidationMessage = ""
        var passwordValidationMessage = ""
        var confirmPasswordValidationMessage = ""

        val localState = _uiState.value

        with(localState) {
            if (firstName.isEmpty()) {
                firstNameValidationMessage = context.getString(R.string.invalid_field_empty)
                isValid = false
            }
            if (lastName.isEmpty()) {
                lastNameValidationMessage = context.getString(R.string.invalid_field_empty)
                isValid = false
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailValidationMessage = context.getString(R.string.invalid_email)
                isValid = false
            } else if (restApiService.accountByEmail(EmailAddressRequest(email = email)).accountId != null) {
                emailValidationMessage = context.getString(R.string.email_already_registered)
                isValid = false
            }
            if (phone.length != 10) {
                phoneValidationMessage = context.getString(R.string.invalid_phone)
                isValid = false
            }
            if (password.length < 6) {
                passwordValidationMessage = context.getString(R.string.invalid_password)
                isValid = false
            }
            if (password != confirmPassword) {
                confirmPasswordValidationMessage =
                    context.getString(R.string.invalid_password_match)
                isValid = false
            }
        }

        _uiState.update {
            it.copy(
                emailValidationMessage = emailValidationMessage,
                firstNameValidationMessage = firstNameValidationMessage,
                lastNameValidationMessage = lastNameValidationMessage,
                phoneValidationMessage = phoneValidationMessage,
                passwordValidationMessage = passwordValidationMessage,
                confirmPasswordValidationMessage = confirmPasswordValidationMessage
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