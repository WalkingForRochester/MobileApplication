package com.walkingforrochester.walkingforrochester.android.ui.state

import android.net.Uri
import com.google.android.gms.maps.model.LatLng

data class LogAWalkState(
   // val walking: Boolean = false,
    //val locationRationalShown: Boolean = false,
    //val notificationRationalShown: Boolean = false,
    //val path: List<LatLng> = mutableListOf(),
    //val startTimestamp: Long = 0,
    //val lastSimplificationTimestamp: Long = 0,
    //val duration: Long = 0,
    //val distanceMiles: Double = 0.0,
    @Deprecated("")
    val surveyDialogState: SurveyDialogState = SurveyDialogState(),
    val loading: Boolean = false,
    //val encodedPath: String = "",
    //val startingPoint: LatLng? = null,
    //val finishingPoint: LatLng? = null,
    //val mockLocation: Boolean = false,
    //val movingTooFast: Boolean = false,
)

data class SurveyDialogState(
    val showDialog: Boolean = false,
    val pickedUpLitter: Boolean = false,
    val bagsCollected: Int = 0,
    val showCamera: Boolean = false,
    val cameraRationalShown: Boolean = false,
    val picUri: Uri? = null
)

enum class LogAWalkEvent {
    StartWalking,
    StopWalking,
    WalkCompleted,
    Submitted,
    MockLocationDetected,
    MovingTooFast,
    CameraRationalError,
    UnexpectedError
}