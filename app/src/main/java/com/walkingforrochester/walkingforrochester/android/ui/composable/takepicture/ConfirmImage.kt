package com.walkingforrochester.walkingforrochester.android.ui.composable.takepicture

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ktx.isPortraitMode
import com.walkingforrochester.walkingforrochester.android.ui.modifier.backgroundInPreview
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmImage(
    imageUri: Uri,
    portraitMode: Boolean,
    modifier: Modifier = Modifier,
    onConfirmImage: () -> Unit = {},
    onDiscardImage: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues()
) {
    Box(modifier = modifier) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUri)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .backgroundInPreview(Color.Gray),
            error = ColorPainter(Color.Gray),
            fallback = ColorPainter(Color.Gray),
            contentScale = ContentScale.Crop
        )
        val buttonColors = IconButtonDefaults.filledIconButtonColors(
            containerColor = Color.Black.copy(alpha = 0.4f),
            contentColor = Color.White
        )

        val buttonModifier = Modifier.size(56.dp)

        if (portraitMode) {
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
                    iconResId = R.drawable.ic_close_48dp,
                    iconDescriptionId = R.string.discard_image_desc,
                    modifier = buttonModifier,
                    buttonColors = buttonColors
                )

                Spacer(buttonModifier)
                ConfirmImageButton(
                    onClick = onConfirmImage,
                    iconResId = R.drawable.ic_check_48dp,
                    iconDescriptionId = R.string.confirm_image_desc,
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
                    .padding(contentPadding)
                    .align(Alignment.CenterEnd)
            ) {

                Spacer(Modifier.weight(1f))

                ConfirmImageButton(
                    onClick = onConfirmImage,
                    iconResId = R.drawable.ic_check_48dp,
                    iconDescriptionId = R.string.confirm_image_desc,
                    modifier = buttonModifier,
                    buttonColors = buttonColors
                )

                Spacer(buttonModifier)

                ConfirmImageButton(
                    onClick = onDiscardImage,
                    iconResId = R.drawable.ic_close_48dp,
                    iconDescriptionId = R.string.discard_image_desc,
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
    @DrawableRes iconResId: Int,
    @StringRes iconDescriptionId: Int,
    modifier: Modifier = Modifier,
    buttonColors: IconButtonColors = IconButtonDefaults.filledIconButtonColors()
) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier,
        colors = buttonColors
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = stringResource(iconDescriptionId),
            modifier = modifier.padding(8.dp),
        )
    }
}

@PreviewScreenSizes
@Composable
private fun PreviewConfirmImage() {
    WalkingForRochesterTheme {
        Surface {
            val portraitMode = currentWindowAdaptiveInfo().windowSizeClass.isPortraitMode()
            ConfirmImage(
                imageUri = Uri.EMPTY,
                portraitMode = portraitMode
            )
        }
    }
}