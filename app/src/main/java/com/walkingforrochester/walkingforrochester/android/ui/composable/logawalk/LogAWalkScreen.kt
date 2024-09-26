package com.walkingforrochester.walkingforrochester.android.ui.composable.logawalk

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.showNotification
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.LoadingOverlay
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.LocalSnackbarHostState
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.RequestLocationPermissionsScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRDialog
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

    val uiState by logAWalkViewModel.uiState.collectAsStateWithLifecycle()


    val permissionState = rememberMultiplePermissionsState(
        permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    )

    LoadingOverlay(uiState.loading)

    if (permissionState.allPermissionsGranted) {
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
                onClick = logAWalkViewModel::onToggleWalk,
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
                WFRDialog(
                    onDismissRequest = { logAWalkViewModel.onDismissMockLocationDialog() },
                    icon = { Icon(imageVector = Icons.Filled.Warning, contentDescription = null) },
                    title = { Text(stringResource(R.string.mock_location_detected)) },
                    buttons = {
                        TextButton(onClick = { logAWalkViewModel.onDismissMockLocationDialog() }) {
                            Text(stringResource(R.string.understood))
                        }
                    }) {
                    Text(stringResource(R.string.mock_location_dialog))
                }
            }
            if (uiState.movingTooFast) {
                WFRDialog(
                    onDismissRequest = { logAWalkViewModel.onDismissMovingTooFastDialog() },
                    icon = { Icon(imageVector = Icons.Filled.Warning, contentDescription = null) },
                    title = { Text(stringResource(R.string.moving_too_fast)) },
                    buttons = {
                        TextButton(onClick = { logAWalkViewModel.onDismissMovingTooFastDialog() }) {
                            Text(stringResource(R.string.understood))
                        }
                    }) {
                    Text(stringResource(R.string.moving_too_fast_dialog))
                }
            }
        }
    } else {
        RequestLocationPermissionsScreen(permissionState = permissionState)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWalkScreen() {
    WalkingForRochesterTheme {
        LogAWalkScreen(onStartWalking = {}, onStopWalking = {})
    }
}