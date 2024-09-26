package com.walkingforrochester.walkingforrochester.android.ui.composable.logawalk

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.walkingforrochester.walkingforrochester.android.ui.theme.MapPathBlue
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import kotlin.math.roundToInt


@SuppressLint("MissingPermission")
@Composable
fun LogAWalkMap(
    modifier: Modifier = Modifier,
    toggleCameraFollow: (Boolean) -> Boolean,
    followCamera: Boolean,
    lastLocation: LatLng? = null,
    selectedAddressLocation: LatLng? = null,
    path: List<LatLng> = listOf(),
    startingPoint: LatLng? = null,
    finishingPoint: LatLng? = null,
    contentPadding: PaddingValues = PaddingValues()
) {
    val context = LocalContext.current
    val cameraPosition = rememberCameraPositionState()

    if (cameraPosition.isMoving && cameraPosition.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
        toggleCameraFollow(false)
    }

    LaunchedEffect(Unit) {
        LocationServices
            .getFusedLocationProviderClient(context)
            .lastLocation.addOnSuccessListener { location ->
                location?.let {
                    cameraPosition.position =
                        CameraPosition.fromLatLngZoom(LatLng(it.latitude, it.longitude), 16f)
                }
            }
        LocationServices
            .getFusedLocationProviderClient(context)
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                location?.let {
                    cameraPosition.position =
                        CameraPosition.fromLatLngZoom(LatLng(it.latitude, it.longitude), 16f)
                }
            }
    }

    LaunchedEffect(followCamera, lastLocation) {
        if (followCamera && lastLocation != null) {
            cameraPosition.animate(CameraUpdateFactory.newLatLng(lastLocation), Int.MAX_VALUE)
        }
    }

    LaunchedEffect(selectedAddressLocation) {
        selectedAddressLocation?.let {
            cameraPosition.animate(CameraUpdateFactory.newLatLng(it), Int.MAX_VALUE)
        }
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPosition,
            properties = MapProperties(isMyLocationEnabled = true),
            onMyLocationButtonClick = {
                toggleCameraFollow(true)
            },
            contentPadding = contentPadding
        ) {
            startingPoint?.let {
                Marker(
                    state = rememberMarkerState(position = it),
                    icon = getMarkerIconFromDrawable(LocalContext.current.getDrawable(com.walkingforrochester.walkingforrochester.android.R.drawable.ic_walk)!!)
                )
            }

            Polyline(
                points = path,
                color = MapPathBlue,
                width = 20f,
                startCap = RoundCap(),
                endCap = RoundCap()
            )

            finishingPoint?.let {
                Marker(
                    state = rememberMarkerState(position = it),
                    icon = getMarkerIconFromDrawable(LocalContext.current.getDrawable(com.walkingforrochester.walkingforrochester.android.R.drawable.ic_finish)!!)
                )
            }

            selectedAddressLocation?.let {
                val markerState = rememberMarkerState()
                markerState.position = it
                Marker(
                    state = markerState,
                    icon = getMarkerIconFromDrawable(
                        drawable = LocalContext.current.getDrawable(com.walkingforrochester.walkingforrochester.android.R.drawable.ic_location)!!,
                        color = Color.Red
                    )
                )
            }
        }
    }
}

private fun getMarkerIconFromDrawable(
    drawable: Drawable,
    color: Color = Color.Black
): BitmapDescriptor {
    val canvas = Canvas()
    val bitmap = Bitmap.createBitmap(
        (drawable.intrinsicWidth * 1.5).roundToInt(),
        (drawable.intrinsicHeight * 1.5).roundToInt(),
        Bitmap.Config.ARGB_8888
    )
    canvas.setBitmap(bitmap)
    drawable.setBounds(
        0, 0, (drawable.intrinsicWidth * 1.5).roundToInt(),
        (drawable.intrinsicHeight * 1.5).roundToInt()
    )
    drawable.colorFilter = PorterDuffColorFilter(color.hashCode(), PorterDuff.Mode.MULTIPLY)
    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

@Preview(showBackground = true)
@Composable
fun PreviewWalkMap() {
    WalkingForRochesterTheme {
        //WalkMap()
    }
}