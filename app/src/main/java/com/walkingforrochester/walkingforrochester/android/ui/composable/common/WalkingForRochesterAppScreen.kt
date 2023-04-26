package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.composable.logawalk.AddressSearchField
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.BottomBar
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.Destination
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.NavigationDrawer
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.NavigationHost
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.TopBar
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.bottomBarDestinations
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.drawerDestinations
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.navigateSingleTopTo
import com.walkingforrochester.walkingforrochester.android.ui.state.MainUiState
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import com.walkingforrochester.walkingforrochester.android.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun WalkingForRochesterAppScreen(
    onStartWalking: () -> Unit,
    onStopWalking: () -> Unit,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val wfrAccountId by longPreferenceState(
        key = stringResource(R.string.wfr_account_id),
        defaultValue = 0L
    )

    WalkingForRochesterTheme(darkTheme = uiState.darkMode) {
        val connection by connectivityState()
        val notConnected = connection === ConnectionState.Unavailable
        if (notConnected) {
            NoConnectionOverlay()
        }
        WFRNavigationDrawer(
            uiState = uiState,
            onToggleDarkMode = mainViewModel::onToggleDarkMode,
            onStartWalking = onStartWalking,
            onStopWalking = onStopWalking,
            isLoggedIn = wfrAccountId != 0L
        )
    }
}

@Composable
fun WFRNavigationDrawer(
    uiState: MainUiState,
    onToggleDarkMode: (Boolean) -> Unit,
    onStartWalking: () -> Unit,
    onStopWalking: () -> Unit,
    isLoggedIn: Boolean
) {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination
    val currentScreen = Destination.values()
        .find { it.route == currentDestination?.route || it.routeWithArgs == currentDestination?.route }
        ?: Destination.Login
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    NavigationDrawer(
        drawerState = drawerState,
        uiState = uiState,
        menuItems = drawerDestinations,
        currentScreen = currentScreen,
        onScreenSelected = { screen ->
            navController.navigateSingleTopTo(screen.route)
            scope.launch { drawerState.close() }
        },
        onBackPressed = {
            scope.launch { drawerState.close() }
        },
        onToggleDarkMode = onToggleDarkMode
    ) {
        AppScreen(backgroundImage = if (Destination.Login == currentScreen) R.drawable.rainbowbg else R.drawable.background,
            topBar = {
                TopBar(currentScreen = currentScreen,
                    onBackButtonClick = { navController.popBackStack() },
                    onNavigationButtonClick = { scope.launch { drawerState.open() } },
                    onProfileButtonClick = { navController.navigateSingleTopTo(Destination.Profile.route) },
                    titleComposable = {
                        if (Destination.LogAWalk == currentScreen) {
                            ProvideTextStyle(value = MaterialTheme.typography.bodyLarge) {
                                AddressSearchField(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                )
                            }
                        }
                    }, isLoggedIn = isLoggedIn
                )
            },
            bottomBar = {
                BottomBar(menuItems = bottomBarDestinations,
                    currentScreen = currentScreen,
                    onScreenSelected = { screen ->
                        navController.navigateSingleTopTo(screen.route)
                    })
            }) { innerPadding ->
            NavigationHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                loggedIn = isLoggedIn,
                onStartWalking = onStartWalking,
                onStopWalking = onStopWalking
            )
        }
    }
}

val LocalSnackbarHostState =
    staticCompositionLocalOf<SnackbarHostState> { error("No SnackbarHostState provided") }

@Composable
fun AppScreen(
    modifier: Modifier = Modifier,
    @DrawableRes backgroundImage: Int? = null,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.Center,
    content: @Composable (PaddingValues) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        Scaffold(
            topBar = topBar,
            bottomBar = bottomBar,
            modifier = modifier,
            floatingActionButton = floatingActionButton,
            floatingActionButtonPosition = floatingActionButtonPosition,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            if (backgroundImage != null) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(backgroundImage),
                    contentDescription = "background_image",
                    contentScale = ContentScale.Crop
                )
            }
            content(paddingValues)
        }
    }
}

suspend fun SnackbarHostState.showLongCloseableSnackbar(message: String) {
    this.showSnackbar(message = message, withDismissAction = true, duration = SnackbarDuration.Long)
}