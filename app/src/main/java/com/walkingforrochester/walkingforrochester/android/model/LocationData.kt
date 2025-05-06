package com.walkingforrochester.walkingforrochester.android.model

import com.google.android.gms.maps.model.LatLng

data class LocationData(
    val latLng: LatLng,
    val isMock: Boolean,
) {
    companion object {
        val DEFAULT = LocationData(
            latLng = LatLng(0.0, 0.0),
            isMock = false,
        )
        val ROCHESTER_NY = LocationData(
            latLng = LatLng(43.1566, -77.6088),
            isMock = false,
        )
    }
}

