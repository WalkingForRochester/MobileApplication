package com.walkingforrochester.walkingforrochester.android

import android.app.Application
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class WalkingForRochesterApp : Application(), OnMapsSdkInitializedCallback {

    override fun onCreate() {
        super.onCreate()

        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        when (renderer) {
            MapsInitializer.Renderer.LEGACY -> Timber.d("The latest version of the renderer is used.")
            MapsInitializer.Renderer.LATEST -> Timber.d("The legacy version of the renderer is used.")
        }
    }


}