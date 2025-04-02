package com.walkingforrochester.walkingforrochester.android.ktx

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil

/**
 * Create a lat long bounds box using the specified distance from center to corner.
 */
fun LatLng.toFixedBounds(distanceFromCenterToCorner: Double): LatLngBounds {
    val southwestCorner = SphericalUtil.computeOffset(this, distanceFromCenterToCorner, 225.0)
    val northeastCorner = SphericalUtil.computeOffset(this, distanceFromCenterToCorner, 45.0)
    return LatLngBounds(southwestCorner, northeastCorner)
}
