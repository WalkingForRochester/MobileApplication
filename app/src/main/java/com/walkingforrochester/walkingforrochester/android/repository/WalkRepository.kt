package com.walkingforrochester.walkingforrochester.android.repository

import android.location.Location
import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.walkingforrochester.walkingforrochester.android.model.LocationData
import com.walkingforrochester.walkingforrochester.android.model.WalkData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface WalkRepository {

    val currentLocation: StateFlow<LatLng>
    val walkData: StateFlow<WalkData>
    val locationPermissionGranted: Flow<Boolean>

    suspend fun startWalk()
    suspend fun stopWalk()

    fun updateBagsOfLitter(value: Int)
    fun updateImageUri(imageUri: Uri)

    fun clearWalk()

    suspend fun updateCurrentLocation(location: Location)

}