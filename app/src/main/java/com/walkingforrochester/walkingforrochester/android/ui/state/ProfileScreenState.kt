package com.walkingforrochester.walkingforrochester.android.ui.state

import android.net.Uri
import androidx.annotation.StringRes

data class ProfileScreenState(
    val accountId: Long = 0L,
    val email: String = "",
    @StringRes val emailValidationMessageId: Int = 0,
    val phone: String = "",
    @StringRes val phoneValidationMessageId: Int = 0,
    val nickname: String = "",
    val profilePic: String = "",
    val localProfilePicUri: Uri? = null,
    val tooLargeImage: Boolean = false,
    val editProfile: Boolean = false,
    val distanceToday: Double = 0.0,
    val distanceOverall: Double = 0.0,
    val durationToday: Long = 0L,
    val durationOverall: Long = 0L,
    val profileDataLoading: Boolean = false,
    val profileDataSaving: Boolean = false,
    val communityService: Boolean = false,
    val facebookId: String? = null
)

enum class ProfileScreenEvent {
    Logout,
    AccountDeleted,
    UnexpectedError
}