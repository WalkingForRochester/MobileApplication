package com.walkingforrochester.walkingforrochester.android.model

data class PermissionPreferences(
    val locationRationalShown: Boolean = false,
    val notificationRationalShown: Boolean = false,
    val cameraRationalShown: Boolean = false
)