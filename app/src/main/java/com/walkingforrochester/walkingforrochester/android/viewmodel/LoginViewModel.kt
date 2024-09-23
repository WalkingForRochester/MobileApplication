package com.walkingforrochester.walkingforrochester.android.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.network.RestApiService
import com.walkingforrochester.walkingforrochester.android.network.request.AccountIdRequest
import com.walkingforrochester.walkingforrochester.android.network.request.EmailAddressRequest
import com.walkingforrochester.walkingforrochester.android.network.request.LoginRequest
import com.walkingforrochester.walkingforrochester.android.network.request.UpdateProfileRequest
import com.walkingforrochester.walkingforrochester.android.network.response.AccountResponse
import com.walkingforrochester.walkingforrochester.android.ui.state.LoginScreenEvent
import com.walkingforrochester.walkingforrochester.android.ui.state.LoginScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val restApiService: RestApiService,
    @ApplicationContext private val context: Context,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginScreenState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<LoginScreenEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onLoginClicked(autofillData: Boolean = true) = viewModelScope.launch {
        performLogin(manualLogin = !autofillData)
    }

    fun continueWithGoogle(
        googleCredential: GoogleIdTokenCredential
    ) = viewModelScope.launch {
        socialSignIn(
            email = googleCredential.id,
            firstName = googleCredential.givenName ?: "",
            lastName = googleCredential.familyName ?: "",
        )
    }

    private suspend fun performLogin(manualLogin: Boolean) {
        _uiState.update { it.copy(loading = true) }
        try {
            if (validateCredentials()) {
                with(_uiState.value) {
                    val result: AccountResponse =
                        restApiService.login(LoginRequest(emailAddress, password))

                    if (result.accountId != null) {
                        completeLogin(result.accountId, manualLogin)
                    } else {
                        setAuthenticationError(result.error)
                    }
                }
            }
        } catch (t: Throwable) {
            Timber.e(t, "Login request failed")
            _eventFlow.emit(LoginScreenEvent.UnexpectedError)
        } finally {
            _uiState.update { it.copy(loading = false) }
        }
    }

    fun continueWithFacebook(obj: JSONObject) = viewModelScope.launch {
        socialSignIn(
            email = obj.optString("email", ""),
            firstName = obj.optString("first_name", "Firstname"),
            lastName = obj.optString("last_name", "Lastname"),
            facebookId = obj.getString("id")
        )
    }

    private suspend fun socialSignIn(
        email: String,
        firstName: String,
        lastName: String,
        facebookId: String = ""
    ) {
        _uiState.update { it.copy(socialLoading = true) }
        try {
            val result: AccountResponse =
                restApiService.accountByEmail(EmailAddressRequest(email = email))

            if (result.accountId != null) {
                completeLogin(accountId = result.accountId)

                if (facebookId.isNotBlank()) {
                    // Update facebook id, if needed
                    restApiService.userProfile(AccountIdRequest(accountId = result.accountId)).let {
                        if (it.facebookId != facebookId) {
                            restApiService.updateProfile(
                                UpdateProfileRequest(
                                    accountId = result.accountId,
                                    email = email,
                                    phone = it.phoneNumber ?: "",
                                    nickname = it.nickname ?: "",
                                    communityService = it.communityService ?: false,
                                    imgUrl = it.imgUrl ?: "",
                                    facebookId = facebookId
                                )
                            )
                        }
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        emailAddress = email,
                        firstName = firstName,
                        lastName = lastName,
                        facebookId = facebookId,
                    )
                }
                _eventFlow.emit(LoginScreenEvent.NeedsRegistration)
            }
        } catch (t: Throwable) {
            Timber.e(t, "Social sign in failed")
            _eventFlow.emit(LoginScreenEvent.UnexpectedError)
        } finally {
            _uiState.update { it.copy(socialLoading = false) }
        }
    }

    fun onLogin(
        newEmailAddress: String,
        newPassword: String
    ) = viewModelScope.launch {
        onEmailAddressValueChange(newEmailAddress)
        onPasswordValueChange(newPassword)
        performLogin(manualLogin = false)
    }

    fun onEmailAddressValueChange(newEmailAddress: String) {
        _uiState.update { state ->
            state.copy(
                emailAddress = newEmailAddress.trim(),
                emailAddressValidationMessageId = 0,
                authenticationErrorMessage = "",
                authenticationErrorMessageId = 0,
            )
        }
    }

    fun onPasswordValueChange(newPassword: String) {
        _uiState.update { state ->
            state.copy(
                password = newPassword,
                authenticationErrorMessage = "",
                authenticationErrorMessageId = 0
            )
        }
    }

    private fun validateCredentials(): Boolean {
        var isValid = true
        var emailAddressValidationMessageId = 0
        var authenticationErrorMessageId = 0

        val localState = _uiState.value

        with(localState) {
            if (!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
                emailAddressValidationMessageId = R.string.invalid_email
                isValid = false
            }

            if (password.isBlank()) {
                authenticationErrorMessageId = R.string.auth_error
                isValid = false
            }
        }

        _uiState.update {
            it.copy(
                emailAddressValidationMessageId = emailAddressValidationMessageId,
                authenticationErrorMessageId = authenticationErrorMessageId,
                authenticationErrorMessage = ""
            )
        }
        return isValid
    }

    private fun setAuthenticationError(errorMessage: String?) {
        _uiState.update {
            it.copy(
                authenticationErrorMessage = errorMessage ?: "",
                authenticationErrorMessageId = if (errorMessage.isNullOrBlank()) R.string.auth_error else 0
            )
        }
    }

    private suspend fun completeLogin(accountId: Long, manualLogin: Boolean = false) {
        sharedPreferences.edit().putLong(context.getString(R.string.wfr_account_id), accountId)
            .apply()

        _eventFlow.emit(
            when (manualLogin) {
                true -> LoginScreenEvent.LoginCompleteManual
                else -> LoginScreenEvent.LoginComplete
            }
        )
    }

}