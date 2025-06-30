package com.walkingforrochester.walkingforrochester.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walkingforrochester.walkingforrochester.android.di.DefaultDispatcher
import com.walkingforrochester.walkingforrochester.android.model.PermissionPreferences
import com.walkingforrochester.walkingforrochester.android.model.WalkData.WalkState
import com.walkingforrochester.walkingforrochester.android.repository.PreferenceRepository
import com.walkingforrochester.walkingforrochester.android.repository.WalkRepository
import com.walkingforrochester.walkingforrochester.android.ui.composable.logawalk.LogAWalkEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LogAWalkViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    private val walkRepository: WalkRepository,
    @param:DefaultDispatcher val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<LogAWalkEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val permissionPreferences = preferenceRepository.permissionPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PermissionPreferences()
        )


    val currentLocation = walkRepository.currentLocation
    val currentWalk = walkRepository.walkData

    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        Timber.e(throwable, "Unexpected error submitting a walk")

        if (!_eventFlow.tryEmit(LogAWalkEvent.UnexpectedError)) {
            Timber.w("Failed to report error due to no listener")
        }
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

    fun onStartWalk() = viewModelScope.launch(exceptionHandler) {
        walkRepository.startWalk()
    }

    fun onStopWalk() = viewModelScope.launch(exceptionHandler) {
        walkRepository.stopWalk()
        _eventFlow.emit(LogAWalkEvent.WalkCompleted)
    }

    fun onClearWalk() {
        walkRepository.clearWalk()
    }
}