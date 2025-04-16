package com.walkingforrochester.walkingforrochester.android.ui.composable.submitwalk

import android.Manifest
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun SubmitWalkScreen(
    modifier: Modifier = Modifier,
    onCompletion: () -> Unit = {},
    onTakePicture: () -> Unit = {},
    windowSizeClass: WindowSizeClass = WindowSizeClass.compute(410.dp.value, 800.dp.value),
    logAWalkViewModel: LogAWalkViewModel = hiltViewModel()
) {
    val walkData by logAWalkViewModel.currentWalk.collectAsStateWithLifecycle()
    val permissionPreferences by logAWalkViewModel.permissionPreferences.collectAsStateWithLifecycle()

    SubmitWalkContent(
        walkData = walkData,
        cameraRationalShown = permissionPreferences.cameraRationalShown,
        windowSizeClass = windowSizeClass,
        modifier = modifier,
        onDiscardWalk = {
            logAWalkViewModel.onDiscardWalking()
            onCompletion()
        },
        onLitterChange = logAWalkViewModel::onBagsCollectedChange,
        onTakePicture = onTakePicture,
        onSubmitWalk = logAWalkViewModel::onSubmitWalking
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SubmitWalkContent(
    walkData: WalkData,
    cameraRationalShown: Boolean,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    onDiscardWalk: () -> Unit = {},
    onLitterChange: (Int) -> Unit = {},
    onTakePicture: () -> Unit = {},
    onSubmitWalk: () -> Unit = {},
    onUpdateCameraRationalShown: (Boolean) -> Unit = {}
) {
    var showDiscardWalkDialog by remember { mutableStateOf(false) }
    var showPhotoRequiredDialog by remember { mutableStateOf(false) }

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

        val dividerColor = DividerDefaults.color.copy(alpha = 0.3f)

        val portrait = windowSizeClass.windowHeightSizeClass != WindowHeightSizeClass.COMPACT &&
            windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.EXPANDED

        if (portrait) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
            ) {

                // To not allow map to dominate on tablets, use a bigger
                // ratio vs phones
                val ratio = when (windowSizeClass.windowWidthSizeClass) {
                    WindowWidthSizeClass.COMPACT -> 1.4f
                    else -> 2f
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(ratio)
                ) {
                    SubmitWalkMap(
                        walkData = walkData,
                        modifier = Modifier.fillMaxSize()
                    )
                    HorizontalDivider(
                        modifier = Modifier.align(Alignment.TopCenter),
                        color = dividerColor
                    )
                    HorizontalDivider(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        color = dividerColor
                    )
                }

                SubmitWalkDetails(
                    walkData = walkData,
                    cameraRationalShown = cameraRationalShown,
                    modifier = Modifier.fillMaxWidth(),
                    onLitterChange = onLitterChange,
                    onTakePicture = onTakePicture,
                    onUpdateCameraRationalShown = onUpdateCameraRationalShown
                )
            }
        } else {
            // Landscape
            val layoutDirection = LocalLayoutDirection.current

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = padding.calculateTopPadding(),
                        end = padding.calculateRightPadding(layoutDirection)
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    SubmitWalkMap(
                        walkData = walkData,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = padding.calculateLeftPadding(layoutDirection),
                            bottom = padding.calculateBottomPadding()
                        )
                    )
                    HorizontalDivider(
                        modifier = Modifier.align(Alignment.TopCenter),
                        color = dividerColor
                    )
                    VerticalDivider(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        color = dividerColor
                    )
                }

                SubmitWalkDetails(
                    walkData = walkData,
                    cameraRationalShown = cameraRationalShown,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    onLitterChange = onLitterChange,
                    onTakePicture = onTakePicture,
                    onUpdateCameraRationalShown = onUpdateCameraRationalShown
                )
            }
        }
    }
}


@Composable
fun SubmitWalkMap(
    walkData: WalkData,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
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
        },
        contentPadding = contentPadding
    ) {
        RenderWalkDataOnMap(walkData)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SubmitWalkDetails(
    walkData: WalkData,
    cameraRationalShown: Boolean,
    modifier: Modifier = Modifier,
    onLitterChange: (Int) -> Unit = {},
    onTakePicture: () -> Unit = {},
    onUpdateCameraRationalShown: (Boolean) -> Unit = {}
) {
    var showCameraPermissionRational by remember { mutableStateOf(false) }

    val cameraPermission = rememberPermissionState(
        permission = Manifest.permission.CAMERA,
        onPermissionResult = { permissionGranted ->
            if (permissionGranted) {
                onUpdateCameraRationalShown(false)
                onTakePicture()
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

    Column(
        modifier = modifier.padding(16.dp),
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
                            onTakePicture()
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
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(8.dp))
            WFROutlinedButton(
                onClick = { onTakePicture() },
                label = R.string.retake_button
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
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

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@PreviewLightDark
//@PreviewScreenSizes
@Composable
fun SubmitWalkContentPreview() {
    WalkingForRochesterTheme {
        Surface {
            val info = currentWindowAdaptiveInfo()

            SubmitWalkContent(
                walkData = WalkData(
                    distanceMeters = 100.0,
                    durationMilli = 5332,
                    bagsOfLitter = 1
                ),
                cameraRationalShown = false,
                windowSizeClass = info.windowSizeClass
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