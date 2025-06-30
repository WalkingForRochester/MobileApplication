package com.walkingforrochester.walkingforrochester.android.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.walkingforrochester.walkingforrochester.android.di.IODispatcher
import com.walkingforrochester.walkingforrochester.android.model.AccountProfile
import com.walkingforrochester.walkingforrochester.android.repository.PreferenceRepository
import com.walkingforrochester.walkingforrochester.android.ui.state.MainUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val preferenceRepository: PreferenceRepository,
    @param:IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val imageLoader: ImageLoader
) : ViewModel() {

    init {
        // Configure image loader built with common okhttp
        SingletonImageLoader.setSafe {
            imageLoader
        }
    }

    private var _initialized = false
    val initialized = flow {
        while (!_initialized) {
            delay(10)
        }
        emit(true)
    }

    val uiState = preferenceRepository.userPreferences.map {
        _initialized = true
        MainUiState(
            darkMode = it.isDarkMode,
            loggedIn = it.accountId != AccountProfile.NO_ACCOUNT
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    fun onToggleDarkMode(darkMode: Boolean) = viewModelScope.launch {
        preferenceRepository.updateDarkMode(darkMode)
    }

    fun initializeApp() = viewModelScope.launch(ioDispatcher) {
        val file = File(context.cacheDir, "image_cache")
        if (file.exists() && file.isDirectory) {
            file.deleteRecursively()
        }
    }
}