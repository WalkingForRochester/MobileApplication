package com.walkingforrochester.walkingforrochester.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walkingforrochester.walkingforrochester.android.model.AccountProfile
import com.walkingforrochester.walkingforrochester.android.repository.PreferenceRepository
import com.walkingforrochester.walkingforrochester.android.ui.state.MainUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
) : ViewModel() {

    private val _uiState: MutableStateFlow<MainUiState>
    val uiState: StateFlow<MainUiState>

    init {
        val state = runBlocking {
            MainUiState(
                darkMode = preferenceRepository.isDarkModeEnabled(),
                loggedIn = preferenceRepository.fetchAccountId() != AccountProfile.NO_ACCOUNT
            )
        }
        _uiState = MutableStateFlow(state)
        uiState = _uiState.asStateFlow()
    }

    fun onToggleDarkMode(darkMode: Boolean) = viewModelScope.launch {
        _uiState.update { it.copy(darkMode = darkMode) }

        preferenceRepository.updateDarkMode(darkMode)
    }
}