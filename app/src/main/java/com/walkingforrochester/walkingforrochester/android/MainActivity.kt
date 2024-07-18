package com.walkingforrochester.walkingforrochester.android

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.facebook.CallbackManager
import com.walkingforrochester.walkingforrochester.android.service.ForegroundLocationService
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WalkingForRochesterAppScreen
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

val LocalFacebookCallbackManager =
    staticCompositionLocalOf<CallbackManager> { error("No CallbackManager provided") }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var callbackManager = CallbackManager.Factory.create()
    private val foregroundOnlyServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ForegroundLocationService.LocalBinder
            foregroundLocationService = binder.service
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundLocationService = null
        }
    }

    var foregroundLocationService: ForegroundLocationService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate()")
        installSplashScreen()
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, myBackPressCallback)

        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(
                LocalFacebookCallbackManager provides callbackManager,
            ) {
                WalkingForRochesterAppScreen(
                    onStartWalking = { foregroundLocationService?.subscribeToLocationUpdates() },
                    onStopWalking = { foregroundLocationService?.unsubscribeToLocationUpdates() },
                )
            }
        }

        // TODO this should be onStart/Stop, but due to service bug, must be create/destroy
        val serviceIntent = Intent(this, ForegroundLocationService::class.java)
        bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)

        processIntent()
    }

    override fun onDestroy() {
        Timber.d("onDestroy()")
        if (isFinishing) {
            Timber.d("activity finishing")
            foregroundLocationService?.unsubscribeToLocationUpdates()
        }

        foregroundLocationService = null
        unbindService(foregroundOnlyServiceConnection)
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        Timber.d("onNewIntent()")
        super.onNewIntent(intent)
        this.intent = intent

        processIntent()
    }

    private fun processIntent() {
        val stopWalking =
            intent.getBooleanExtra(ForegroundLocationService.EXTRA_STOP_WALKING, false)
        if (stopWalking) {
            foregroundLocationService?.stopFromIntent()
        }
    }

    private val myBackPressCallback = object : OnBackPressedCallback(
        enabled = true
    ) {
        override fun handleOnBackPressed() {
            moveTaskToBack(false)
        }
    }
}
