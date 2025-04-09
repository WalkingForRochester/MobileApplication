package com.walkingforrochester.walkingforrochester.android.ui.composable.submitwalk

import android.Manifest
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.formatElapsedMilli
import com.walkingforrochester.walkingforrochester.android.formatMetersToMiles
import com.walkingforrochester.walkingforrochester.android.ktx.backgroundInPreview
import com.walkingforrochester.walkingforrochester.android.model.WalkData
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.HorizontalNumberPicker
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.ShowCameraRational
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFROutlinedButton
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.checkOrRequestPermission
import com.walkingforrochester.walkingforrochester.android.ui.composable.logawalk.RenderWalkDataOnMap
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import com.walkingforrochester.walkingforrochester.android.viewmodel.LogAWalkViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun SubmitWalkScreen(
    modifier: Modifier = Modifier,
    onCompletion: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    logAWalkViewModel: LogAWalkViewModel = hiltViewModel()
) {
    val uiState by logAWalkViewModel.uiState.collectAsStateWithLifecycle()
    val walkData by logAWalkViewModel.currentWalk.collectAsStateWithLifecycle()
    val permissionPreferences by logAWalkViewModel.permissionPreferences.collectAsStateWithLifecycle()

    SubmitWalkContent(
        walkData = walkData,
        cameraRationalShown = permissionPreferences.cameraRationalShown,
        modifier = modifier,
        onDiscardWalk = {
            logAWalkViewModel.onDiscardWalking()
            onCompletion()
        },
        onLitterChange = logAWalkViewModel::onBagsCollectedChange,
        onSubmitWalk = logAWalkViewModel::onSubmitWalking
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SubmitWalkContent(
    walkData: WalkData,
    cameraRationalShown: Boolean,
    modifier: Modifier = Modifier,
    onDiscardWalk: () -> Unit = {},
    onLitterChange: (Int) -> Unit = {},
    onSubmitWalk: () -> Unit = {},
    onUpdateCameraRationalShown: (Boolean) -> Unit = {}
) {
    var showDiscardWalkDialog by remember { mutableStateOf(false) }
    var showPhotoRequiredDialog by remember { mutableStateOf(false) }
    var showCameraPermissionRational by remember { mutableStateOf(false) }

    BackHandler {
        showDiscardWalkDialog = true
    }

    val submitWalk: () -> Unit = {
        if (walkData.imageUri != Uri.EMPTY) {
            onSubmitWalk()
        } else {
            showPhotoRequiredDialog = true
        }
    }

    val cameraPermission = rememberPermissionState(
        permission = Manifest.permission.CAMERA,
        onPermissionResult = { permissionGranted ->
            if (permissionGranted) {
                onUpdateCameraRationalShown(false)
                // TODO show camera screen
                Timber.d("JSR show camera via permission result")
            }
        }
    )

    if (showCameraPermissionRational) {
        ShowCameraRational(
            cameraPermissionState = cameraPermission,
            rationalShown = cameraRationalShown,
            onRequestPermissions = {
                onUpdateCameraRationalShown(true)
                showCameraPermissionRational = false
            },
            onDismissRequest = { showCameraPermissionRational = false },
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Walk Details")
                },
                navigationIcon = {
                    IconButton(onClick = { showDiscardWalkDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.discard_button)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = submitWalk) {
                        Text(stringResource(R.string.submit))
                    }
                },
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
            )
        },
        contentWindowInsets = WindowInsets.systemBars.union(
            WindowInsets.displayCutout.only(
                WindowInsetsSides.Horizontal
            )
        )
    ) { padding ->

        if (showDiscardWalkDialog) {
            ConfirmDiscardWalkDialog(
                onDismiss = { showDiscardWalkDialog = false },
                onDiscardWalk = onDiscardWalk
            )
        }

        if (showPhotoRequiredDialog) {
            PhotoRequiredDialog(
                onDismiss = { showPhotoRequiredDialog = false }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding),
            //.padding(horizontal = 16.dp)

        ) {
            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.3f))
            SubmitWalkMap(
                walkData = walkData,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.3f))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.distance_label),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = walkData.distanceMeters.formatMetersToMiles(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.duration_label),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = walkData.durationMilli.formatElapsedMilli(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.how_many_bags_collected),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge
                )

                HorizontalNumberPicker(
                    minValue = 0,
                    maxValue = 15,
                    currentValue = walkData.bagsOfLitter,
                    onValueChange = onLitterChange
                )

                Spacer(modifier = Modifier.size(16.dp))

                if (walkData.imageUri == Uri.EMPTY) {
                    Text(
                        text = stringResource(R.string.take_picture_of_litter),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    IconButton(
                        onClick = {
                            cameraPermission.checkOrRequestPermission(
                                rationalShown = cameraRationalShown,
                                onPermissionsGranted = {
                                    onUpdateCameraRationalShown(false)
                                    // TODO show camera...
                                    Timber.d("JSR show camera via icon button")
                                },
                                onShowRational = {
                                    showCameraPermissionRational = true
                                }
                            )
                        },
                        modifier = Modifier.size(100.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = stringResource(
                                R.string.take_picture_of_litter_description
                            ),
                            modifier = Modifier.size(76.dp),
                        )
                    }
                    Spacer(Modifier.height(100.dp))
                } else {
                    AsyncImage(
                        model = walkData.imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(200.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    WFROutlinedButton(
                        onClick = {
                            // TODO show camera...
                        },
                        label = R.string.retake_button
                    )
                }

                ///Spacer(modifier = Modifier.height(16.dp))
                // Spacer(modifier = Modifier.weight(1f))

                /* WFRButton(
                 onClick = {
                 },
                 modifier = Modifier.widthIn(120.dp),
                 label = R.string.submit,
                 enabled = surveyDialogState.picUri != null
             )*/

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun SubmitWalkMap(
    walkData: WalkData,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState()

    val mapPadding = with(LocalDensity.current) { 64.dp.toPx().toInt() }

    val scope = rememberCoroutineScope()

    LaunchedEffect(walkData.bounds) {
        cameraPositionState.move(
            CameraUpdateFactory.newLatLngBounds(
                walkData.bounds,
                mapPadding
            )
        )
    }

    GoogleMap(
        modifier = modifier.backgroundInPreview(Color.Gray),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = true),
        uiSettings = MapUiSettings(zoomControlsEnabled = false),
        onMyLocationButtonClick = {
            scope.launch {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(
                        walkData.bounds,
                        mapPadding
                    )
                )
            }

            // indicate camera movement handled
            true
        }
    ) {
        RenderWalkDataOnMap(walkData)
    }
}

@Composable
fun ConfirmDiscardWalkDialog(
    onDismiss: () -> Unit = {},
    onDiscardWalk: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDiscardWalk) {
                Text(text = stringResource(R.string.discard_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        title = { Text(text = stringResource(R.string.discard_walk_title)) },
        text = {
            Text(text = stringResource(R.string.discard_walk_confirmation))
        }
    )
}

@Composable
fun PhotoRequiredDialog(
    onDismiss: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.ok_button))
            }
        },
        text = {
            Text(text = stringResource(R.string.photo_required))
        }
    )
}

@PreviewLightDark
@Composable
fun SubmitWalkContentPreview() {
    WalkingForRochesterTheme {
        Surface {
            SubmitWalkContent(
                walkData = WalkData(
                    distanceMeters = 100.0,
                    durationMilli = 5332,
                    bagsOfLitter = 1
                ),
                cameraRationalShown = false
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewConfirmDiscardWalkDialog() {
    WalkingForRochesterTheme {
        Surface {
            ConfirmDiscardWalkDialog()
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewPhotoRequiredDialog() {
    WalkingForRochesterTheme {
        Surface {
            PhotoRequiredDialog()
        }
    }
}