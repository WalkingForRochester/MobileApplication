package com.walkingforrochester.walkingforrochester.android.viewmodel

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.text.format.DateUtils
import android.util.Patterns
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.WFRDateFormatter
import com.walkingforrochester.walkingforrochester.android.md5
import com.walkingforrochester.walkingforrochester.android.network.RestApiService
import com.walkingforrochester.walkingforrochester.android.network.request.AccountIdRequest
import com.walkingforrochester.walkingforrochester.android.network.request.EmailAddressRequest
import com.walkingforrochester.walkingforrochester.android.network.request.UpdateProfileRequest
import com.walkingforrochester.walkingforrochester.android.roundDouble
import com.walkingforrochester.walkingforrochester.android.ui.state.ProfileScreenEvent
import com.walkingforrochester.walkingforrochester.android.ui.state.ProfileScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject

const val FILE_SIZE_LIMIT: Long = 20 * 1024 * 1024 // 20 megabytes

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val restApiService: RestApiService,
    @ApplicationContext private val context: Context,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileScreenState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ProfileScreenEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var previousState = ProfileScreenState()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(profileDataLoading = true) }
            try {
                val accountId =
                    sharedPreferences.getLong(context.getString(R.string.wfr_account_id), 0)

                val result = restApiService.userProfile(AccountIdRequest(accountId))

                if (result.error == null) {
                    _uiState.update {
                        it.copy(
                            accountId = accountId,
                            email = result.email ?: "",
                            phone = result.phoneNumber ?: "",
                            nickname = result.nickname ?: "",
                            communityService = result.communityService ?: false,
                            profilePic = result.imgUrl ?: "",
                            distanceToday = result.distance ?: 0.0,
                            distanceOverall = result.totalDistance ?: 0.0,
                            durationToday = result.duration ?: 0L,
                            durationOverall = result.totalDuration ?: 0L,
                            facebookId = result.facebookId,
                            profileDataLoading = false
                        )
                    }
                } else {
                    throw RuntimeException("Couldn't get profile data: ${result.error}")
                }
            } catch (t: Throwable) {
                Timber.e(t, "Unable to initialize ProfileViewModel")
                _eventFlow.emit(ProfileScreenEvent.UnexpectedError)
                _uiState.update { it.copy(profileDataLoading = false) }
            }
        }
    }

    fun onEmailChange(newEmail: String) =
        _uiState.update { state ->
            state.copy(
                email = newEmail.trim(),
                emailValidationMessageId = 0
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

    fun onCommunityServiceChange(newCommunityService: Boolean) =
        _uiState.update { it.copy(communityService = newCommunityService) }

    fun onEdit() = viewModelScope.launch {
        previousState = _uiState.value.copy()
        _uiState.update { it.copy(editProfile = true) }
    }

    fun onSave() = viewModelScope.launch {
        if (validateForm()) {
            _uiState.update { it.copy(profileDataSaving = true) }
            try {
                with(_uiState.value) {
                    var fileName: String?
                    localProfilePicUri?.let {
                        fileName = "IMG_PROFILE_${
                            LocalDate.now().format(WFRDateFormatter.formatter)
                        }_${md5(accountId.toString())}"
                        if (it.path != null) {
                            context.contentResolver.openInputStream(it)?.use { inputStream ->
                                val body: MultipartBody.Part =
                                    MultipartBody.Part.createFormData(
                                        "file",
                                        fileName,
                                        inputStream.readBytes()
                                            .toRequestBody("form-data".toMediaTypeOrNull())
                                    )
                                restApiService.uploadImage(body)
                                _uiState.update { state ->
                                    state.copy(
                                        profilePic =
                                        "https://walkingforrochester.com/images/profile/$fileName.jpg"
                                    )
                                }
                            }
                        }
                    }
                    restApiService.updateProfile(
                        UpdateProfileRequest(
                            accountId = accountId,
                            email = email,
                            phone = phone,
                            nickname = nickname,
                            communityService = communityService,
                            imgUrl = profilePic,
                            facebookId = facebookId
                        )
                    )
                    _uiState.update {
                        it.copy(
                            localProfilePicUri = null,
                            editProfile = false,
                            profileDataSaving = false
                        )
                    }
                }
            } catch (t: Throwable) {
                Timber.e(t, "Unable to save user profile")
                _eventFlow.emit(ProfileScreenEvent.UnexpectedError)
                _uiState.update { it.copy(profileDataSaving = false) }
            }
        }
    }

    fun onCancel() = viewModelScope.launch {
        _uiState.update { previousState.copy(editProfile = false) }
    }

    fun onShare(context: Context): Intent {
        with(_uiState.value) {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/png"
                putExtra(
                    Intent.EXTRA_STREAM, getLogo(context)
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Check out my stats with Walking For Rochester!\nDistance, last walk: ${
                        roundDouble(
                            distanceToday
                        )
                    } mi. overall: ${roundDouble(distanceOverall)} mi\nDuration, last walk: ${
                        DateUtils.formatElapsedTime(durationToday / 1000)
                    }. overall: ${DateUtils.formatElapsedTime(durationOverall / 1000)}"
                )
            }
            return Intent.createChooser(sendIntent, "Share statistics")
        }
    }

    fun setLocalPhotoUri(uri: Uri?) = viewModelScope.launch {
        uri?.let {
            context.contentResolver.openAssetFileDescriptor(it, "r").use { fd ->
                fd?.let { fileDescriptor ->
                    if (fileDescriptor.length > FILE_SIZE_LIMIT) {
                        _uiState.update { state -> state.copy(tooLargeImage = true) }
                        return@launch
                    }
                }
            }
        }
        _uiState.update { it.copy(localProfilePicUri = uri, tooLargeImage = false) }
    }

    fun onLogout() = viewModelScope.launch {
        removeAccountFromPreferences()
        _eventFlow.emit(ProfileScreenEvent.Logout)
    }

    fun onDeleteAccount() = viewModelScope.launch {
        val accountId = _uiState.value.accountId
        try {
            restApiService.deleteUser(AccountIdRequest(accountId = accountId))
            // If no errors, treat as a logout...
            removeAccountFromPreferences()
            _eventFlow.emit(ProfileScreenEvent.AccountDeleted)
        } catch (e: Throwable) {
            Timber.w(e, "Failed to delete account")
            _eventFlow.emit(ProfileScreenEvent.UnexpectedError)
        }
    }

    private fun removeAccountFromPreferences() {
        sharedPreferences.edit()
            .remove(context.getString(R.string.wfr_account_id))
            .remove(context.getString(R.string.wfr_dark_mode_enabled))
            .apply()
    }

    private suspend fun validateForm(): Boolean {
        var isValid = true
        var emailValidationMessageId = 0
        var phoneValidationMessageId = 0

        with(_uiState.value) {
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailValidationMessageId = R.string.invalid_email
                isValid = false
            } else if (
                email != previousState.email &&
                restApiService.accountByEmail(EmailAddressRequest(email = email)).accountId != null
            ) {
                emailValidationMessageId = R.string.email_in_use
                isValid = false
            }
            if (phone.length != 10) {
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
        var bmpUri: Uri? = null
        try {
            val file = File(
                context.cacheDir,
                "wfr.png"
            )
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
            bmpUri =
                androidx.core.content.FileProvider.getUriForFile(context, context.packageName, file)
        } catch (e: IOException) {
            Timber.e("unable to write logo file")
        }
        return bmpUri
    }

}