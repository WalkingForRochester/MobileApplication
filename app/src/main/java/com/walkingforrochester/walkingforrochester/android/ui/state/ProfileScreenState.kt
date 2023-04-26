package com.walkingforrochester.walkingforrochester.android.ui.state

import android.net.Uri

data class ProfileScreenState(
    var accountId: Long = 0L,
    var email: String = "",
    var emailValidationMessage: String = "",
    var phone: String = "",
    var phoneValidationMessage: String = "",
    var nickname: String = "",
    var profilePic: String = "",
    var localProfilePicUri: Uri? = null,
    var tooLargeImage: Boolean = false,
    var profilePicValidationMessage: String = "",
    var editProfile: Boolean = false,
    var distanceToday: Double = 0.0,
    var distanceOverall: Double = 0.0,
    var durationToday: Long = 0L,
    var durationOverall: Long = 0L,
    var profileDataLoading: Boolean = false,
    var profileDataSaving: Boolean = false,
    var communityService: Boolean = false,
    var facebookId: String? = null
)

enum class ProfileScreenEvent {
    Logout
}