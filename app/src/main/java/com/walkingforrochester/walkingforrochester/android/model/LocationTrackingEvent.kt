package com.walkingforrochester.walkingforrochester.android.model

import android.location.Location

data class LocationTrackingEvent(
    val event: LocationTrackingEventType,
    val locations: List<Location> = listOf(),
)

enum class LocationTrackingEventType {
    Location, Stop
}