package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.BottomBar
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.Destinations
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.LoginDestination
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.NavigationDrawer
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.NavigationHost
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.ProfileDestination
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.TopBar
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.bottomBarDestinations
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.drawerDestinations
import com.walkingforrochester.walkingforrochester.android.ui.composable.navigation.navigateSingleTopTo
import com.walkingforrochester.walkingforrochester.android.ui.state.MainUiState
import kotlinx.coroutines.launch

@Composable
fun WalkingForRochesterAppScreen(
    onStartWalking: () -> Unit,
    onStopWalking: () -> Unit,
    onToggleDarkMode: (Boolean) -> Unit = {},
    uiState: MainUiState = MainUiState()
) {
    WFRNavigationDrawer(
        uiState = uiState,
        onToggleDarkMode = onToggleDarkMode,
        onStartWalking = onStartWalking,
        onStopWalking = onStopWalking,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WFRNavigationDrawer(
    uiState: MainUiState,
    onToggleDarkMode: (Boolean) -> Unit,
    onStartWalking: () -> Unit,
    onStopWalking: () -> Unit
) {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination
    val currentScreen = Destinations
        .find { it.route == currentDestination?.route || it.routeWithArgs == currentDestination?.route }
        ?: LoginDestination
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    NavigationDrawer(
        drawerState = drawerState,
        uiState = uiState,
        menuItems = drawerDestinations,
        currentScreen = currentScreen,
        onScreenSelected = { screen ->
            navController.navigateSingleTopTo(screen.route)
        },
        onCloseDrawer = {
            scope.launch { drawerState.close() }
        },
        onToggleDarkMode = onToggleDarkMode
    ) {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        AppScreen(
            backgroundImage = if (LoginDestination == currentScreen) R.drawable.rainbowbg else null,
            topBar = {
                TopBar(
                    currentScreen = currentScreen,
                    onBackButtonClick = { navController.popBackStack() },
                    onNavigationButtonClick = { scope.launch { drawerState.open() } },
                    onProfileButtonClick = { navController.navigateSingleTopTo(ProfileDestination.route) },
                    scrollBehavior = scrollBehavior
                )
            },
            bottomBar = {
                BottomBar(menuItems = bottomBarDestinations,
                    currentScreen = currentScreen,
                    onScreenSelected = { screen ->
                        navController.navigateSingleTopTo(screen.route)
                    })
            }) { contentPadding ->
            NavigationHost(
                navController = navController,
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                loggedIn = uiState.loggedIn,
                onStartWalking = onStartWalking,
                onStopWalking = onStopWalking,
                contentPadding = contentPadding
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
            modifier = modifier.imePadding(),
            topBar = topBar,
            bottomBar = bottomBar,
            floatingActionButton = floatingActionButton,
            floatingActionButtonPosition = floatingActionButtonPosition,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            contentWindowInsets = WindowInsets.systemBars.union(
                WindowInsets.displayCutout.only(
                    WindowInsetsSides.Horizontal
                )
            )
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