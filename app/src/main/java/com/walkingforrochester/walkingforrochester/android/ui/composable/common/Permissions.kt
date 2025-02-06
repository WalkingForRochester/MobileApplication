package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import com.walkingforrochester.walkingforrochester.android.BuildConfig
import com.walkingforrochester.walkingforrochester.android.R
import timber.log.Timber

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestLocationPermissionsScreen(
    permissionState: MultiplePermissionsState,
    rationalShown: Boolean,
    onUpdateRationalShown: (Boolean) -> Unit = {}
) {
    val activity = LocalActivity.current

    RequestPermissions(
        permissionState = permissionState,
        rationalShown = rationalShown,
        rationalTitle = stringResource(R.string.location_title),
        rationalText = stringResource(R.string.location_permission_rationale),
        onUpdateRationalShown = onUpdateRationalShown,
        onDismissRequest = {
            if (activity is ComponentActivity) {
                Timber.d("JSR calling onBackPressed")
                activity.onBackPressedDispatcher.onBackPressed()
            } else {
                Timber.d("JSR moving to back")
                activity?.moveTaskToBack(false)
            }
        }
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestCameraPermissionsScreen(
    permissionState: MultiplePermissionsState,
    rationalShown: Boolean,
    onUpdateRationalShown: (Boolean) -> Unit = {},
    onDismissRequest: () -> Unit = {}
) {
    RequestPermissions(
        permissionState = permissionState,
        rationalShown = rationalShown,
        rationalTitle = stringResource(R.string.camera_title),
        rationalText = stringResource(R.string.camera_permission_rationale),
        onUpdateRationalShown = onUpdateRationalShown,
        onDismissRequest = onDismissRequest
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun RequestPermissions(
    permissionState: MultiplePermissionsState,
    rationalShown: Boolean,
    rationalTitle: String,
    rationalText: String,
    onUpdateRationalShown: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        Timber.d("JSR result = %d", it.resultCode)
        permissionState.launchMultiplePermissionRequest()
    }

    when {
        // Do nothing is invoked but all permissions granted
        permissionState.allPermissionsGranted -> {}

        rationalShown || permissionState.permissions.any { it.status.shouldShowRationale } -> {
            Timber.d("showing rationale")

            permissionState.permissions.forEach {
                Timber.d("JSR %s %s", it.permission, it.status)
            }
            DisplayRationalDialog(
                permissionState = permissionState,
                title = rationalTitle,
                rational = rationalText,
                settingsLauncher = launcher,
                rationalShown = rationalShown,
                onUpdateRationalShown = onUpdateRationalShown,
                modifier = modifier,
                onDismissRequest = onDismissRequest
            )
        }

        else -> {
            permissionState.permissions.forEach {
                Timber.d("JSR %s %s", it.permission, it.status)
            }
            LaunchedEffect(Unit) {
                Timber.d("requesting permissions")
                permissionState.launchMultiplePermissionRequest()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun DisplayRationalDialog(
    permissionState: MultiplePermissionsState,
    title: String,
    rational: String,
    settingsLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    rationalShown: Boolean,
    onUpdateRationalShown: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            val resId = if (rationalShown) R.string.open_app_settings else R.string.continue_button
            TextButton(onClick = {

                if (rationalShown) {
                    openSettings(settingsLauncher)
                } else {
                    permissionState.launchMultiplePermissionRequest()
                    // Indicate rational was shown
                    onUpdateRationalShown(true)
                }
            }) {
                Text(text = stringResource(resId))
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        title = { Text(text = title) },
        text = {
            Column {
                Text(text = rational)
                if (rationalShown) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.please_open_settings_to_grant_permissions))
                }
            }
        }
    )
}

private fun openSettings(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
    val intent = Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
    }

    try {
        Timber.d("JSR starting settings intent")
        launcher.launch(intent)
    } catch (e: ActivityNotFoundException) {
        Timber.w("Failed to open settings: %s", e.message)
    }
}