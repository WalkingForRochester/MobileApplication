package com.walkingforrochester.walkingforrochester.android.di

import com.walkingforrochester.walkingforrochester.android.repository.NetworkRepository
import com.walkingforrochester.walkingforrochester.android.repository.PreferenceRepository
import com.walkingforrochester.walkingforrochester.android.repository.WalkRepository
import com.walkingforrochester.walkingforrochester.android.repository.internal.NetworkRepositoryImpl
import com.walkingforrochester.walkingforrochester.android.repository.internal.PreferenceRepositoryImpl
import com.walkingforrochester.walkingforrochester.android.repository.internal.WalkRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Singleton
    @Binds
    fun bindNetworkRepository(networkRepositoryImpl: NetworkRepositoryImpl): NetworkRepository

    @Singleton
    @Binds
    fun bindPreferenceRepository(preferenceRepositoryImpl: PreferenceRepositoryImpl): PreferenceRepository

    @Singleton
    @Binds
    fun bindWalkRepository(walkRepositoryImpl: WalkRepositoryImpl): WalkRepository
}