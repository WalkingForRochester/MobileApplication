package com.walkingforrochester.walkingforrochester.android.viewmodel

import android.content.Context
import android.location.Location
import android.os.Build
import androidx.annotation.Keep
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.walkingforrochester.walkingforrochester.android.WFRDateFormatter
import com.walkingforrochester.walkingforrochester.android.md5
import com.walkingforrochester.walkingforrochester.android.metersToMiles
import com.walkingforrochester.walkingforrochester.android.model.LocationTrackingEvent
import com.walkingforrochester.walkingforrochester.android.model.LocationTrackingEventType
import com.walkingforrochester.walkingforrochester.android.network.RestApiService
import com.walkingforrochester.walkingforrochester.android.network.request.LogAWalkRequest
import com.walkingforrochester.walkingforrochester.android.repository.PreferenceRepository
import com.walkingforrochester.walkingforrochester.android.ui.state.GuidelinesDialogState
import com.walkingforrochester.walkingforrochester.android.ui.state.LogAWalkEvent
import com.walkingforrochester.walkingforrochester.android.ui.state.LogAWalkState
import com.walkingforrochester.walkingforrochester.android.ui.state.SurveyDialogState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.io.File
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// 20 mi/h = 8.9408 m/s
const val SPEED_LIMIT_METERS_PER_SECOND: Float = 8.9408f

@HiltViewModel
class LogAWalkViewModel @Inject constructor(
    private val restApiService: RestApiService,
    private val preferenceRepository: PreferenceRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogAWalkState())
    val uiState = _uiState.asStateFlow()
    private val _eventFlow = MutableSharedFlow<LogAWalkEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        Timber.d("Initializing LogAWalkViewModel")
        EventBus.getDefault().register(this)
    }

    override fun onCleared() {
        super.onCleared()

        EventBus.getDefault().unregister(this)
        Timber.d("Cleared LogAWalkViewModel")
    }

    fun onToggleWalk(walking: Boolean? = null) = viewModelScope.launch {
        try {
            val shouldStart = walking ?: !_uiState.value.walking
            if (shouldStart) {
                showGuidelinesDialog()
            } else {
                showSurveyDialog()
            }
        } catch (t: Throwable) {
            Timber.e(t)
            _eventFlow.emit(LogAWalkEvent.UnexpectedError)
        }
    }

    private suspend fun startWalk() {
        _uiState.update {
            val now = System.currentTimeMillis()
            LogAWalkState(walking = true, startTimestamp = now, lastSimplificationTimestamp = now)
        }
        _eventFlow.emit(LogAWalkEvent.StartWalking)
    }

    private suspend fun stopWalk() {
        _eventFlow.emit(LogAWalkEvent.StopWalking)
        _uiState.update {
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
        }

        submitWalk()
        _uiState.update { it.copy(loading = false) }
        _eventFlow.emit(LogAWalkEvent.Submitted)
    }

    private suspend fun submitWalk() {
        with(_uiState.value) {
            val file = uploadImage()
            if (file.isNotBlank()) {
                restApiService.logAWalk(
                    LogAWalkRequest(
                        accountId = preferenceRepository.fetchAccountId(),
                        collect = surveyDialogState.bagsCollected,
                        distance = _uiState.value.distanceMiles,
                        duration = duration.toDouble(),
                        imageFileName = file,
                        path = encodedPath
                    )
                )
            }
        }
    }

    private suspend fun uploadImage(): String {
        with(_uiState.value) {
            surveyDialogState.picUri?.let {
                val fileName = "IMG_WALKING_PICKIMAGE_${
                    LocalDate.now().format(WFRDateFormatter.formatter)
                }_${md5(preferenceRepository.fetchAccountId().toString())}"
                if (it.path != null) {
                    context.contentResolver.openInputStream(it)?.use { inputStream ->
                        val body: MultipartBody.Part =
                            MultipartBody.Part.createFormData(
                                "file",
                                fileName,
                                inputStream.readBytes()
                                    .toRequestBody("form-data".toMediaTypeOrNull())
                            )
                        restApiService.uploadImage(body)
                        return fileName
                    }
                }
            }
            _eventFlow.emit(LogAWalkEvent.CameraRationalError)
            return ""
        }
    }

    fun toggleCameraFollow(shouldFollowCamera: Boolean): Boolean {
        _uiState.update { it.copy(followCamera = shouldFollowCamera) }
        return false
    }

    private fun showGuidelinesDialog() =
        _uiState.update { it.copy(guidelinesDialogState = GuidelinesDialogState(showDialog = true)) }

    fun onDismissGuidelines() =
        _uiState.update { it.copy(guidelinesDialogState = GuidelinesDialogState()) }

    fun onGuidelinesLinkClick() =
        _uiState.update { it.copy(guidelinesDialogState = it.guidelinesDialogState.copy(linkClicked = true)) }

    fun onAcceptGuidelines() = viewModelScope.launch {
        _uiState.update { it.copy(guidelinesDialogState = GuidelinesDialogState()) }
        startWalk()
    }

    private fun showSurveyDialog() =
        _uiState.update { it.copy(surveyDialogState = SurveyDialogState(showDialog = true)) }

    fun onDismissSurveyDialog() =
        _uiState.update { it.copy(surveyDialogState = SurveyDialogState()) }

    fun onDiscardWalking() = viewModelScope.launch {
        _uiState.update { LogAWalkState() }
        _eventFlow.emit(LogAWalkEvent.StopWalking)
    }

    fun onSubmitWalking() = viewModelScope.launch {
        try {
            _uiState.update { it.copy(surveyDialogState = it.surveyDialogState.copy(showDialog = false)) }
            stopWalk()
        } catch (t: Throwable) {
            Timber.e(t, "Unable to submit a Walk")
            _eventFlow.emit(LogAWalkEvent.UnexpectedError)
        }
    }

    fun onPickedUpLitterChange(newPickedUpLitter: Boolean) = _uiState.update {
        it.copy(
            surveyDialogState = it.surveyDialogState.copy(
                pickedUpLitter = newPickedUpLitter,
                bagsCollected = 0
            )
        )
    }

    fun onBagsCollectedChange(newBagsCollected: Int) =
        _uiState.update { it.copy(surveyDialogState = it.surveyDialogState.copy(bagsCollected = newBagsCollected)) }

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

    fun onDismissMockLocationDialog() = _uiState.update { LogAWalkState() }

    fun onDismissMovingTooFastDialog() = _uiState.update { LogAWalkState() }

    @Keep
    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onLocationTrackingEvent(event: LocationTrackingEvent) = viewModelScope.launch {
        try {
            when (event.event) {
                LocationTrackingEventType.Location -> trackLocation(event.locations)
                LocationTrackingEventType.Stop -> onToggleWalk(walking = false)
            }
        } catch (t: Throwable) {
            Timber.e(t, "Location event error")
            _eventFlow.emit(LogAWalkEvent.UnexpectedError)
        }
    }

    private suspend fun trackLocation(locations: List<Location>) {
        val lastLocation = locations.last()

        if (!validateLocation(lastLocation)) return

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

        simplifyPath()

        Timber.d("Pushed locations to the path $locations")
    }

    private suspend fun validateLocation(location: Location): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && location.isMock) {
            _uiState.update { LogAWalkState(mockLocation = true) }
            _eventFlow.emit(LogAWalkEvent.MockLocationDetected)

            return false
        }

        if (location.hasSpeed() && location.hasSpeedAccuracy() && location.speed - location.speedAccuracyMetersPerSecond > SPEED_LIMIT_METERS_PER_SECOND) {
            location.speed
            _uiState.update { LogAWalkState(movingTooFast = true) }
            _eventFlow.emit(LogAWalkEvent.MovingTooFast)

            return false
        }

        return true
    }

    private fun simplifyPath(force: Boolean = false) {
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
    }

}