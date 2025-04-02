package com.walkingforrochester.walkingforrochester.android.model

import android.net.Uri
import androidx.compose.runtime.Stable
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.walkingforrochester.walkingforrochester.android.ktx.toFixedBounds

@Stable
data class WalkData(
    val state: WalkState = WalkState.IDLE,
    val startTime: Long = System.currentTimeMillis(),
    val durationMilli: Long = 0,
    val distanceMeters: Double = 0.0,
    val startPosition: LatLng = DEFAULT_POSITION,
    val endPosition: LatLng = DEFAULT_POSITION,
    val path: List<LatLng> = emptyList<LatLng>(),
    val bounds: LatLngBounds = DEFAULT_BOUNDS,
    val bagsOfLitter: Int = 0,
    val imageUri: Uri = Uri.EMPTY
) {
    enum class WalkState {
        IDLE,
        IN_PROGRESS,
        COMPLETE,
        MOCK_LOCATION_DETECTED,
        SPEEDING_DETECTED
    }

    companion object {
        val DEFAULT_POSITION = LatLng(0.0, 0.0)
        val DEFAULT_BOUNDS = DEFAULT_POSITION.toFixedBounds(100.0)
    }
}

