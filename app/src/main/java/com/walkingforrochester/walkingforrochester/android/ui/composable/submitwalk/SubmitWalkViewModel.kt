package com.walkingforrochester.walkingforrochester.android.ui.composable.submitwalk

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.maps.android.PolyUtil
import com.walkingforrochester.walkingforrochester.android.di.DefaultDispatcher
import com.walkingforrochester.walkingforrochester.android.metersToMiles
import com.walkingforrochester.walkingforrochester.android.model.WalkData
import com.walkingforrochester.walkingforrochester.android.repository.NetworkRepository
import com.walkingforrochester.walkingforrochester.android.repository.PreferenceRepository
import com.walkingforrochester.walkingforrochester.android.repository.WalkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SubmitWalkViewModel @Inject constructor(
    private val walkRepository: WalkRepository,
    private val preferenceRepository: PreferenceRepository,
    private val networkRepository: NetworkRepository,
    @DefaultDispatcher val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _walkEvent = MutableSharedFlow<SubmitWalkEvent>()
    val walkEvent = _walkEvent.asSharedFlow()

    val currentWalk = walkRepository.walkData

    val cameraRationalShown = preferenceRepository.permissionPreferences
        .map {
            it.cameraRationalShown
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        Timber.e(throwable, "Unexpected error submitting a walk")

        if (!_walkEvent.tryEmit(SubmitWalkEvent.UnexpectedError)) {
            Timber.w("Failed to report error due to no listener")
        }
    }

    fun checkWalk() = viewModelScope.launch {
        val state = walkRepository.walkData.value.state
        if (state != WalkData.WalkState.COMPLETE) {
            walkRepository.clearWalk()
            _walkEvent.emit(SubmitWalkEvent.WalkDiscarded)
            Timber.d("Check walk, walk discarded event submitted")
        }
    }

    fun submitWalk() = viewModelScope.launch(exceptionHandler) {
        _walkEvent.emit(SubmitWalkEvent.SubmissionInProgress)
        submitWalkToServer()
    }

    fun discardWalk() = viewModelScope.launch {
        walkRepository.clearWalk()
        _walkEvent.emit(SubmitWalkEvent.WalkDiscarded)
    }

    fun updateBagsOfLitter(newValue: Int) {
        walkRepository.updateBagsOfLitter(newValue)
    }

    fun updateCameraRationalShown(
        shown: Boolean
    ) = viewModelScope.launch(context = exceptionHandler) {
        preferenceRepository.updateCameraRationalShown(shown)
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
                walkRepository.clearWalk()
                _walkEvent.emit(SubmitWalkEvent.WalkSubmitted)
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
            _walkEvent.emit(SubmitWalkEvent.UnexpectedError)
        }
        return fileName
    }
}