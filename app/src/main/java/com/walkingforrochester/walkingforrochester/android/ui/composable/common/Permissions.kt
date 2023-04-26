package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.walkingforrochester.walkingforrochester.android.BuildConfig
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.savePreference
import timber.log.Timber

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestPermissions(
    permissionState: MultiplePermissionsState,
    @StringRes askedOncePrefKey: Int,
    @StringRes dontAskAgainPrefKey: Int,
    rationaleContent: @Composable () -> Unit,
    askManualGrantContent: @Composable () -> Unit
) {
    val context = LocalContext.current
    val askedOnce by booleanPreferenceState(
        key = stringResource(askedOncePrefKey),
        defaultValue = false
    )
    val dontAskAgain by booleanPreferenceState(
        key = stringResource(dontAskAgainPrefKey),
        defaultValue = false
    )

    LaunchedEffect(Unit) {
        if (!askedOnce) {
            Timber.d("requesting permissions for the first time")
            permissionState.launchMultiplePermissionRequest()
        }
    }

    when {
        dontAskAgain -> {
            Timber.d("asking to grant permissions manually")
            askManualGrantContent()
        }

        permissionState.shouldShowRationale -> {
            Timber.d("showing rationale")
            LaunchedEffect(Unit) {
                savePreference(
                    context,
                    context.getString(askedOncePrefKey),
                    true
                )
            }
            rationaleContent()
        }

        !askedOnce -> {
            LaunchedEffect(Unit) {
                Timber.d("requesting permissions for the first time")
                permissionState.launchMultiplePermissionRequest()
            }
        }

        !permissionState.allPermissionsGranted && !permissionState.shouldShowRationale -> {
            Timber.d("setting don't ask again preference")
            LaunchedEffect(Unit) {
                savePreference(
                    context,
                    context.getString(dontAskAgainPrefKey),
                    true
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestLocationPermissionsScreen(permissionState: MultiplePermissionsState) {
    RequestPermissions(
        permissionState = permissionState,
        askedOncePrefKey = R.string.wfr_asked_location_permission_once,
        dontAskAgainPrefKey = R.string.wfr_dont_ask_location_permissions,
        rationaleContent = { RequestLocationPermissionsContent(onRequestPermissions = permissionState::launchMultiplePermissionRequest) },
        askManualGrantContent = { RequestLocationPermissionsContent(dontAskAgain = true) }
    )
}

@Composable
private fun RequestLocationPermissionsContent(
    dontAskAgain: Boolean = false,
    onRequestPermissions: () -> Unit = {}
) {
    val context = LocalContext.current
    val isAtLeastAndroidR = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    val textToShow =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            stringResource(R.string.location_permission_rationale)
        } else {
            stringResource(R.string.location_permission_rationale_before_tiramisu)
        }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = textToShow,
            style = MaterialTheme.typography.headlineSmall.copy(color = Color.Black),
            textAlign = TextAlign.Center
        )
        if (dontAskAgain && isAtLeastAndroidR) {
            Spacer(modifier = Modifier.size(24.dp))
            Text(
                text = stringResource(R.string.please_open_settings_to_grant_permissions),
                style = MaterialTheme.typography.headlineSmall.copy(color = Color.Black),
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.size(24.dp))
        WFRButton(
            label = if (dontAskAgain) R.string.open_app_settings else R.string.grant_permissions,
            onClick = {
                if (dontAskAgain && isAtLeastAndroidR) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    ContextCompat.startActivity(context, intent, null)
                } else {
                    onRequestPermissions()
                }
            }
        )
    }
}
