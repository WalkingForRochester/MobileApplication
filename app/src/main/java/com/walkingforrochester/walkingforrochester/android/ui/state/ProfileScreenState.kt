package com.walkingforrochester.walkingforrochester.android.ui.state

import android.net.Uri
import androidx.annotation.StringRes

data class ProfileScreenState(
    @param:StringRes val emailValidationMessageId: Int = 0,
    @param:StringRes val phoneValidationMessageId: Int = 0,
    val localProfilePicUri: Uri? = null,
    val editProfile: Boolean = false,
    val profileDataLoading: Boolean = false,
    val profileDataSaving: Boolean = false,
)

enum class ProfileScreenEvent {
    Logout,
    AccountDeleted,
    UnexpectedError
}