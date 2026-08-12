package com.walkingforrochester.walkingforrochester.android.ui.composable.takepicture

import androidx.camera.core.SurfaceRequest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.RippleDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ktx.isPortraitMode
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun CaptureImage(
    portraitMode: Boolean,
    modifier: Modifier = Modifier,
    surfaceRequest: SurfaceRequest? = null,
    onBindUseCases: (LifecycleOwner) -> Unit = {},
    onUnbindUseCases: () -> Unit = {},
    onCaptureImage: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // Bind use cases only while this screen is active.
    LifecycleStartEffect(key1 = lifecycleOwner) {
        onBindUseCases(lifecycleOwner)

        onStopOrDispose {
            onUnbindUseCases()
        }
    }

    Box(modifier = modifier) {

        PreviewImage(
            surfaceRequest = surfaceRequest,
            modifier = Modifier.fillMaxSize()
        )

        val alignment = if (portraitMode) Alignment.BottomCenter else Alignment.CenterEnd
        val padding =
            if (portraitMode) PaddingValues(bottom = 24.dp) else PaddingValues(end = 24.dp)

        @OptIn(ExperimentalMaterial3Api::class)
        val darkRippleConfig = RippleConfiguration(
            color = Color.Black,
            rippleAlpha = RippleDefaults.RippleAlpha
        )
        CompositionLocalProvider(LocalRippleConfiguration provides darkRippleConfig) {
            OutlinedIconButton(
                onClick = { onCaptureImage() },
                modifier = Modifier
                    .safeDrawingPadding()
                    .padding(padding)
                    .align(alignment)
                    .size(64.dp), // Set total button target size here
                border = BorderStroke(width = 2.dp, color = Color.White),
                colors = IconButtonDefaults.outlinedIconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.3f),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_circle_24dp),
                    modifier = Modifier.size(60.dp),
                    contentDescription = stringResource(R.string.take_picture)
                )
            }
        }
    }
}

@PreviewScreenSizes
@Composable
private fun PreviewCaptureImage() {
    WalkingForRochesterTheme {
        Surface {
            val portraitMode = currentWindowAdaptiveInfo().windowSizeClass.isPortraitMode()

            CaptureImage(
                portraitMode = portraitMode
            )
        }
    }
}