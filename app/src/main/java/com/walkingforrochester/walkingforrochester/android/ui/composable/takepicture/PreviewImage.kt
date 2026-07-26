package com.walkingforrochester.walkingforrochester.android.ui.composable.takepicture

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.SurfaceRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme


@Composable
fun PreviewImage(
    surfaceRequest: SurfaceRequest?,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current || surfaceRequest == null) {
        Box(modifier.background(Color.Gray))
    } else {
        CameraXViewfinder(
            surfaceRequest = surfaceRequest,
            modifier = modifier
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewImagePreview() {
    WalkingForRochesterTheme {
        PreviewImage(
            surfaceRequest = null,
            modifier = Modifier.size(200.dp)
        )
    }
}