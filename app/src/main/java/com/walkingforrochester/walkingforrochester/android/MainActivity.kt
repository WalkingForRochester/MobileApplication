package com.walkingforrochester.walkingforrochester.android

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facebook.CallbackManager
import com.walkingforrochester.walkingforrochester.android.service.ForegroundLocationService
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.ConnectionState
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.NoConnectionOverlay
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WalkingForRochesterAppScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.connectivityState
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import com.walkingforrochester.walkingforrochester.android.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
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

    private var foregroundLocationService: ForegroundLocationService? = null
    private lateinit var customTabsManager: CustomTabsManager
    private var keepSplashScreenOn = true

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate()")
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { keepSplashScreenOn }

        onBackPressedDispatcher.addCallback(this, myBackPressCallback)

        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()

            LaunchedEffect(mainViewModel) {
                // Wait until state is available to before removing splash screen
                mainViewModel.uiState.first()
                keepSplashScreenOn = false
            }

            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
            val darkMode = uiState.darkMode

            WalkingForRochesterTheme(darkTheme = darkMode) {
                Surface {
                    val connection by connectivityState()
                    if (connection == ConnectionState.Unavailable) {
                        NoConnectionOverlay()
                    }

                    CompositionLocalProvider(
                        LocalFacebookCallbackManager provides callbackManager,
                    ) {
                        val context = LocalContext.current
                        val toolbarColor = MaterialTheme.colorScheme.surface.toArgb()
                        val dividerColor = DividerDefaults.color.toArgb()

                        CompositionLocalProvider(
                            value = LocalUriHandler provides customTabsManager.createUriHandler(
                                context = context,
                                isDarkMode = darkMode,
                                toolbarColor = toolbarColor,
                                navigationBarDividerColor = dividerColor
                            )
                        ) {
                            WalkingForRochesterAppScreen(
                                onStartWalking = { foregroundLocationService?.subscribeToLocationUpdates() },
                                onStopWalking = { foregroundLocationService?.unsubscribeToLocationUpdates() },
                                onToggleDarkMode = { mainViewModel.onToggleDarkMode(it) },
                                uiState = uiState
                            )
                        }
                    }
                }
            }
        }

        customTabsManager = CustomTabsManager(application, lifecycle)
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
