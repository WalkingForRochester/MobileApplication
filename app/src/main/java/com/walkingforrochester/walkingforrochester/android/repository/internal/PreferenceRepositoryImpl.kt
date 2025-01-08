package com.walkingforrochester.walkingforrochester.android.repository.internal

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.di.IODispatcher
import com.walkingforrochester.walkingforrochester.android.model.AccountProfile
import com.walkingforrochester.walkingforrochester.android.repository.PreferenceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferences: SharedPreferences,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : PreferenceRepository {

    override suspend fun fetchAccountId(): Long = withContext(ioDispatcher) {
        sharedPreferences.getLong(
            context.getString(R.string.wfr_account_id),
            AccountProfile.NO_ACCOUNT
        )
    }

    override suspend fun isDarkModeEnabled(): Boolean = withContext(ioDispatcher) {
        sharedPreferences.getBoolean(
            context.getString(R.string.wfr_dark_mode_enabled),
            false
        )
    }

    override suspend fun updateDarkMode(enabled: Boolean) = withContext(ioDispatcher) {
        sharedPreferences.edit(commit = true) {
            putBoolean(context.getString(R.string.wfr_dark_mode_enabled), enabled)
        }
    }

    override suspend fun removeAccountInfo() = withContext(ioDispatcher) {
        sharedPreferences.edit(commit = true) {
            remove(context.getString(R.string.wfr_account_id))
            remove(context.getString(R.string.wfr_dark_mode_enabled))
        }
    }
}