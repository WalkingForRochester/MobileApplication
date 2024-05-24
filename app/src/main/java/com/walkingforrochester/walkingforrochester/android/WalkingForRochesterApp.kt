package com.walkingforrochester.walkingforrochester.android

import android.app.Application
import com.google.android.gms.maps.MapsInitializer
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class WalkingForRochesterApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST) {
            when (it) {
                MapsInitializer.Renderer.LATEST -> Timber.d("The latest version of the renderer is used.")
                MapsInitializer.Renderer.LEGACY -> Timber.d("The legacy version of the renderer is used.")
            }
        }
    }
}