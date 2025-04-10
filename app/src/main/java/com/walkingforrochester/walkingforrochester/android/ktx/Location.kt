package com.walkingforrochester.walkingforrochester.android.ktx

import android.location.Location
import androidx.core.location.LocationCompat
import com.google.android.gms.maps.model.LatLng
import com.walkingforrochester.walkingforrochester.android.model.LocationData

fun Location.toLocationData(): LocationData {
    return LocationData(
        latLng = LatLng(latitude, longitude),
        isMock = LocationCompat.isMock(this),
    )
}