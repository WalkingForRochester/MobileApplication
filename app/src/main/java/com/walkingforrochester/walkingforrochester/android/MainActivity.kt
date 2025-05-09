package com.walkingforrochester.walkingforrochester.android

import android.content.ComponentName
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.facebook.CallbackManager
import com.walkingforrochester.walkingforrochester.android.service.ForegroundLocationService
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.ConnectionState
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.NoConnectionOverlay
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WalkingForRochesterAppScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.observeConnectivityAsFlow
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

    private val foregroundLocationServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Timber.d("onServiceConnected")
        }

        override fun onNullBinding(name: ComponentName?) {
            Timber.d("onNullBinding - service bound")
            // Doing nothing as there are no controls for the service being exposed anymore
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Timber.d("onServiceDisconnected")
        }
    }

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

            var initialized by remember { mutableStateOf(false) }
            LaunchedEffect(mainViewModel) {
                // Wait until state is available to before removing splash screen
                initialized = mainViewModel.initialized.first()
                keepSplashScreenOn = false
            }

            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

            // Use launched effect so the connectivity observation isn't destroyed
            // via a recompose causing overlay to blink.
            val owner = LocalLifecycleOwner.current
            val context = LocalContext.current
            var state by remember { mutableStateOf(ConnectionState.Available) }
            LaunchedEffect(owner) {
                owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    context.observeConnectivityAsFlow().collect {
                        if (state != it) {
                            state = it
                        }
                    }
                }
            }

            if (initialized) {
                val darkMode = uiState.darkMode

                WalkingForRochesterTheme(darkTheme = darkMode) {
                    Surface {
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
                                    onToggleDarkMode = { mainViewModel.onToggleDarkMode(it) },
                                    uiState = uiState
                                )
                            }
                        }

                        if (state == ConnectionState.Unavailable) {
                            NoConnectionOverlay()
                        }
                    }
                }
            }
        }

        customTabsManager = CustomTabsManager(application, lifecycle)
    }

    override fun onStart() {
        Timber.d("onStart()")
        super.onStart()

        val serviceIntent = Intent(this, ForegroundLocationService::class.java)
        bindService(serviceIntent, foregroundLocationServiceConnection, BIND_AUTO_CREATE)
    }

    override fun onStop() {
        Timber.d("onStop()")

        unbindService(foregroundLocationServiceConnection)

        super.onStop()
    }

    private val myBackPressCallback = object : OnBackPressedCallback(
        enabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
    ) {
        override fun handleOnBackPressed() {
            moveTaskToBack(false)
        }
    }
}
