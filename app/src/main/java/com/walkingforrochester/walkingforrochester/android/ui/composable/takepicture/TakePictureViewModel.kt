package com.walkingforrochester.walkingforrochester.android.ui.composable.takepicture

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walkingforrochester.walkingforrochester.android.di.IODispatcher
import com.walkingforrochester.walkingforrochester.android.ktx.compressImage
import com.walkingforrochester.walkingforrochester.android.repository.WalkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TakePictureViewModel @Inject constructor(
    private val walkRepository: WalkRepository,
    @IODispatcher val ioDispatcher: CoroutineDispatcher,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val imageUri = savedStateHandle.getStateFlow(IMAGE_URI_KEY, Uri.EMPTY)

    fun captureImageFile(imageFile: File) {
        Timber.d("updateImageFile: %s", imageFile.name)
        savedStateHandle[IMAGE_URI_KEY] = imageFile.toUri()
    }

    fun confirmImage(context: Context) = viewModelScope.launch {
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
        } catch (e: Exception) {
            Timber.w("Failed to rename file: %s", e.message)
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