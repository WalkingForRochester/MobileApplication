package com.walkingforrochester.walkingforrochester.android.ui.composable.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.state.MainUiState

@Composable
fun NavigationDrawer(
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    uiState: MainUiState,
    menuItems: List<Destination>,
    currentScreen: Destination,
    onScreenSelected: (Destination) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onBackPressed: () -> Unit,
    content: @Composable () -> Unit
) {
    if (drawerState.isOpen) {
        BackHandler {
            onBackPressed()
        }
    }
    val gesturesEnabled = currentScreen.enableDrawerGestures || drawerState.isOpen
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                menuItems = menuItems,
                currentScreen = currentScreen,
                darkMode = uiState.darkMode,
                onScreenSelected = onScreenSelected,
                onToggleDarkMode = onToggleDarkMode,
                onCloseDrawer = onBackPressed
            )
        },
        modifier = modifier,
        gesturesEnabled = gesturesEnabled,
    ) {
        content()
    }
}

@Composable
fun DrawerContent(
    modifier: Modifier = Modifier,
    menuItems: List<Destination>,
    currentScreen: Destination,
    darkMode: Boolean,
    onScreenSelected: (Destination) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onCloseDrawer: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val guidelinesUrl = stringResource(R.string.guidelines_url)

    ModalDrawerSheet(modifier = modifier) {
        Text(
            text = "Walking For Rochester",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        Spacer(Modifier.height(12.dp))
        Column(
            Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column {
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = stringResource(id = R.string.safety_guidelines)
                        )
                    },
                    label = { Text(stringResource(id = R.string.safety_guidelines)) },
                    selected = false,
                    onClick = {
                        uriHandler.openUri(guidelinesUrl)
                        onCloseDrawer()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                menuItems.forEach {
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                painter = painterResource(id = it.icon!!),
                                contentDescription = it.title
                            )
                        },
                        label = { Text(it.title) },
                        selected = currentScreen == it,
                        onClick = { onScreenSelected(it) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }

            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.dark_mode))
                Switch(checked = darkMode, onCheckedChange = onToggleDarkMode)
            }
        }
    }
}