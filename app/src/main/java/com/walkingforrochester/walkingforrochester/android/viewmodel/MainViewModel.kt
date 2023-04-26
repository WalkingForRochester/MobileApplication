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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                darkMode = sharedPreferences.getBoolean(
                    context.getString(R.string.wfr_dark_mode_enabled),
                    false
                )
            )
        }
    }

    fun onToggleDarkMode(darkMode: Boolean) = flow<Nothing> {
        _uiState.update { it.copy(darkMode = darkMode) }

        sharedPreferences.edit()
            .putBoolean(context.getString(R.string.wfr_dark_mode_enabled), darkMode).apply()
    }.catch {
        Timber.e(it, "Unable to toggle dark mode")
        showUnexpectedErrorToast(context)
    }.launchIn(viewModelScope)

}