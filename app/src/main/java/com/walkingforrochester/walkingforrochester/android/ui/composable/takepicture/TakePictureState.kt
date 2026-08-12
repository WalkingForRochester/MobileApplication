package com.walkingforrochester.walkingforrochester.android.ui.composable.takepicture

import android.net.Uri
import androidx.camera.core.SurfaceRequest

data class TakePictureState(
    val event: TakePictureEvent = TakePictureEvent.CaptureImage,
    val imageUri: Uri = Uri.EMPTY,
    val surfaceRequest: SurfaceRequest? = null,
    val error: TakePictureError = TakePictureError.None
)

enum class TakePictureEvent {
    CaptureImage, ConfirmImage, ImageConfirmed
}

enum class TakePictureError {
    None, CaptureError, ConfirmError
}