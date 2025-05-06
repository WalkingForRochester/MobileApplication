package com.walkingforrochester.walkingforrochester.android.viewmodel

import android.util.Patterns
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.model.AccountProfile
import com.walkingforrochester.walkingforrochester.android.model.ProfileException
import com.walkingforrochester.walkingforrochester.android.repository.NetworkRepository
import com.walkingforrochester.walkingforrochester.android.repository.PreferenceRepository
import com.walkingforrochester.walkingforrochester.android.ui.state.LoginScreenEvent
import com.walkingforrochester.walkingforrochester.android.ui.state.LoginScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
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
    private val networkRepository: NetworkRepository,
    private val preferenceRepository: PreferenceRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginScreenState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<LoginScreenEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        // Restore email from saved state. Not doing password for security reasons.
        _uiState.update {
            it.copy(
                emailAddress = savedStateHandle[EMAIL_KEY] ?: "",
            )
        }
    }

    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->

        if (throwable is ProfileException) {
            Timber.e("Login failed: %s", throwable.message)
            setAuthenticationError(throwable.message)
        } else {
            Timber.e(throwable, "Unexpected error processing login")
        }

        if (!_eventFlow.tryEmit(LoginScreenEvent.UnexpectedError)) {
            Timber.w("Failed to report error due to no listener")
        }

        _uiState.update {
            it.copy(loading = false)
        }
    }

    fun onLoginClicked() = viewModelScope.launch(context = exceptionHandler) {
        performLogin(manualLogin = true)
    }

    fun continueWithGoogle(
        googleCredential: GoogleIdTokenCredential
    ) = viewModelScope.launch(context = exceptionHandler) {
        socialSignIn(
            email = googleCredential.id,
            firstName = googleCredential.givenName ?: "",
            lastName = googleCredential.familyName ?: "",
        )
    }

    private suspend fun performLogin(manualLogin: Boolean) {
        _uiState.update { it.copy(loading = true) }

        if (validateCredentials()) {
            with(_uiState.value) {
                Timber.d("Performing login request")
                val accountId = networkRepository.performLogin(
                    email = emailAddress,
                    password = password
                )

                completeLogin(accountId, manualLogin)
            }
        }
        _uiState.update { it.copy(loading = false) }
    }

    fun continueWithFacebook(
        obj: JSONObject
    ) = viewModelScope.launch(context = exceptionHandler) {
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
        _uiState.update { it.copy(loading = true) }
        val accountId = networkRepository.fetchAccountId(email = email)

        if (accountId != AccountProfile.NO_ACCOUNT) {
            completeLogin(accountId = accountId)

            if (facebookId.isNotBlank()) {
                // Update facebook id, if needed
                val profile = networkRepository.fetchProfile(accountId = accountId)

                if (profile.facebookId != facebookId) {
                    networkRepository.updateProfile(profile.copy(facebookId = facebookId))
                }
            }
        } else {
            // Account doesn't exist, so prepopulate registration
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
        _uiState.update { it.copy(loading = false) }
    }

    fun onCredentialLogin(
        newEmailAddress: String,
        newPassword: String
    ) = viewModelScope.launch(context = exceptionHandler) {
        onEmailAddressValueChange(newEmailAddress)
        onPasswordValueChange(newPassword)
        performLogin(manualLogin = false)
        savedStateHandle[EMAIL_KEY] = newEmailAddress
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
        savedStateHandle[EMAIL_KEY] = newEmailAddress
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
        preferenceRepository.updateAccountId(accountId = accountId)

        _eventFlow.emit(
            when (manualLogin) {
                true -> LoginScreenEvent.LoginCompleteManual
                else -> LoginScreenEvent.LoginComplete
            }
        )
    }

    companion object {
        private const val EMAIL_KEY = "email"
    }
}