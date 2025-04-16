package com.walkingforrochester.walkingforrochester.android.ui.composable.takepicture

import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakePictureScreen(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    onCompletion: () -> Unit = {},
    takePictureViewModel: TakePictureViewModel = hiltViewModel()
) {
    BackHandler(
        onBack = {
            takePictureViewModel.removeImage()
            onCompletion()
        }
    )

    val context = LocalContext.current
    val imageUri by takePictureViewModel.imageUri.collectAsStateWithLifecycle()

    TakePictureContent(
        imageUri = imageUri,
        windowSizeClass = windowSizeClass,
        modifier = modifier,
        onNavigateBack = onCompletion,
        onCaptureImage = { takePictureViewModel.captureImageFile(it) },
        onConfirmImage = {
            takePictureViewModel.confirmImage(context.filesDir)
            onCompletion()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakePictureContent(
    imageUri: Uri,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onCaptureImage: (File) -> Unit = {},
    onConfirmImage: () -> Unit = {}
) {
    var captureImage by rememberSaveable { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    FilledIconButton(
                        onClick = {
                            if (captureImage) {
                                onNavigateBack()
                            } else {
                                captureImage = true
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.3f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Discard Image",
                        )
                    }
                },
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { contentPadding ->
        AnimatedContent(
            targetState = captureImage,
            transitionSpec = {
                fadeIn(animationSpec = tween(220))
                    .togetherWith(fadeOut(animationSpec = tween(90)))
            }
        ) {
            if (it) {
                CaptureImage(
                    windowSizeClass = windowSizeClass,
                    modifier = modifier.fillMaxSize(),
                    onImageFile = { file ->
                        onCaptureImage(file)
                        captureImage = false
                    }
                )
            } else {
                ConfirmImage(
                    imageUri = imageUri,
                    windowSizeClass = windowSizeClass,
                    modifier = modifier.fillMaxSize(),
                    onConfirmImage = {
                        onConfirmImage()
                        captureImage = true
                    },
                    onDiscardImage = {
                        captureImage = true
                    },
                    contentPadding = contentPadding
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewTakePicture() {
    WalkingForRochesterTheme {
        val info = currentWindowAdaptiveInfo()

        TakePictureContent(
            imageUri = Uri.EMPTY,
            windowSizeClass = info.windowSizeClass
        )
    }
}