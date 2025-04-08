package com.walkingforrochester.walkingforrochester.android.service

import android.app.Application
import android.location.Location
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.walkingforrochester.walkingforrochester.android.repository.WalkRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.concurrent.TimeUnit

class LocationLifecycleObserver(
    private val application: Application,
    private val walkRepository: WalkRepository,
    private val defaultDispatcher: CoroutineDispatcher
) : DefaultLifecycleObserver {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)

        owner.lifecycleScope.launch {

            // Wait for location permission
            walkRepository.locationPermissionGranted.first()

            owner.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
                monitorLocationFlow().collect {
                    walkRepository.updateCurrentLocation(it)
                }
            }
        }
    }

    private fun monitorLocationFlow(): Flow<Location> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    trySendBlocking(location)
                        .onFailure {
                            Timber.w("Failed to send location")
                        }
                }
            }
        }

        try {
            // Get last location...
            fusedLocationProviderClient.lastLocation.await()?.let {
                trySendBlocking(it)
            }

            fusedLocationProviderClient.requestLocationUpdates(
                buildLocationRequest(),
                defaultDispatcher.asExecutor(),
                callback
            )
            Timber.d("requesting location updates")
        } catch (e: SecurityException) {
            Timber.w("Unexpected security exception: %s", e.message)
            close()
        }

        awaitClose { fusedLocationProviderClient.removeLocationUpdates(callback) }
    }

    private fun buildLocationRequest(): LocationRequest {
        return LocationRequest.Builder(TimeUnit.SECONDS.toMillis(5))
            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(3))
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateDistanceMeters(1f)
            .build()
    }
}