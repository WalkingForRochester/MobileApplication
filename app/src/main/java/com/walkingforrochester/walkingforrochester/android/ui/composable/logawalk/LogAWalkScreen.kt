package com.walkingforrochester.walkingforrochester.android.ui.composable.logawalk

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.showNotification
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.LoadingOverlay
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.LocalSnackbarHostState
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.RequestLocationPermissionsScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.RequestNotificationPermissionsScreen
import com.walkingforrochester.walkingforrochester.android.ui.state.LogAWalkEvent
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import com.walkingforrochester.walkingforrochester.android.viewmodel.LogAWalkViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LogAWalkScreen(
    modifier: Modifier = Modifier,
    onStartWalking: () -> Unit = {},
    onStopWalking: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    logAWalkViewModel: LogAWalkViewModel = hiltViewModel()
) {
    val snackbarHostState = LocalSnackbarHostState.current
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        logAWalkViewModel.eventFlow.collect { event ->
            when (event) {
                LogAWalkEvent.StartWalking -> onStartWalking()
                LogAWalkEvent.StopWalking -> onStopWalking()
                LogAWalkEvent.MockLocationDetected -> {
                    onStopWalking()
                    showNotification(context = context, text = "Spoof Location detected")
                }

                LogAWalkEvent.MovingTooFast -> {
                    onStopWalking()
                    showNotification(context = context, text = "You're moving too fast")
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

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
            logAWalkViewModel.recoverWalkingState()
        }
    }

    var requestNotificationPermission by remember { mutableStateOf(false) }
    var notificationPermissionShown by remember { mutableStateOf(false) }

    val uiState by logAWalkViewModel.uiState.collectAsStateWithLifecycle()


    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    val notificationPermissionState = rememberMultiplePermissionsState(
        permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            emptyList()
        },
        onPermissionsResult = {
            // Callback from launchMultiplePermissionRequest(). Regardless of permission
            // grant, prevent dialog from showing again this run and start the walk.
            // Notification is "optional" in that it just isn't shown but service still runs.
            notificationPermissionShown = true
            requestNotificationPermission = false
            logAWalkViewModel.onToggleWalk()
        }
    )

    LoadingOverlay(uiState.loading)

    if (locationPermissionState.allPermissionsGranted) {
        // If all permissions granted, rational is reset
        logAWalkViewModel.onUpdateLocationRationalShown(false)
        Box(modifier = modifier.fillMaxSize()) {
            LogAWalkMap(
                toggleCameraFollow = logAWalkViewModel::toggleCameraFollow,
                followCamera = uiState.followCamera,
                lastLocation = uiState.lastLocation,
                selectedAddressLocation = uiState.selectedAddressLocation,
                path = uiState.path,
                startingPoint = uiState.startingPoint,
                finishingPoint = uiState.finishingPoint,
                contentPadding = contentPadding
            )
            StartStopWalkButton(
                walking = uiState.walking,
                onClick = {
                    if (uiState.walking) {
                        logAWalkViewModel.onToggleWalk()
                    } else {
                        if (notificationPermissionState.permissions.isEmpty() ||
                            notificationPermissionState.allPermissionsGranted
                        ) {
                            logAWalkViewModel.onUpdateNotificationRationalShown(false)
                            logAWalkViewModel.onToggleWalk()
                        } else {
                            requestNotificationPermission = true
                        }
                    }
                },
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(16.dp)
                    .align(alignment = Alignment.BottomCenter)
            )
            if (uiState.guidelinesDialogState.showDialog) {
                GuidelinesDialog(
                    onLinkClick = { logAWalkViewModel.onGuidelinesLinkClick() },
                    onAcceptGuideLines = { logAWalkViewModel.onAcceptGuidelines() },
                    onDismissGuidelines = { logAWalkViewModel.onDismissGuidelines() }
                )
            }
            if (uiState.surveyDialogState.showDialog) {
                WalkSurveyDialog(
                    logAWalkViewModel = logAWalkViewModel,
                    surveyDialogState = uiState.surveyDialogState,
                )
            }
            if (uiState.mockLocation) {
                WalkEndedDialog(
                    onDismissRequest = { logAWalkViewModel.onDismissMockLocationDialog() },
                    title = stringResource(R.string.mock_location_detected),
                    text = stringResource(R.string.mock_location_dialog)
                )
            }
            if (uiState.movingTooFast) {
                WalkEndedDialog(
                    onDismissRequest = { logAWalkViewModel.onDismissMovingTooFastDialog() },
                    title = stringResource(R.string.moving_too_fast),
                    text = stringResource(R.string.moving_too_fast_dialog)
                )
            }
            if (requestNotificationPermission) {
                if (notificationPermissionShown) {
                    logAWalkViewModel.onToggleWalk()
                    requestNotificationPermission = false
                } else {
                    RequestNotificationPermissionsScreen(
                        permissionState = notificationPermissionState,
                        rationalShown = uiState.notificationRationalShown,
                        onUpdateRationalShown = {
                            logAWalkViewModel.onUpdateNotificationRationalShown(it)
                        },
                        onDismissRequest = {
                            logAWalkViewModel.onToggleWalk()
                            requestNotificationPermission = false
                            notificationPermissionShown = true
                        }
                    )
                }
            }
        }
    } else {
        RequestLocationPermissionsScreen(
            permissionState = locationPermissionState,
            rationalShown = uiState.locationRationalShown,
            onUpdateRationalShown = { it ->
                logAWalkViewModel.onUpdateLocationRationalShown(it)
            }
        )
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

@Preview
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

@Preview(showBackground = true)
@Composable
fun PreviewWalkScreen() {
    WalkingForRochesterTheme {
        LogAWalkScreen(onStartWalking = {}, onStopWalking = {})
    }
}