package com.walkingforrochester.walkingforrochester.android.ui.composable.takepicture

import android.Manifest
import android.view.OrientationEventListener
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ktx.isPortraitMode
import com.walkingforrochester.walkingforrochester.android.ui.composable.takepicture.TakePictureEvent.CaptureImage
import com.walkingforrochester.walkingforrochester.android.ui.composable.takepicture.TakePictureEvent.ConfirmImage
import com.walkingforrochester.walkingforrochester.android.ui.composable.takepicture.TakePictureEvent.ImageConfirmed
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun TakePictureScreen(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    takePictureViewModel: TakePictureViewModel = hiltViewModel()
) {
    BackHandler(
        onBack = {
            takePictureViewModel.removeImage()
            onNavigateBack()
        }
    )

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    val portraitMode = windowSizeClass.isPortraitMode()

    // Ensure camera permission is still granted. If user used
    // one time permission and app was closed too long, we will loose
    // permission and no longer be able to use camera. In that
    // case we will invoke onCompletion() to trigger navigation back
    // to our caller.
    if (cameraPermission.status == PermissionStatus.Granted) {

        val takePictureState by takePictureViewModel.takePictureState.collectAsStateWithLifecycle()

        MonitorOrientation(
            onOrientationChanged = { takePictureViewModel.updateOrientation(it) }
        )

        TakePictureContent(
            portraitMode = portraitMode,
            takePictureState = takePictureState,
            modifier = modifier,
            onNavigateBack = onNavigateBack,
            onBindUseCases = { lifecycleOwner -> takePictureViewModel.bindUseCases(lifecycleOwner) },
            onUnbindUseCases = { takePictureViewModel.unbindUseCases() },
            onCaptureImage = { takePictureViewModel.captureImage() },
            onDiscardImage = { takePictureViewModel.discardImage() },
            onConfirmImage = { takePictureViewModel.confirmImage() },
            onClearError = { takePictureViewModel.clearError() }
        )
    } else {
        takePictureViewModel.removeImage()
        onNavigateBack()
    }
}

@Composable
fun MonitorOrientation(
    onOrientationChanged: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    // Sensors not available in preview, so only setup this listener
    // on device/emulator
    val orientationEventListener = remember(context) {
        object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) {
                    return
                }

                onOrientationChanged(orientation)
            }
        }
    }

    // Monitor rotation for image capture on start/stop
    LifecycleStartEffect(context) {
        orientationEventListener.enable()

        onStopOrDispose { orientationEventListener.disable() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakePictureContent(
    portraitMode: Boolean,
    takePictureState: TakePictureState,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onBindUseCases: (LifecycleOwner) -> Unit = {},
    onUnbindUseCases: () -> Unit = {},
    onCaptureImage: () -> Unit = {},
    onDiscardImage: () -> Unit = {},
    onConfirmImage: () -> Unit = {},
    onClearError: () -> Unit = {},
) {
    val snackBarHostState = remember { SnackbarHostState() }

    val resources = LocalResources.current

    LaunchedEffect(takePictureState.error) {
        val errorMsg = when (takePictureState.error) {
            TakePictureError.None -> ""
            TakePictureError.CaptureError -> resources.getString(R.string.capture_error)
            TakePictureError.ConfirmError -> resources.getString(R.string.confirm_error)
        }

        if (errorMsg.isNotEmpty()) {
            snackBarHostState.showSnackbar(errorMsg)
            onClearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    FilledIconButton(
                        onClick = {
                            if (takePictureState.event == ConfirmImage) {
                                onDiscardImage()
                            } else {
                                onNavigateBack()
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.4f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back_24dp),
                            contentDescription = stringResource(R.string.back_button),
                        )
                    }
                },
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { contentPadding ->

        when (takePictureState.event) {
            ImageConfirmed -> onNavigateBack()
            else -> {
                AnimatedContent(
                    targetState = takePictureState.event,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220))
                            .togetherWith(fadeOut(animationSpec = tween(90)))
                    }
                ) {
                    if (it == CaptureImage) {
                        CaptureImage(
                            portraitMode = portraitMode,
                            modifier = modifier.fillMaxSize(),
                            surfaceRequest = takePictureState.surfaceRequest,
                            onBindUseCases = onBindUseCases,
                            onUnbindUseCases = onUnbindUseCases,
                            onCaptureImage = onCaptureImage
                        )
                    } else {
                        ConfirmImage(
                            imageUri = takePictureState.imageUri,
                            portraitMode = portraitMode,
                            modifier = modifier.fillMaxSize(),
                            onConfirmImage = onConfirmImage,
                            onDiscardImage = onDiscardImage,
                            contentPadding = contentPadding
                        )
                    }
                }
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun PreviewTakePicture() {
    WalkingForRochesterTheme {
        Surface {
            val portraitMode = currentWindowAdaptiveInfo().windowSizeClass.isPortraitMode()
            TakePictureContent(
                portraitMode = portraitMode,
                takePictureState = TakePictureState()
            )
        }
    }
}