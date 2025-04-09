package com.walkingforrochester.walkingforrochester.android.viewmodel

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.maps.android.PolyUtil
import com.walkingforrochester.walkingforrochester.android.di.DefaultDispatcher
import com.walkingforrochester.walkingforrochester.android.metersToMiles
import com.walkingforrochester.walkingforrochester.android.model.PermissionPreferences
import com.walkingforrochester.walkingforrochester.android.model.WalkData.WalkState
import com.walkingforrochester.walkingforrochester.android.repository.NetworkRepository
import com.walkingforrochester.walkingforrochester.android.repository.PreferenceRepository
import com.walkingforrochester.walkingforrochester.android.repository.WalkRepository
import com.walkingforrochester.walkingforrochester.android.ui.state.LogAWalkEvent
import com.walkingforrochester.walkingforrochester.android.ui.state.LogAWalkState
import com.walkingforrochester.walkingforrochester.android.ui.state.SurveyDialogState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class LogAWalkViewModel @Inject constructor(
    private val networkRepository: NetworkRepository,
    private val preferenceRepository: PreferenceRepository,
    private val walkRepository: WalkRepository,
    @DefaultDispatcher val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogAWalkState())
    val uiState = _uiState.asStateFlow()
    private val _eventFlow = MutableSharedFlow<LogAWalkEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val permissionPreferences = preferenceRepository.permissionPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PermissionPreferences()
        )


    val currentLocation = walkRepository.currentLocation
    /*val currentLocation = walkRepository.currentLocation.map { location->
        validateLocation(location)
        Timber.d("JSR latlng %s", location.latLng)
        location.latLng
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = walkRepository.currentLocation.value.latLng
    )*/

    val currentWalk = walkRepository.walkData

    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        Timber.e(throwable, "Unexpected error submitting a walk")

        if (!_eventFlow.tryEmit(LogAWalkEvent.UnexpectedError)) {
            Timber.w("Failed to report error due to no listener")
        }

        _uiState.update { it.copy(loading = false) }
    }

    fun recoverWalkingState() = viewModelScope.launch(context = exceptionHandler) {
        val walkData = walkRepository.walkData.value
        if (walkData.state == WalkState.COMPLETE) {
            _eventFlow.emit(LogAWalkEvent.WalkCompleted)
        }
    }

    fun onUpdateLocationRationalShown(
        shown: Boolean
    ) = viewModelScope.launch(context = exceptionHandler) {
        preferenceRepository.updateLocationRationalShown(shown)
    }

    fun onUpdateNotificationRationalShown(
        shown: Boolean
    ) = viewModelScope.launch(context = exceptionHandler) {
        preferenceRepository.updateNotificationRationalShown(shown)
    }

    fun onUpdateCameraRationalShown(
        shown: Boolean
    ) = viewModelScope.launch(context = exceptionHandler) {
        preferenceRepository.updateCameraRationalShown(shown)
    }

    /*fun onToggleWalk() = viewModelScope.launch(context = exceptionHandler) {
        if (walkRepository.walkData.value.state == WalkState.IN_PROGRESS) {
            showSurveyDialog()
        } else {
            showGuidelinesDialog()
        }
    }*/

    fun onStartWalk() = viewModelScope.launch(exceptionHandler) {
        // _eventFlow.emit(LogAWalkEvent.StartWalking)
        walkRepository.startWalk()
    }

    fun onStopWalk() = viewModelScope.launch(exceptionHandler) {
        // _eventFlow.emit(LogAWalkEvent.StopWalking)
        walkRepository.stopWalk()
        _eventFlow.emit(LogAWalkEvent.WalkCompleted)
        /*_uiState.update {
            it.copy(
                loading = true,
                walking = false,
                duration = System.currentTimeMillis() - it.startTimestamp,
                finishingPoint = it.path.last()
            )
        }

        simplifyPath(true)
        _uiState.update {
            it.copy(
                distanceMiles = metersToMiles(SphericalUtil.computeLength(it.path)),
                encodedPath = PolyUtil.encode(it.path)
            )
        }*/
    }

    fun submitWalk() = viewModelScope.launch(exceptionHandler) {
        _uiState.update { it.copy(loading = true) }
        //submitWalkToServer()
        walkRepository.clearWalk()
        _uiState.update { it.copy(loading = false) }
        _eventFlow.emit(LogAWalkEvent.Submitted)
    }

    private suspend fun submitWalkToServer() = withContext(defaultDispatcher) {
        with(walkRepository.walkData.value) {
            val file = uploadImage(imageUri)
            if (file.isNotBlank()) {
                networkRepository.submitWalk(
                    accountId = preferenceRepository.fetchAccountId(),
                    bagsCollected = bagsOfLitter,
                    distanceInMiles = distanceMeters.metersToMiles(),
                    duration = durationMilli,
                    imageFileName = file,
                    encodedPolyline = PolyUtil.encode(path)
                )
            }
        }
    }

    private suspend fun uploadImage(uri: Uri?): String {
        val fileName = when {
            uri == null -> ""
            else -> {
                networkRepository.uploadWalkImage(
                    accountId = preferenceRepository.fetchAccountId(),
                    imageUri = uri
                )
            }
        }

        if (fileName.isBlank()) {
            _eventFlow.emit(LogAWalkEvent.CameraRationalError)
        }

        return fileName
    }

    private fun showGuidelinesDialog() = {
        //_uiState.update { it.copy(guidelinesDialogState = GuidelinesDialogState(showDialog = true)) }
    }

    /*fun onDismissGuidelines() = {
        _uiState.update { it.copy(guidelinesDialogState = GuidelinesDialogState()) }

    fun onGuidelinesLinkClick() =
        _uiState.update { it.copy(guidelinesDialogState = it.guidelinesDialogState.copy()) }
*/
    fun onAcceptGuidelines() = viewModelScope.launch(context = exceptionHandler) {
        //_uiState.update { it.copy(guidelinesDialogState = GuidelinesDialogState()) }
        //startWalk()
    }

    private fun showSurveyDialog() = viewModelScope.launch(context = exceptionHandler) {
        _uiState.update {
            it.copy(
                surveyDialogState = SurveyDialogState(
                    showDialog = true,
                    cameraRationalShown = false//preferenceRepository.cameraRationalShown()
                )
            )
        }
    }

    fun onDismissSurveyDialog() =
        _uiState.update { it.copy(surveyDialogState = SurveyDialogState()) }

    fun onDiscardWalking() = viewModelScope.launch(context = exceptionHandler) {
        walkRepository.clearWalk()
        //_uiState.update { LogAWalkState() }
        _uiState.update {
            it.copy(
                //guidelinesDialogState = GuidelinesDialogState(),
                surveyDialogState = SurveyDialogState()
            )
        }
        _eventFlow.emit(LogAWalkEvent.StopWalking)
    }

    fun onSubmitWalking() = viewModelScope.launch(context = exceptionHandler) {
        _uiState.update { it.copy(surveyDialogState = it.surveyDialogState.copy(showDialog = false)) }
        submitWalk()
    }

    @Deprecated("")
    fun onPickedUpLitterChange(newPickedUpLitter: Boolean) = _uiState.update {
        it.copy(
            surveyDialogState = it.surveyDialogState.copy(
                pickedUpLitter = newPickedUpLitter,
                bagsCollected = 0
            )
        )
    }

    fun onBagsCollectedChange(newBagsCollected: Int) {
        walkRepository.updateBagsOfLitter(newBagsCollected)
    }

    fun onShowCamera() = _uiState.update {
        it.copy(
            surveyDialogState = it.surveyDialogState.copy(
                showCamera = true,
                picUri = null
            )
        )
    }

    fun onHideCamera() =
        _uiState.update { it.copy(surveyDialogState = it.surveyDialogState.copy(showCamera = false)) }

    fun onCapturePhoto(pic: File) =
        _uiState.update { it.copy(surveyDialogState = it.surveyDialogState.copy(picUri = pic.toUri())) }

    fun onConfirmPhoto() =
        _uiState.update { it.copy(surveyDialogState = it.surveyDialogState.copy(showCamera = false)) }

    fun onDiscardPhoto() =
        _uiState.update { it.copy(surveyDialogState = it.surveyDialogState.copy(picUri = null)) }

    fun onClearWalk() {
        _uiState.update { LogAWalkState() }
        walkRepository.clearWalk()
    }

    /*@Keep
    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onLocationTrackingEvent(
        event: LocationTrackingEvent
    ) = viewModelScope.launch(context = exceptionHandler) {
        when (event.event) {
            LocationTrackingEventType.Location -> trackLocation(event.locations)
            LocationTrackingEventType.Stop -> showSurveyDialog()
        }
    }*/

    /*private suspend fun trackLocation(locations: List<Location>) {
        val lastLocation = locations.last()

        // if (!validateLocation(lastLocation)) return

        if (_uiState.value.path.isEmpty()) {
            _uiState.update { state ->
                state.copy(
                    startingPoint = locations.first().let { LatLng(it.latitude, it.longitude) })
            }
        }

        _uiState.update {
            it.copy(
                lastLocation = LatLng(
                    lastLocation.latitude,
                    lastLocation.longitude
                ), path = it.path + locations.map { l -> LatLng(l.latitude, l.longitude) })
        }

        //simplifyPath()

        Timber.d("Pushed locations to the path $locations")
    }*/

    /*private suspend fun validateLocation(locationData: LocationData) {
        if (walkRepository.walkData.value.active) {
            if (locationData.isMock) {
                _uiState.update { it.copy(mockLocation = true) }
                _eventFlow.emit(LogAWalkEvent.MockLocationDetected)
            }

            if (locationData.adjustedSpeed > SPEED_LIMIT_METERS_PER_SECOND) {
                _uiState.update { it.copy(movingTooFast = true) }
                _eventFlow.emit(LogAWalkEvent.MovingTooFast)
            }
        }
    }*/

    /*private suspend fun validateLocation(location: Location): Boolean {
        if (LocationCompat.isMock(location)) {
            _uiState.update { it.copy(mockLocation = true) }
            _eventFlow.emit(LogAWalkEvent.MockLocationDetected)

            return false
        }

        if (location.hasSpeed() &&
            location.hasSpeedAccuracy() &&
            location.speed - location.speedAccuracyMetersPerSecond > SPEED_LIMIT_METERS_PER_SECOND
        ) {
            Timber.w("Computed speed is %f", location.speed - location.speedAccuracyMetersPerSecond)
            _uiState.update { it.copy(movingTooFast = true) }
            _eventFlow.emit(LogAWalkEvent.MovingTooFast)

            return false
        }

        return true
    }*/

    /*private fun simplifyPath(force: Boolean = false) {
        if (force || TimeUnit.MINUTES.toMillis(10) < System.currentTimeMillis() - _uiState.value.lastSimplificationTimestamp) {
            _uiState.update {
                it.copy(
                    path = PolyUtil.simplify(
                        _uiState.value.path + _uiState.value.path.first(),
                        1.0
                    ).apply {
                        // remove last point added to make a polygon after simplification
                        removeAt(size - 1)
                    },

                    lastSimplificationTimestamp = System.currentTimeMillis()
                )
            }
        }
    }*/

}