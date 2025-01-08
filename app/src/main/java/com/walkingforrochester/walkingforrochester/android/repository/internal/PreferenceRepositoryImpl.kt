package com.walkingforrochester.walkingforrochester.android.repository.internal

import android.content.Context
import android.content.SharedPreferences
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

    override suspend fun removeAccountInfo(): Boolean = withContext(ioDispatcher) {
        sharedPreferences.edit()
            .remove(context.getString(R.string.wfr_account_id))
            .remove(context.getString(R.string.wfr_dark_mode_enabled))
            .commit()
    }
}