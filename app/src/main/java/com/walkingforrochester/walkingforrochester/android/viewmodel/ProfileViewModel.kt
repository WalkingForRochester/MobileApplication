package com.walkingforrochester.walkingforrochester.android.viewmodel

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Patterns
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.di.IODispatcher
import com.walkingforrochester.walkingforrochester.android.formatDouble
import com.walkingforrochester.walkingforrochester.android.formatElapsedMilli
import com.walkingforrochester.walkingforrochester.android.model.AccountProfile
import com.walkingforrochester.walkingforrochester.android.repository.NetworkRepository
import com.walkingforrochester.walkingforrochester.android.repository.PreferenceRepository
import com.walkingforrochester.walkingforrochester.android.ui.state.ProfileScreenEvent
import com.walkingforrochester.walkingforrochester.android.ui.state.ProfileScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkRepository: NetworkRepository,
    private val preferenceRepository: PreferenceRepository,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _accountProfile = MutableStateFlow<AccountProfile>(AccountProfile.DEFAULT_PROFILE)
    val accountProfile = _accountProfile.asStateFlow()

    private val _uiState = MutableStateFlow(ProfileScreenState())
    val uiState = _uiState.asStateFlow()

    // Because the network error may occur BEF
    private val _eventFlow = MutableSharedFlow<ProfileScreenEvent>(
        extraBufferCapacity = 1,
        //onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val eventFlow = _eventFlow.asSharedFlow()

    private var previousProfile = AccountProfile.DEFAULT_PROFILE

    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        Timber.e(throwable, "Unexpected error processing profile")

        if (!_eventFlow.tryEmit(ProfileScreenEvent.UnexpectedError)) {
            Timber.w("Failed to report error due to no listener")
        }

        _uiState.update {
            it.copy(
                profileDataLoading = false,
                profileDataSaving = false
            )
        }
    }

    fun loadProfile() = viewModelScope.launch(context = exceptionHandler) {
        _uiState.update { it.copy(profileDataLoading = true) }
        refreshProfile()
        recoverSavedState()
        _uiState.update { it.copy(profileDataLoading = false) }
    }

    private suspend fun refreshProfile() {
        val accountId = preferenceRepository.fetchAccountId()
        val profile = if (accountId != AccountProfile.NO_ACCOUNT) {
            networkRepository.fetchProfile(accountId)
        } else {
            AccountProfile.DEFAULT_PROFILE
        }
        _accountProfile.update { profile }
    }

    fun onProfileChange(accountProfile: AccountProfile) {
        val oldProfile = _accountProfile.value

        if (accountProfile.email != oldProfile.email) {
            _uiState.update { it.copy(emailValidationMessageId = 0) }
        }
        if (accountProfile.phoneNumber != oldProfile.phoneNumber) {
            _uiState.update { it.copy(phoneValidationMessageId = 0) }
        }
        _accountProfile.update {
            it.copy(
                email = accountProfile.email.trim(),
                phoneNumber = accountProfile.phoneNumber.filter { it.isDigit() },
                nickname = accountProfile.nickname.filter { it != '\n' },
                communityService = accountProfile.communityService
            )
        }

        updateSavedState()
    }

    fun onEdit() {
        previousProfile = _accountProfile.value.copy()
        _uiState.update { it.copy(editProfile = true) }
        updateSavedState()
    }

    fun onSave() = viewModelScope.launch(context = exceptionHandler) {
        if (validateForm()) {
            _uiState.update { it.copy(profileDataSaving = true) }

            var profile = _accountProfile.value
            _uiState.value.localProfilePicUri?.let {
                val profilePic = networkRepository.uploadProfileImage(profile.accountId, it)
                if (profilePic.isNotBlank()) {
                    profile = profile.copy(imageUrl = profilePic)
                }
            }

            Timber.d("Updating profile")
            networkRepository.updateProfile(_accountProfile.value)
            Timber.d("Refresh profile")
            refreshProfile()

            _uiState.update {
                it.copy(
                    localProfilePicUri = null,
                    editProfile = false,
                    profileDataSaving = false
                )
            }

            updateSavedState()
        }
    }

    fun onCancel() = viewModelScope.launch(context = exceptionHandler) {
        _accountProfile.update { previousProfile }
        _uiState.update {
            it.copy(
                localProfilePicUri = null,
                editProfile = false
            )
        }
        updateSavedState()
    }

    fun onShare(context: Context): Intent {
        with(_accountProfile.value) {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/jpeg"
                putExtra(
                    Intent.EXTRA_STREAM, getLogo(context)
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Check out my stats with Walking For Rochester!\nDistance, last walk: ${
                        distanceToday.formatDouble()
                    } mi. overall: ${totalDistance.formatDouble()} mi\nDuration, last walk: ${
                        durationToday.formatElapsedMilli()
                    }. overall: ${totalDuration.formatElapsedMilli()}"
                )
            }
            return Intent.createChooser(sendIntent, "Share statistics")
        }
    }

    fun onChoosePhoto(
        uri: Uri?
    ) = viewModelScope.launch(context = ioDispatcher + exceptionHandler) {
        var tooLarge = false
        uri?.let {
            context.contentResolver.openAssetFileDescriptor(it, "r").use { fd ->
                fd?.let { fileDescriptor ->
                    if (fileDescriptor.length > FILE_SIZE_LIMIT) {
                        tooLarge = true
                    }
                }
            }
        }
        _uiState.update { it.copy(localProfilePicUri = uri, tooLargeImage = tooLarge) }
        updateSavedState()
    }

    fun onLogout() = viewModelScope.launch(context = exceptionHandler) {
        preferenceRepository.removeAccountInfo()
        _eventFlow.emit(ProfileScreenEvent.Logout)
    }

    fun onDeleteAccount() = viewModelScope.launch(context = exceptionHandler) {
        Timber.d("Deleting account...")
        val accountId = _accountProfile.value.accountId
        networkRepository.deleteUser(accountId)
        // If no errors, treat as a logout...
        preferenceRepository.removeAccountInfo()
        _eventFlow.emit(ProfileScreenEvent.AccountDeleted)
    }


    private suspend fun validateForm(): Boolean {
        var isValid = true
        var emailValidationMessageId = 0
        var phoneValidationMessageId = 0

        with(_accountProfile.value) {
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailValidationMessageId = R.string.invalid_email
                isValid = false
            } else if (
                email != previousProfile.email && networkRepository.isEmailInUse(email)
            ) {
                emailValidationMessageId = R.string.email_in_use
                isValid = false
            }
            if (phoneNumber.length != 10) {
                phoneValidationMessageId = R.string.invalid_phone
                isValid = false
            }
        }

        _uiState.update {
            it.copy(
                emailValidationMessageId = emailValidationMessageId,
                phoneValidationMessageId = phoneValidationMessageId
            )
        }
        return isValid
    }

    private fun getLogo(context: Context): Uri? {
        val drawable =
            AppCompatResources.getDrawable(context, R.drawable.wfr_logo) as? BitmapDrawable
        val bitmap = drawable?.bitmap ?: return null
        return runBlocking(ioDispatcher) {
            try {
                val file = File(
                    context.cacheDir,
                    "wfr.jpeg"
                )
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }

                FileProvider.getUriForFile(
                    context,
                    context.packageName,
                    file
                )
            } catch (e: IOException) {
                Timber.e("unable to write logo file: %s", e.message)
                null
            }
        }
    }

    private fun recoverSavedState() {
        val editProfile: Boolean = true == savedStateHandle[EDIT_PROFILE_KEY]
        if (editProfile) {
            previousProfile = _accountProfile.value.copy()
            _uiState.update {
                it.copy(
                    editProfile = true,
                    localProfilePicUri = savedStateHandle[PROFILE_IMAGE_KEY]
                )
            }
            _accountProfile.update {
                it.copy(
                    email = savedStateHandle[EMAIL_KEY] ?: it.email,
                    phoneNumber = savedStateHandle[PHONE_KEY] ?: it.phoneNumber,
                    nickname = savedStateHandle[NICKNAME_KEY] ?: it.nickname
                )
            }
        }
    }

    private fun updateSavedState() {
        val uiState = _uiState.value
        val accountProfile = _accountProfile.value

        savedStateHandle[EDIT_PROFILE_KEY] = uiState.editProfile
        savedStateHandle[EMAIL_KEY] = accountProfile.email
        savedStateHandle[PHONE_KEY] = accountProfile.phoneNumber
        savedStateHandle[NICKNAME_KEY] = accountProfile.nickname
        savedStateHandle[PROFILE_IMAGE_KEY] = uiState.localProfilePicUri
    }

    companion object {
        const val FILE_SIZE_LIMIT: Long = 20 * 1024 * 1024 // 20 megabytes

        private val EDIT_PROFILE_KEY = "editProfile"
        private val EMAIL_KEY = "email"
        private val PHONE_KEY = "phoneNumber"
        private val NICKNAME_KEY = "nickName"
        private val PROFILE_IMAGE_KEY = "profileImage"
    }
}