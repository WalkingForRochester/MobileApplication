package com.walkingforrochester.walkingforrochester.android.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.network.RestApiService
import com.walkingforrochester.walkingforrochester.android.network.request.AccountIdRequest
import com.walkingforrochester.walkingforrochester.android.network.request.EmailAddressRequest
import com.walkingforrochester.walkingforrochester.android.network.request.LoginRequest
import com.walkingforrochester.walkingforrochester.android.network.request.UpdateProfileRequest
import com.walkingforrochester.walkingforrochester.android.network.response.AccountResponse
import com.walkingforrochester.walkingforrochester.android.showUnexpectedErrorToast
import com.walkingforrochester.walkingforrochester.android.ui.state.LoginScreenEvent
import com.walkingforrochester.walkingforrochester.android.ui.state.LoginScreenState
import com.walkingforrochester.walkingforrochester.android.ui.state.RegistrationScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
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

    fun onLoginClicked() = flow<Nothing> {
        _uiState.update { it.copy(loading = true) }
        if (validateCredentials()) {
            with(_uiState.value) {
                val result: AccountResponse =
                    restApiService.login(LoginRequest(emailAddress, password))

                if (result.accountId != null) {
                    completeLogin(result.accountId)
                } else {
                    setAuthenticationError(
                        result.error ?: context.getString(R.string.auth_error)
                    )
                }
            }
        }
    }.catch {
        Timber.e(it, "Login request failed")
        showUnexpectedErrorToast(context)
    }.onCompletion {
        _uiState.update { it.copy(loading = false) }
    }.launchIn(viewModelScope)

    fun continueWithGoogle(gsa: GoogleSignInAccount) = viewModelScope.launch {
        socialSignIn(gsa.email ?: "", gsa.givenName ?: "", gsa.familyName ?: "")
    }

    fun continueWithFacebook(obj: JSONObject) = viewModelScope.launch {
        socialSignIn(
            email = obj.optString("email",""),
            firstName = obj.optString("first_name","Firstname"),
            lastName = obj.optString("last_name","Lastname"),
            facebookId = obj.getString("id")
        )
    }

    private suspend fun socialSignIn(
        email: String,
        firstName: String,
        lastName: String,
        facebookId: String? = null
    ) =
        flow<Nothing> {
            _uiState.update { it.copy(socialLoading = true) }
            val result: AccountResponse =
                restApiService.accountByEmail(EmailAddressRequest(email = email))

            if (result.accountId != null) {
                completeLogin(accountId = result.accountId)

                facebookId?.let {
                    with(restApiService.userProfile(AccountIdRequest(accountId = result.accountId))) {
                        restApiService.updateProfile(
                            UpdateProfileRequest(
                                accountId = result.accountId,
                                email = email,
                                phone = phoneNumber ?: "",
                                nickname = nickname ?: "",
                                communityService = communityService ?: false,
                                imgUrl = imgUrl ?: "",
                                facebookId = facebookId
                            )
                        )
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        registrationScreenState = RegistrationScreenState(
                            email = email,
                            firstName = firstName,
                            lastName = lastName,
                            facebookId = facebookId
                        )
                    )
                }
                _eventFlow.emit(LoginScreenEvent.NeedsRegistration)
            }
        }.catch {
            Timber.e(it, "Social sign in failed")
            showUnexpectedErrorToast(context)
        }.onCompletion {
            _uiState.update { it.copy(socialLoading = false) }
        }.launchIn(viewModelScope)

    fun onEmailAddressValueChange(newEmailAddress: String) {
        _uiState.update { state ->
            state.copy(
                emailAddress = newEmailAddress.trim(),
                emailAddressValidationMessage = "",
                authenticationErrorMessage = ""
            )
        }
    }

    fun onPasswordValueChange(newPassword: String) {
        _uiState.update { state ->
            state.copy(
                password = newPassword,
                passwordValidationMessage = "",
                authenticationErrorMessage = ""
            )
        }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    private fun validateCredentials(): Boolean {
        var isValid = true
        _uiState.update {
            it.copy(
                emailAddressValidationMessage = "",
                passwordValidationMessage = "",
                authenticationErrorMessage = ""
            )
        }
        val localState = _uiState.value.copy()

        with(localState) {
            if (!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
                emailAddressValidationMessage = context.getString(R.string.invalid_email)
                isValid = false
            }
        }

        _uiState.update { localState }
        return isValid
    }

    private fun setAuthenticationError(errorMessage: String) {
        _uiState.update { it.copy(authenticationErrorMessage = errorMessage) }
    }

    private suspend fun completeLogin(accountId: Long) {
        sharedPreferences.edit().putLong(context.getString(R.string.wfr_account_id), accountId)
            .apply()
        _eventFlow.emit(LoginScreenEvent.LoginComplete)
    }

}