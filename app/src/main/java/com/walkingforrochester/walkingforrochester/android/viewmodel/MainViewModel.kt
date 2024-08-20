package com.walkingforrochester.walkingforrochester.android.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.showUnexpectedErrorToast
import com.walkingforrochester.walkingforrochester.android.ui.state.MainUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState: MutableStateFlow<MainUiState>
    val uiState: StateFlow<MainUiState>

    init {
        val state = MainUiState(
            darkMode = sharedPreferences.getBoolean(
                context.getString(R.string.wfr_dark_mode_enabled),
                false
            ),
            loggedIn = sharedPreferences.getLong(
                context.getString(R.string.wfr_account_id),
                0L
            ) != 0L
        )

        _uiState = MutableStateFlow(state)
        uiState = _uiState.asStateFlow()
    }

    fun onToggleDarkMode(darkMode: Boolean) = viewModelScope.launch {
        _uiState.update { it.copy(darkMode = darkMode) }

        try {
            sharedPreferences.edit()
                .putBoolean(context.getString(R.string.wfr_dark_mode_enabled), darkMode).apply()
        } catch (t: Throwable) {
            Timber.e(t, "Unable to toggle dark mode")
            showUnexpectedErrorToast(context)
        }
    }
}