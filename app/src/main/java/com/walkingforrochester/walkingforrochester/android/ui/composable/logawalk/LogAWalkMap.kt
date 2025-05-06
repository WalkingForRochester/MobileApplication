package com.walkingforrochester.walkingforrochester.android.ui.composable.logawalk

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.modifier.backgroundInPreview
import com.walkingforrochester.walkingforrochester.android.model.LocationData
import com.walkingforrochester.walkingforrochester.android.model.WalkData
import com.walkingforrochester.walkingforrochester.android.model.WalkData.WalkState
import com.walkingforrochester.walkingforrochester.android.ui.theme.MapPathBlue
import timber.log.Timber


@SuppressLint("MissingPermission")
@Composable
fun LogAWalkMap(
    currentLocation: LatLng,
    currentWalk: WalkData,
    modifier: Modifier = Modifier,
    showCurrentLocation: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val cameraPositionState = rememberCameraPositionState()
    var lastLocation by rememberSaveable { mutableStateOf(LocationData.DEFAULT.latLng) }
    var followCamera by rememberSaveable { mutableStateOf(true) }

    if (cameraPositionState.isMoving &&
        cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE
    ) {
        followCamera = false
    }

    LaunchedEffect(followCamera, currentLocation) {
        if (followCamera && currentLocation != lastLocation) {
            val dist = SphericalUtil.computeDistanceBetween(currentLocation, lastLocation)
            val cameraUpdate = CameraUpdateFactory.newLatLng(currentLocation)
            when {
                lastLocation == LocationData.DEFAULT.latLng -> {
                    Timber.d("Initial move: %s", currentLocation)
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(currentLocation, 16f)
                    )
                }

                dist < 1000 -> {
                    Timber.d("animating to %s %s", currentLocation, lastLocation)
                    cameraPositionState.animate(update = cameraUpdate)
                }

                else -> {
                    // Moving if large distance change.
                    Timber.d("moving to %s %s", currentLocation, lastLocation)
                    cameraPositionState.move(update = cameraUpdate)
                }
            }
            lastLocation = currentLocation
        } else {
            Timber.d("skipping %s = %s ", currentLocation, lastLocation)
            lastLocation = currentLocation
        }
    }

    GoogleMap(
        modifier = modifier.backgroundInPreview(Color.Gray),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = showCurrentLocation,
            maxZoomPreference = 20f,
            minZoomPreference = 9f
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = showCurrentLocation,
            zoomGesturesEnabled = showCurrentLocation
        ),
        onMyLocationButtonClick = {
            followCamera = true
            // indicate camera should animate to current location
            false
        },
        contentPadding = contentPadding
    ) {
        RenderWalkDataOnMap(currentWalk)
    }
}

@Composable
@GoogleMapComposable
fun RenderWalkDataOnMap(currentWalk: WalkData) {
    val context = LocalContext.current
    val startingMarkerState = rememberUpdatedMarkerState()
    if (currentWalk.state == WalkState.IN_PROGRESS ||
        currentWalk.state == WalkState.COMPLETE
    ) {
        startingMarkerState.position = currentWalk.startPosition
        val width = with(LocalDensity.current) { 6.dp.toPx() }

        Marker(
            state = startingMarkerState,
            anchor = Offset(0.5f, 0.5f),
            icon = remember(R.drawable.trip_origin) {
                getMarkerIconFromDrawable(
                    drawable = AppCompatResources.getDrawable(
                        context,
                        R.drawable.trip_origin
                    )
                )
            },
            zIndex = 10f
        )

        Polyline(
            points = currentWalk.path,
            color = MapPathBlue,
            width = width,
            startCap = RoundCap(),
            endCap = RoundCap(),

        )
    }

    val finishingCapMarkerState = rememberUpdatedMarkerState()
    val finishingMarkerState = rememberUpdatedMarkerState()

    if (currentWalk.endPosition != LocationData.DEFAULT.latLng) {
        finishingCapMarkerState.position = currentWalk.endPosition
        finishingMarkerState.position = currentWalk.endPosition

        Marker(
            state = finishingCapMarkerState,
            anchor = Offset(0.5f, 0.5f),
            icon = remember(R.drawable.trip_destination_cap) {
                getMarkerIconFromDrawable(
                    drawable = AppCompatResources.getDrawable(
                        context,
                        R.drawable.trip_destination_cap
                    )
                )
            },
            zIndex = 11f
        )

        Marker(
            state = finishingMarkerState,
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
            zIndex = 12f
        )
    }
}

private fun getMarkerIconFromDrawable(
    drawable: Drawable?
): BitmapDescriptor {
    if (drawable == null) return BitmapDescriptorFactory.defaultMarker()
    val canvas = Canvas()
    val width = drawable.intrinsicWidth
    val height = drawable.intrinsicHeight
    val bitmap = createBitmap(width = width, height = height)
    canvas.setBitmap(bitmap)
    drawable.setBounds(0, 0, width, height)

    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
