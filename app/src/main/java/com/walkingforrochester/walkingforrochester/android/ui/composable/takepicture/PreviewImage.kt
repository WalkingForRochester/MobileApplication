package com.walkingforrochester.walkingforrochester.android.ui.composable.takepicture

import android.view.Surface
import android.view.ViewGroup
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.doOnAttach
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun PreviewImage(
    modifier: Modifier = Modifier,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
    onUpdateUseCases: (
        surfaceProvider: Preview.SurfaceProvider,
        rotation: Int
    ) -> Unit = { sp, r -> }
) {
    // Unable to show android view in preview, so drop in a placeholder
    if (LocalInspectionMode.current) {
        Box(modifier = modifier.background(Color.Gray))
    } else {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                val previewView = PreviewView(context).apply {
                    this.scaleType = scaleType
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
                previewView.doOnAttach {
                    onUpdateUseCases(
                        previewView.surfaceProvider,
                        previewView.display?.rotation ?: Surface.ROTATION_0
                    )
                }
                previewView
            }
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewImagePreview() {
    WalkingForRochesterTheme {
        PreviewImage(modifier = Modifier.size(200.dp))
    }
}