package com.walkingforrochester.walkingforrochester.android.ui.composable.takepicture

import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walkingforrochester.walkingforrochester.android.di.IODispatcher
import com.walkingforrochester.walkingforrochester.android.repository.WalkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val walkRepository: WalkRepository,
    @IODispatcher val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _imageUri = MutableStateFlow(Uri.EMPTY)
    val imageUri = _imageUri.asStateFlow()

    fun captureImageFile(imageFile: File) {
        Timber.d("updateImageFile: %s", imageFile.name)
        _imageUri.update { imageFile.toUri() }
    }

    fun confirmImage(filesDir: File) = viewModelScope.launch {
        val confirmFile = File(filesDir, CONFIRM_FILE_NAME)
        if (renameFile(confirmFile)) {
            walkRepository.updateImageUri(confirmFile.toUri())
        } else {
            walkRepository.updateImageUri(Uri.EMPTY)
        }
    }

    private suspend fun renameFile(targetFile: File): Boolean = withContext(ioDispatcher) {
        try {
            val imageFile = _imageUri.value.toFile()
            imageFile.renameTo(targetFile)
            true
        } catch (e: Exception) {
            Timber.w("Failed to rename file: %s", e.message)
            false
        }
    }

    fun removeImage() {
        walkRepository.updateImageUri(Uri.EMPTY)
    }

    companion object {
        const val CAPTURE_FILE_NAME = "wfr_walk_capture.jpg"
        const val CONFIRM_FILE_NAME = "wfr_walk_confirm.jpg"
    }
}