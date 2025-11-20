package com.walkingforrochester.walkingforrochester.android.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Patterns
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.di.IODispatcher
import com.walkingforrochester.walkingforrochester.android.formatDouble
import com.walkingforrochester.walkingforrochester.android.formatElapsedMilli
import com.walkingforrochester.walkingforrochester.android.ktx.compressImage
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
import timber.log.Timber
import java.io.File
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val networkRepository: NetworkRepository,
    private val preferenceRepository: PreferenceRepository,
    @param:IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _accountProfile = MutableStateFlow(AccountProfile.DEFAULT_PROFILE)
    val accountProfile = _accountProfile.asStateFlow()

    private val _uiState = MutableStateFlow(ProfileScreenState())
    val uiState = _uiState.asStateFlow()

    // Because the network error may occur BEF
    private val _eventFlow = MutableSharedFlow<ProfileScreenEvent>(
        // Using capacity of one to allow exception handler to emit outside of coroutine
        extraBufferCapacity = 1,
    )
    val eventFlow = _eventFlow.asSharedFlow()

    private var previousProfile = AccountProfile.DEFAULT_PROFILE

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
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
            networkRepository.updateProfile(profile)
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
                type = "image/png"
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

        val profileUri = when (uri) {
            null -> null
            else -> compressFile(uri)
        }

        _uiState.update { it.copy(localProfilePicUri = profileUri) }
        updateSavedState()
    }

    private fun compressFile(uri: Uri): Uri? {
        val choiceFile = File(context.cacheDir, CHOICE_FILE_NAME)
        var copied = false
        context.contentResolver.openInputStream(uri).use { ios ->
            ios?.let {
                choiceFile.outputStream().use { os ->
                    ios.copyTo(os)
                    copied = true
                }
            }
        }

        if (!copied) return null

        val confirmFile = File(context.cacheDir, CONFIRM_FILE_NAME)

        val compressed = choiceFile.compressImage(
            targetFile = confirmFile,
            targetWidth = TARGET_DIMENSION,
            targetHeight = TARGET_DIMENSION
        )

        choiceFile.delete()
        return if (compressed) confirmFile.toUri() else null
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
        val resources = context.resources
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(context.packageName)
            .appendPath(resources.getResourceTypeName(R.drawable.wfr_logo))
            .appendPath(resources.getResourceEntryName(R.drawable.wfr_logo))
            .build()
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
        private const val EDIT_PROFILE_KEY = "editProfile"
        private const val EMAIL_KEY = "email"
        private const val PHONE_KEY = "phoneNumber"
        private const val NICKNAME_KEY = "nickName"
        private const val PROFILE_IMAGE_KEY = "profileImage"

        private const val CHOICE_FILE_NAME = "wfr_profile_choice.jpg"
        private const val CONFIRM_FILE_NAME = "wfr_profile_compress.jpg"

        private const val TARGET_DIMENSION = 500
    }
}