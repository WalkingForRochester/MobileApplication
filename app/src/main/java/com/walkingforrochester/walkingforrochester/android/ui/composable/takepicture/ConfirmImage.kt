package com.walkingforrochester.walkingforrochester.android.ui.composable.takepicture

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import coil.compose.AsyncImage
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.modifier.backgroundInPreview
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmImage(
    imageUri: Uri,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    onConfirmImage: () -> Unit = {},
    onDiscardImage: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues()
) {
    val portrait = windowSizeClass.windowHeightSizeClass != WindowHeightSizeClass.COMPACT &&
        windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.EXPANDED

    Box(modifier = modifier) {
        AsyncImage(
            model = imageUri,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .backgroundInPreview(Color.Gray),
            contentScale = ContentScale.Crop
        )
        val buttonColors = IconButtonDefaults.filledIconButtonColors(
            containerColor = Color.Black.copy(alpha = 0.3f),
            contentColor = Color.White
        )

        val buttonModifier = Modifier.size(56.dp)

        if (portrait) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .padding(contentPadding)
                    .align(Alignment.BottomCenter)
            ) {
                Spacer(Modifier.weight(1f))

                ConfirmImageButton(
                    onClick = onDiscardImage,
                    iconVector = Icons.Filled.Close,
                    iconDescription = stringResource(R.string.discard_image_desc),
                    modifier = buttonModifier,
                    buttonColors = buttonColors
                )

                Spacer(buttonModifier)
                ConfirmImageButton(
                    onClick = onConfirmImage,
                    iconVector = Icons.Filled.Check,
                    iconDescription = stringResource(R.string.confirm_image_desc),
                    modifier = buttonModifier,
                    buttonColors = buttonColors
                )

                Spacer(Modifier.weight(1f))
            }
        } else {

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 24.dp)
                    .safeDrawingPadding()
                    .align(Alignment.CenterEnd)
            ) {

                Spacer(Modifier.weight(1f))

                ConfirmImageButton(
                    onClick = onConfirmImage,
                    iconVector = Icons.Filled.Check,
                    iconDescription = stringResource(R.string.confirm_image_desc),
                    modifier = buttonModifier,
                    buttonColors = buttonColors
                )

                Spacer(buttonModifier)

                ConfirmImageButton(
                    onClick = onDiscardImage,
                    iconVector = Icons.Filled.Close,
                    iconDescription = stringResource(R.string.discard_image_desc),
                    modifier = buttonModifier,
                    buttonColors = buttonColors
                )

                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ConfirmImageButton(
    onClick: () -> Unit,
    iconVector: ImageVector,
    iconDescription: String,
    modifier: Modifier = Modifier,
    buttonColors: IconButtonColors = IconButtonDefaults.filledIconButtonColors()
) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier,
        colors = buttonColors
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = iconDescription,
            modifier = modifier.padding(8.dp),
        )
    }
}

@PreviewScreenSizes
@Composable
private fun PreviewConfirmImage() {
    WalkingForRochesterTheme {
        Surface {
            val info = currentWindowAdaptiveInfo()
            ConfirmImage(
                imageUri = Uri.EMPTY,
                windowSizeClass = info.windowSizeClass
            )
        }
    }
}