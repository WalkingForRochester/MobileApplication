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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.walkingforrochester.walkingforrochester.android.BuildConfig
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import timber.log.Timber

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ShowLocationRational(
    locationPermissionState: MultiplePermissionsState,
    rationalShown: Boolean,
    modifier: Modifier = Modifier,
    onRequestPermissions: () -> Unit = {},
    onDismissRequest: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val activity = LocalActivity.current

    DisplayRationalDialog(
        title = stringResource(R.string.location_title),
        rational = stringResource(R.string.location_permission_rationale),
        rationalShown = rationalShown,
        launchPermissionRequest = { locationPermissionState.launchMultiplePermissionRequest() },
        onRequestPermissions = onRequestPermissions,
        onOpenSettings = onOpenSettings,
        onDismissRequest = {
            if (activity is ComponentActivity) {
                activity.onBackPressedDispatcher.onBackPressed()
            } else {
                activity?.moveTaskToBack(false)
            }
            onDismissRequest()
        },
        dismissButtonLabel = stringResource(R.string.close_app_label),
        modifier = modifier
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ShowNotificationRational(
    notificationPermissionState: PermissionState,
    rationalShown: Boolean,
    modifier: Modifier = Modifier,
    onRequestPermissions: () -> Unit = {},
    onDismissRequest: () -> Unit = {}
) {
    // If rational shown, proceed as any notifications are not shown to the user,
    // but doesn't impact the ability to launch the foreground service.
    if (rationalShown) {
        onDismissRequest()
    } else {
        DisplayRationalDialog(
            title = stringResource(R.string.notification_permission_title),
            rational = stringResource(R.string.notification_permission_rationale),
            rationalShown = false,
            launchPermissionRequest = { notificationPermissionState.launchPermissionRequest() },
            onOpenSettings = onDismissRequest,
            onDismissRequest = onDismissRequest,
            dismissButtonLabel = stringResource(R.string.skip_label),
            onRequestPermissions = onRequestPermissions,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ShowCameraRational(
    cameraPermissionState: PermissionState,
    rationalShown: Boolean,
    modifier: Modifier = Modifier,
    onRequestPermissions: () -> Unit = {},
    onDismissRequest: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    DisplayRationalDialog(
        title = stringResource(R.string.camera_title),
        rational = stringResource(R.string.camera_permission_rationale),
        rationalShown = rationalShown,
        launchPermissionRequest = { cameraPermissionState.launchPermissionRequest() },
        onRequestPermissions = onRequestPermissions,
        onOpenSettings = onOpenSettings,
        onDismissRequest = onDismissRequest,
        dismissButtonLabel = stringResource(R.string.cancel),
        modifier = modifier
    )
}

@OptIn(ExperimentalPermissionsApi::class)
fun PermissionState.checkOrRequestPermission(
    rationalShown: Boolean,
    onShowRational: () -> Unit,
    onPermissionsGranted: () -> Unit = {},
    onRequestPermission: () -> Unit = {}
) {
    when {
        status.isGranted -> onPermissionsGranted()
        rationalShown || status.shouldShowRationale -> onShowRational()
        else -> {
            Timber.d("requesting permissions")
            launchPermissionRequest()
            onRequestPermission()
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
fun MultiplePermissionsState.checkOrRequestPermission(
    rationalShown: Boolean,
    onShowRational: () -> Unit,
    onPermissionsGranted: () -> Unit = {},
    onRequestPermission: () -> Unit = {}
) {
    when {
        allPermissionsGranted -> onPermissionsGranted()
        rationalShown || permissions.any { it.status.shouldShowRationale } -> onShowRational()
        else -> {
            Timber.d("requesting permissions")
            launchMultiplePermissionRequest()
            onRequestPermission()
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun DisplayRationalDialog(
    title: String,
    rational: String,
    rationalShown: Boolean,
    launchPermissionRequest: () -> Unit,
    onRequestPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismissRequest: () -> Unit,
    dismissButtonLabel: String,
    modifier: Modifier = Modifier

) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            val resId =
                if (rationalShown) R.string.open_app_settings else R.string.continue_button
            TextButton(onClick = {
                if (rationalShown) {
                    onOpenSettings()
                } else {
                    Timber.d("Requesting permission after rational")
                    launchPermissionRequest()
                }
                onRequestPermissions()
            }) {
                Text(text = stringResource(resId))
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = dismissButtonLabel)
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

@OptIn(ExperimentalPermissionsApi::class)
@PreviewLightDark
@Composable
private fun PreviewShowLocationRational() {
    // Fake permissions... always granted in preview
    val permissionState = rememberMultiplePermissionsState(permissions = emptyList())

    WalkingForRochesterTheme {
        ShowLocationRational(
            locationPermissionState = permissionState,
            rationalShown = false,
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@PreviewLightDark
@Composable
private fun PreviewShowNotificationRational() {
    // Fake permissions... always granted in preview
    val permissionState = rememberPermissionState("notification")

    WalkingForRochesterTheme {
        ShowNotificationRational(
            notificationPermissionState = permissionState,
            rationalShown = false
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@PreviewLightDark
@Composable
private fun PreviewShowCameraRational() {
    // Fake permissions... always granted in preview
    val permissionState = rememberPermissionState("camera")

    WalkingForRochesterTheme {
        ShowCameraRational(
            cameraPermissionState = permissionState,
            rationalShown = true
        )
    }
}

@Composable
fun rememberOnOpenSettings(
    onResult: (ActivityResult) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        onResult(it)
    }

    return remember { { openSettings(launcher) } }
}

private fun openSettings(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
    val intent = Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
    }

    try {
        Timber.d("showing settings...")
        launcher.launch(intent)
    } catch (e: ActivityNotFoundException) {
        Timber.w("Failed to open settings: %s", e.message)
    }
}