package com.walkingforrochester.walkingforrochester.android.repository.internal

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.walkingforrochester.walkingforrochester.android.di.DefaultDispatcher
import com.walkingforrochester.walkingforrochester.android.ktx.toFixedBounds
import com.walkingforrochester.walkingforrochester.android.ktx.toLocationData
import com.walkingforrochester.walkingforrochester.android.model.LocationData
import com.walkingforrochester.walkingforrochester.android.model.WalkData
import com.walkingforrochester.walkingforrochester.android.model.WalkData.WalkState
import com.walkingforrochester.walkingforrochester.android.repository.WalkRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class WalkRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : WalkRepository {

    private var lastLocation: Location? = null

    private val _currentLocation = MutableStateFlow(LocationData.ROCHESTER_NY.latLng)
    private val _walkData = MutableStateFlow(WalkData())

    override val currentLocation = _currentLocation.asStateFlow()
    override val walkData = _walkData.asStateFlow()
    override val locationPermissionGranted: Flow<Boolean> = flow {

        while (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            delay(100)
        }

        Timber.d("location permission granted")
        emit(true)
    }

    override suspend fun startWalk() {
        val currentPosition = _currentLocation.value
        Timber.d("start walk")
        _walkData.update {
            WalkData(
                state = WalkState.IN_PROGRESS,
                startPosition = currentPosition,
                path = buildPath(emptyList(), currentPosition),
                bounds = currentPosition.toFixedBounds(100.0)
            )
        }
    }

    override suspend fun stopWalk() = withContext(defaultDispatcher) {

        Timber.d("stop requested: %s", _walkData.value.state)
        if (_walkData.value.state == WalkState.IN_PROGRESS) {
            Timber.d("stopping walk")
            _walkData.update {
                val adjustedPath = simplifyPath(it.path)

                it.copy(
                    state = WalkState.COMPLETE,
                    endPosition = adjustedPath.last(),
                    durationMilli = System.currentTimeMillis() - it.startTime,
                    distanceMeters = SphericalUtil.computeLength(adjustedPath),
                    path = adjustedPath,
                    bounds = buildBounds(adjustedPath)
                )
            }
        }
    }

    override fun updateBagsOfLitter(value: Int) {
        _walkData.update { it.copy(bagsOfLitter = value) }
    }

    override fun updateImageUri(imageUri: Uri) {
        _walkData.update { it.copy(imageUri = imageUri) }
    }

    override fun clearWalk() {
        Timber.d("clear walk")
        _walkData.update { WalkData() }
    }

    override suspend fun updateCurrentLocation(
        location: Location
    ) = withContext(defaultDispatcher) {
        Timber.d("have new location %s", location)
        if (isBetterLocation(location)) {
            Timber.d("have better location: %s", location)
            lastLocation = location
            val locationData = location.toLocationData()
            _currentLocation.update { locationData.latLng }

            if (_walkData.value.state == WalkState.IN_PROGRESS) {
                updateWalk(locationData)
            }
        }
    }

    private fun updateWalk(locationData: LocationData) {
        Timber.d("Updating walk: %s", locationData)
        _walkData.update {
            val newPath = buildPath(it.path, locationData.latLng)
            val endedDueToMockLocation =
                it.state == WalkState.MOCK_LOCATION_DETECTED || locationData.isMock
            val endedDueToMovingToFast =
                it.state == WalkState.SPEEDING_DETECTED || locationData.adjustedSpeed > SPEED_LIMIT_METERS_PER_SECOND

            Timber.d("speed: %f", locationData.adjustedSpeed)
            it.copy(
                path = newPath,
                bounds = it.bounds.including(locationData.latLng),
                state = when {
                    endedDueToMockLocation -> WalkState.MOCK_LOCATION_DETECTED
                    endedDueToMovingToFast -> WalkState.SPEEDING_DETECTED
                    else -> it.state
                }
            )
        }

    }

    private fun buildBounds(path: List<LatLng>): LatLngBounds {
        val startBounds = path.first().toFixedBounds(50.0)
        val endBounds = path.last().toFixedBounds(50.0)

        return LatLngBounds.Builder().apply {
            include(startBounds.northeast)
            include(startBounds.southwest)

            for (point in path) {
                include(point)
            }

            include(endBounds.northeast)
            include(endBounds.southwest)
        }.build()
    }

    private fun buildPath(oldPath: List<LatLng>, endPosition: LatLng): List<LatLng> {
        val newPath = mutableListOf<LatLng>()

        when {
            oldPath.size < 2 -> newPath.add(endPosition)
            oldPath.size == 2 && oldPath.first() == oldPath.last() -> newPath.add(oldPath.first())
            else -> newPath.addAll(oldPath)
        }

        newPath.add(endPosition)

        return newPath.toList()
    }

    private fun simplifyPath(path: List<LatLng>): List<LatLng> {
        if (path.size < 2) return path

        val polyList = mutableListOf<LatLng>()
        polyList.addAll(path)
        polyList.add(path.first())

        val simplifiedPoly = PolyUtil.simplify(polyList, 1.0).apply {
            removeAt(size - 1)
        }

        return simplifiedPoly.toList()
    }

    private fun isBetterLocation(location: Location): Boolean {
        val lastLocation = lastLocation ?: return true

        // Check whether the new location fix is newer or older
        val timeDelta = location.time - lastLocation.time
        val isSignificantlyNewer = timeDelta >= TWO_MINUTES
        val isSignificantlyOlder = timeDelta <= -TWO_MINUTES
        val isRefreshTime: Boolean = timeDelta >= FIFTEEN_SECONDS
        val accuracyDelta = location.accuracy - lastLocation.accuracy

        // Determine location quality using a combination of timeliness and accuracy
        return when {
            // If been two min since last update, then accept the new location
            isSignificantlyNewer -> true
            isSignificantlyOlder -> {
                Timber.d("significantly older... if emulator reboot")
                return false
            }
            // Newer and more accurate
            timeDelta > 0 && accuracyDelta < 0f -> true
            // Newer and the user moved
            timeDelta > 0 && location.distanceTo(lastLocation) > MOVED_DISTANCE -> true
            // Same or better accuracy and time to refresh
            isRefreshTime && accuracyDelta <= 0f -> true
            // Time to refresh and not significantly less accurate from same provider
            isRefreshTime && accuracyDelta <= MINIMUM_ACCURACY_DELTA &&
                location.provider == lastLocation.provider -> true

            else -> false
        }
    }

    companion object {
        const val FIFTEEN_SECONDS = 15 * 1000
        const val TWO_MINUTES = 2 * 60 * 1000
        private const val MINIMUM_ACCURACY_DELTA = 200f
        private const val MOVED_DISTANCE = 1f

        // 20 mi/h = 8.9408 m/s
        private const val SPEED_LIMIT_METERS_PER_SECOND: Float = 8.9408f
    }
}