package com.walkingforrochester.walkingforrochester.android.ui.composable.logawalk

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.maps.model.LatLng
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.model.LocationData
import com.walkingforrochester.walkingforrochester.android.model.WalkData
import com.walkingforrochester.walkingforrochester.android.model.WalkData.WalkState
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.LocalSnackbarHostState
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.ShowLocationRational
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.ShowNotificationRational
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.checkOrRequestPermission
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.rememberOnOpenSettings
import com.walkingforrochester.walkingforrochester.android.ui.state.LogAWalkEvent
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import com.walkingforrochester.walkingforrochester.android.viewmodel.LogAWalkViewModel
import timber.log.Timber

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LogAWalkScreen(
    modifier: Modifier = Modifier,
    onNavigateToSubmitWalk: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    logAWalkViewModel: LogAWalkViewModel = hiltViewModel()
) {
    val snackbarHostState = LocalSnackbarHostState.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Timber.d("Collecting events...")
        logAWalkViewModel.eventFlow.collect { event ->
            when (event) {
                LogAWalkEvent.StartWalking -> {
                    //onStartWalking()
                }

                LogAWalkEvent.StopWalking -> {
                    //onNavigateToSubmitWalk()
                }

                LogAWalkEvent.WalkCompleted -> {
                    onNavigateToSubmitWalk()
                }

                LogAWalkEvent.MockLocationDetected -> {
                    //  onStopWalking()
                }

                LogAWalkEvent.MovingTooFast -> {
                    // onStopWalking()
                }

                LogAWalkEvent.Submitted -> snackbarHostState.showSnackbar(
                    message = context.getString(R.string.submission_complete_message)
                )

                LogAWalkEvent.CameraRationalError -> snackbarHostState.showSnackbar(
                    message = context.getString(R.string.camera_permission_rationale)
                )

                LogAWalkEvent.UnexpectedError -> snackbarHostState.showSnackbar(
                    message = context.getString(R.string.unexpected_error)
                )
            }
        }
    }

    var showLocationRational by remember { mutableStateOf(false) }

    val permissionPreferences by logAWalkViewModel.permissionPreferences.collectAsStateWithLifecycle()
    val currentLocation by logAWalkViewModel.currentLocation.collectAsStateWithLifecycle()
    val currentWalk by logAWalkViewModel.currentWalk.collectAsStateWithLifecycle()

    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ),
        onPermissionsResult = {
            val rejectedPermissions = it.filterNot { it.value }

            if (rejectedPermissions.isEmpty()) {
                // all granted, so mark rational not shown
                logAWalkViewModel.onUpdateLocationRationalShown(false)
            } else {
                // Location required for app so put up rational
                showLocationRational = true
            }
        }
    )

    LifecycleStartEffect(context) {
        Timber.d("started...")
        logAWalkViewModel.recoverWalkingState()
        locationPermissionState.checkOrRequestPermission(
            rationalShown = permissionPreferences.locationRationalShown,
            onShowRational = { showLocationRational = true },
            onPermissionsGranted = {
                logAWalkViewModel.onUpdateLocationRationalShown(false)
                showLocationRational = false
            }
        )
        onStopOrDispose {
            when (lifecycle.currentState) {
                Lifecycle.State.CREATED -> Timber.d("stopped...")
                else -> Timber.d("disposed...")
            }
        }
    }

    LogAWalkContent(
        currentLocation = currentLocation,
        currentWalk = currentWalk,
        locationPermissionGranted = locationPermissionState.allPermissionsGranted,
        notificationRationalShown = permissionPreferences.notificationRationalShown,
        modifier = modifier,
        onStartWalk = { logAWalkViewModel.onStartWalk() },
        onStopWalk = { logAWalkViewModel.onStopWalk() },
        onClearWalk = { logAWalkViewModel.onClearWalk() },
        onUpdateNotificationRationalShown = { logAWalkViewModel.onUpdateNotificationRationalShown(it) },
        contentPadding = contentPadding
    )

    // Must be remembered outside of the if show rational block so that the
    // dialog can be closed when settings is launched and the result
    // lambda is properly invoked
    val onOpenSettings = rememberOnOpenSettings { result ->
        Timber.d("requesting location permissions after activity result %s", result)
        locationPermissionState.launchMultiplePermissionRequest()
        showLocationRational = false
    }

    if (showLocationRational) {
        // Because rational shown if OS permission request dismissed, only mark
        // rational shown if is required.
        val rationalRequired =
            permissionPreferences.locationRationalShown || locationPermissionState.permissions.any { it.status.shouldShowRationale }

        ShowLocationRational(
            locationPermissionState = locationPermissionState,
            rationalShown = permissionPreferences.locationRationalShown,
            onRequestPermissions = {
                logAWalkViewModel.onUpdateLocationRationalShown(rationalRequired)
                showLocationRational = false
            },
            onDismissRequest = { showLocationRational = false },
            onOpenSettings = onOpenSettings
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LogAWalkContent(
    currentLocation: LatLng,
    currentWalk: WalkData,
    locationPermissionGranted: Boolean,
    notificationRationalShown: Boolean,
    modifier: Modifier = Modifier,
    onStartWalk: () -> Unit = {},
    onStopWalk: () -> Unit = {},
    onClearWalk: () -> Unit = {},
    onUpdateNotificationRationalShown: (Boolean) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues()
) {
    var showGuidelinesDialog by rememberSaveable { mutableStateOf(false) }
    var showStopWalkDialog by rememberSaveable { mutableStateOf(false) }

    var showNotificationRational by rememberSaveable { mutableStateOf(false) }
    var notificationRationalShownOnce by rememberSaveable { mutableStateOf(false) }

    val notificationPermissionState = rememberNotificationPermission { granted ->
        // Callback from launchMultiplePermissionRequest().
        if (granted) {
            // If permissions granted, reset everything
            onUpdateNotificationRationalShown(false)
            notificationRationalShownOnce = false
        }

        // Regardless of permission grant show walking guidelines.
        // Notification is "optional" in that it just isn't shown but service still runs.
        showGuidelinesDialog = true
    }

    Box(modifier = modifier.fillMaxSize()) {
        LogAWalkMap(
            currentLocation = currentLocation,
            currentWalk = currentWalk,
            showCurrentLocation = locationPermissionGranted,
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize()
        )

        AnimatedVisibility(
            visible = locationPermissionGranted,
            modifier = modifier.align(alignment = Alignment.BottomCenter),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            val isWalking = currentWalk.state == WalkState.IN_PROGRESS ||
                currentWalk.state == WalkState.COMPLETE

            StartStopWalkButton(
                walking = isWalking,
                onClick = {
                    if (isWalking) {
                        showStopWalkDialog = true
                    } else {
                        if (notificationPermissionState.status.isGranted) {
                            Timber.d("JSR notification granted...")
                            onUpdateNotificationRationalShown(false)
                            notificationRationalShownOnce = false
                            showGuidelinesDialog = true
                        } else {
                            showNotificationRational = true
                        }
                    }
                },
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(16.dp)
            )
        }
        if (showGuidelinesDialog) {
            GuidelinesDialog(
                onAcceptGuideLines = {
                    showGuidelinesDialog = false
                    onStartWalk()
                },
                onDismissGuidelines = { showGuidelinesDialog = false }
            )
        }
        if (showStopWalkDialog) {
            StopWalkConfirmationDialog(
                onStopWalk = {
                    showStopWalkDialog = false
                    onStopWalk()
                },
                onDismiss = { showStopWalkDialog = false }
            )
        }

        if (currentWalk.state == WalkState.MOCK_LOCATION_DETECTED) {
            WalkEndedDialog(
                onDismissRequest = { onClearWalk() },
                title = stringResource(R.string.mock_location_detected),
                text = stringResource(R.string.mock_location_dialog)
            )
        }

        if (showNotificationRational) {
            if (notificationRationalShownOnce) {
                // User already saw our rational dialog once
                // Do not show again regardless of permission state and
                // start the walk
                showGuidelinesDialog = true
                showNotificationRational = false
            } else {
                ShowNotificationRational(
                    notificationPermissionState = notificationPermissionState,
                    rationalShown = notificationRationalShown,
                    onRequestPermissions = {
                        Timber.d("JSR show permissions")
                        // If requested to see permissions, mark that
                        // the rational was shown when this is at least the 2nd time
                        // showing the rational
                        onUpdateNotificationRationalShown(notificationRationalShown || notificationPermissionState.status.shouldShowRationale)

                        // Also indicate the rational was shown once
                        // so user isn't asked again
                        notificationRationalShownOnce = true
                        showNotificationRational = false
                    },
                    onDismissRequest = {
                        Timber.d("JSR dismiss rational")
                        notificationRationalShownOnce = true
                        showNotificationRational = false

                        // User skipped, so go ahead and start walk
                        // nice notification is optional
                        showGuidelinesDialog = true
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberNotificationPermission(
    onPermissionResult: (Boolean) -> Unit
): PermissionState {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS,
            onPermissionResult = onPermissionResult
        )
    } else {
        remember {
            object : PermissionState {
                override fun launchPermissionRequest() {}
                override val permission: String = "notification"
                override val status: PermissionStatus = PermissionStatus.Granted
            }
        }
    }
}

@Composable
fun WalkEndedDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.understood))
            }
        },
        modifier = modifier,
        icon = { Icon(imageVector = Icons.Filled.Warning, contentDescription = null) },
        title = { Text(title) },
        text = { Text(text) }
    )
}

@PreviewLightDark
@Composable
fun PreviewWalkEndedDialog() {
    WalkingForRochesterTheme {
        WalkEndedDialog(
            onDismissRequest = {},
            title = stringResource(R.string.mock_location_detected),
            text = stringResource(R.string.mock_location_dialog)
        )
    }
}

@PreviewLightDark
@Composable
fun PreviewWalkScreen() {
    WalkingForRochesterTheme {
        Surface(modifier = Modifier.size(200.dp)) {
            LogAWalkContent(
                currentLocation = LocationData.ROCHESTER_NY.latLng,
                currentWalk = WalkData(state = WalkState.IDLE),
                locationPermissionGranted = true,
                notificationRationalShown = false
            )
        }
    }
}