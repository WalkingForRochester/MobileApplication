package com.walkingforrochester.walkingforrochester.android.ui.composable.navigation

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun BottomBar(
    menuItems: List<Destination>,
    currentScreen: Destination,
    modifier: Modifier = Modifier,
    onScreenSelected: (Destination) -> Unit = {},
) {
    AnimatedVisibility(
        currentScreen.showBottomBar,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
    ) {
        NavigationBar(modifier = modifier) {
            menuItems.forEach {
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = it.icon ?: Icons.Filled.Info,
                            contentDescription = null
                        )
                    },
                    label = { Text(text = stringResource(id = it.title)) },
                    selected = currentScreen == it,
                    onClick = { onScreenSelected(it) }
                )
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewBottomBar() {
    WalkingForRochesterTheme {
        BottomBar(
            menuItems = bottomBarDestinations,
            currentScreen = LogAWalk
        )
    }
}