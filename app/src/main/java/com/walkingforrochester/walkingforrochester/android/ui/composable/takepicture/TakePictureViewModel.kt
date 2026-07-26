package com.walkingforrochester.walkingforrochester.android.ui.composable.takepicture

import android.content.Context
import android.net.Uri
import android.util.Size
import androidx.annotation.IntRange
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.UseCase
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walkingforrochester.walkingforrochester.android.di.IODispatcher
import com.walkingforrochester.walkingforrochester.android.ktx.compressImage
import com.walkingforrochester.walkingforrochester.android.ktx.mainExecutorCompat
import com.walkingforrochester.walkingforrochester.android.ktx.takePicture
import com.walkingforrochester.walkingforrochester.android.repository.WalkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TakePictureViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val walkRepository: WalkRepository,
    @param:IODispatcher val ioDispatcher: CoroutineDispatcher,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val preview = Preview.Builder()
        .setResolutionSelector(
            ResolutionSelector.Builder().setAspectRatioStrategy(
                AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY
            ).build()
        ).build()

    private val imageCapture = ImageCapture.Builder()
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

    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    private val _camera = MutableStateFlow<Camera?>(null)
    private var cameraProvider: ProcessCameraProvider? = null

    val surfaceRequest = _surfaceRequest.asStateFlow()

    val captureImageUri = savedStateHandle.getStateFlow(IMAGE_URI_KEY, Uri.EMPTY)

    fun updateOrientation(@IntRange(from = 0, to = 359) orientation: Int) {
        imageCapture.targetRotation = UseCase.snapToSurfaceRotation(orientation)
    }

    fun bindUseCases(
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    ) = viewModelScope.launch {
        val provider = ProcessCameraProvider.awaitInstance(context)
        cameraProvider = provider

        provider.unbindAll()

        // CameraX will call this whenever it needs a surface to draw into.
        preview.surfaceProvider = { newRequest ->
            _surfaceRequest.update { newRequest }
        }

        _camera.update {
            provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        }
    }

    fun unbindUseCases() {
        cameraProvider?.unbindAll()
        preview.surfaceProvider = null
        _surfaceRequest.update { null }
        cameraProvider = null
    }

    override fun onCleared() {
        unbindUseCases()
    }

    fun captureImage() = viewModelScope.launch {

        try {
            val captureFile = File(context.cacheDir, CAPTURE_FILE_NAME)
            val uri = imageCapture.takePicture(
                captureFile = captureFile,
                executor = context.mainExecutorCompat,
            ) ?: Uri.EMPTY
            Timber.d("updateImageFile: %s", uri)
            savedStateHandle[IMAGE_URI_KEY] = uri
        } catch (e: CancellationException) {
            Timber.d("Capture cancelled")
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Unexpected exception")
            savedStateHandle[IMAGE_URI_KEY] = Uri.EMPTY
        }
    }

    fun discardImage() = viewModelScope.launch {
        savedStateHandle[IMAGE_URI_KEY] = Uri.EMPTY
    }

    fun confirmImage() = viewModelScope.launch {
        val confirmFile = File(context.cacheDir, CONFIRM_FILE_NAME)
        if (compressImage(confirmFile)) {
            walkRepository.updateImageUri(confirmFile.toUri())
        } else {
            walkRepository.updateImageUri(Uri.EMPTY)
        }
    }

    fun removeImage() {
        walkRepository.updateImageUri(Uri.EMPTY)
    }

    private suspend fun compressImage(targetFile: File): Boolean = withContext(ioDispatcher) {
        try {
            val imageUri: Uri? = savedStateHandle[IMAGE_URI_KEY]
            if (imageUri != null && imageUri != Uri.EMPTY) {
                val imageFile = imageUri.toFile()
                imageFile.compressImage(
                    targetFile = targetFile,
                    targetWidth = PHOTO_WIDTH,
                    targetHeight = PHOTO_HEIGHT
                )
            } else {
                Timber.w("Image uri is null")
                false
            }
        } catch (e: CancellationException) {
            Timber.d("Compress cancelled")
            throw e
        } catch (e: Exception) {
            Timber.w("Failed to compress image file: %s", e.message)
            false
        }
    }

    companion object {
        const val CAPTURE_FILE_NAME = "wfr_walk_capture.jpg"
        const val CONFIRM_FILE_NAME = "wfr_walk_confirm.jpg"

        const val PHOTO_WIDTH = 768
        const val PHOTO_HEIGHT = 1024

        private const val IMAGE_URI_KEY = "imageUri"
    }
}