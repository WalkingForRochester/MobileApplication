package com.walkingforrochester.walkingforrochester.android.model

import android.location.Location
import com.google.android.gms.maps.model.LatLng

data class LocationTrackingEvent(
    val event: LocationTrackingEventType,
    val locations: List<Location> = listOf(),
    val selectedAddress: LatLng? = null
)

enum class LocationTrackingEventType {
    Location, Stop, SelectedAddress
}