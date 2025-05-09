package com.walkingforrochester.walkingforrochester.android.ui.composable.takepicture

import android.util.Size
import android.view.OrientationEventListener
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ktx.getCameraProvider
import com.walkingforrochester.walkingforrochester.android.ktx.takePicture
import com.walkingforrochester.walkingforrochester.android.ui.composable.takepicture.TakePictureViewModel.Companion.PHOTO_HEIGHT
import com.walkingforrochester.walkingforrochester.android.ui.composable.takepicture.TakePictureViewModel.Companion.PHOTO_WIDTH
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

@Composable
fun CaptureImage(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    onImageFile: (File) -> Unit = {},
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
) {
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val previewUseCase = remember {
        Preview.Builder()
            .setResolutionSelector(
                ResolutionSelector.Builder().setAspectRatioStrategy(
                    AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY
                ).build()
            ).build()
    }

    val imageCaptureUseCase = remember {
        ImageCapture.Builder()
            .setCaptureMode(CAPTURE_MODE_MINIMIZE_LATENCY)
            .setResolutionSelector(
                ResolutionSelector.Builder()
                    .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                    .setResolutionStrategy(
                        ResolutionStrategy(
                            Size(PHOTO_WIDTH, PHOTO_HEIGHT),
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                        )
                    ).build()
            )
            .build()
    }

    val previewMode = LocalInspectionMode.current
    if (!previewMode) {
        // Sensors not available in preview, so only setup this listener
        // on device/emulator
        val orientationEventListener = remember(context) {
            object : OrientationEventListener(context) {
                override fun onOrientationChanged(orientation: Int) {
                    if (orientation == ORIENTATION_UNKNOWN) {
                        return
                    }

                    val rotation = UseCase.snapToSurfaceRotation(orientation)
                    imageCaptureUseCase.targetRotation = rotation
                }
            }
        }

        // Monitor rotation for image capture on start/stop
        LifecycleStartEffect(context) {
            orientationEventListener.enable()

            onStopOrDispose { orientationEventListener.disable() }
        }
    }

    val portrait = windowSizeClass.windowHeightSizeClass != WindowHeightSizeClass.COMPACT &&
        windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.EXPANDED

    Box(modifier = modifier) {

        PreviewImage(
            modifier = Modifier.fillMaxSize(),
            onUpdateUseCases = { surfaceProvider ->
                previewUseCase.surfaceProvider = surfaceProvider
            }
        )

        val alignment = if (portrait) Alignment.BottomCenter else Alignment.CenterEnd
        val padding = if (portrait) PaddingValues(bottom = 24.dp) else PaddingValues(end = 24.dp)

        OutlinedButton(
            onClick = {
                coroutineScope.launch {
                    val file = imageCaptureUseCase.takePicture(
                        context = context,
                        fileName = TakePictureViewModel.CAPTURE_FILE_NAME
                    )
                    onImageFile(file)
                }
            },
            modifier = Modifier
                .padding(padding)
                .safeDrawingPadding()
                .align(alignment),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Black.copy(alpha = 0.3f),
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(0.dp),
            border = BorderStroke(width = 2.dp, color = Color.White)
        ) {
            Icon(
                imageVector = Icons.Default.Circle,
                modifier = Modifier.size(60.dp),
                contentDescription = stringResource(R.string.take_picture)
            )
        }

        LaunchedEffect(Unit) {
            val cameraProvider = context.getCameraProvider()
            try {
                // Must unbind the use-cases before rebinding them.
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, previewUseCase, imageCaptureUseCase
                )
            } catch (ex: Exception) {
                Timber.e(ex, "Failed to bind camera use cases")
            }
        }
    }
}

@PreviewScreenSizes
@Composable
private fun PreviewCaptureImage() {
    WalkingForRochesterTheme {
        Surface {
            val info = currentWindowAdaptiveInfo()
            CaptureImage(
                windowSizeClass = info.windowSizeClass
            )
        }
    }
}