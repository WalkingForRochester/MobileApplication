package com.walkingforrochester.walkingforrochester.android.ui.composable.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    modifier: Modifier = Modifier,
    enableDrawerGestures: Boolean = false,
    darkMode: Boolean = false,
    menuItems: List<Destination> = drawerDestinations,
    onScreenSelected: (Destination) -> Unit = {},
    onToggleDarkMode: (Boolean) -> Unit = {},
    onCloseDrawer: () -> Unit = {},
    content: @Composable () -> Unit
) {
    if (drawerState.isOpen) {
        BackHandler {
            onCloseDrawer()
        }
    }
    val gesturesEnabled = enableDrawerGestures || drawerState.isOpen
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                menuItems = menuItems,
                darkMode = darkMode,
                onScreenSelected = onScreenSelected,
                onToggleDarkMode = onToggleDarkMode,
                onCloseDrawer = onCloseDrawer
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
    darkMode: Boolean,
    onScreenSelected: (Destination) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onCloseDrawer: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val resources = LocalResources.current

    ModalDrawerSheet(modifier = modifier) {
        Text(
            text = "Walking For Rochester",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
        )
        Spacer(Modifier.height(12.dp))
        Column(
            Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column {

                menuItems.forEach {
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = stringResource(id = it.title),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        },
                        selected = false,
                        onClick = {
                            if (it.uriTarget == 0) {
                                onScreenSelected(it)
                            } else {
                                val uri = resources.getString(it.uriTarget)
                                uriHandler.openUri(uri = uri)
                            }
                            onCloseDrawer()
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        icon = {
                            if (it.iconResId != null) {
                                Icon(
                                    painter = painterResource(it.iconResId),
                                    contentDescription = null
                                )
                            }
                        },
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp, start = 28.dp, end = 28.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.dark_mode))
                Switch(checked = darkMode, onCheckedChange = onToggleDarkMode)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewNavDrawer() {
    WalkingForRochesterTheme {
        NavigationDrawer(
            enableDrawerGestures = false,
            menuItems = drawerDestinations,
            drawerState = rememberDrawerState(DrawerValue.Open)
        ) {
            Text("Hello")
        }
    }
}