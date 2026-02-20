package com.walkingforrochester.walkingforrochester.android.ui.composable.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    currentScreen: Destination,
    modifier: Modifier = Modifier,
    onNavigationButtonClick: () -> Unit = {},
    onBackButtonClick: () -> Unit = {},
    onProfileButtonClick: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    if (currentScreen.showTopBar) {
        CenterAlignedTopAppBar(
            title = {
                Text(text = stringResource(id = currentScreen.title))
            },
            modifier = modifier,
            navigationIcon = {
                IconButton(onClick = { if (currentScreen.showBackButton) onBackButtonClick() else onNavigationButtonClick() }) {
                    val drawable = if (currentScreen.showBackButton) R.drawable.ic_arrow_back_24dp else R.drawable.ic_menu_24dp
                    val contentDescription =
                        if (currentScreen.showBackButton) R.string.back_button else R.string.open_navigation_drawer
                    Icon(
                        painter = painterResource(drawable),
                        contentDescription = stringResource(contentDescription)
                    )
                }
            },
            actions = {
                if (currentScreen.showProfileButton) {
                    IconButton(onClick = onProfileButtonClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_account_circle_24dp),
                            contentDescription = stringResource(R.string.open_profile_screen)
                        )
                    }
                }
            },
            windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
            scrollBehavior = scrollBehavior
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewTopBarBackButton() {
    WalkingForRochesterTheme {
        TopBar(
            currentScreen = Destination(
                route = "test",
                title = R.string.back_button,
                showBackButton = true,
                showProfileButton = false,
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewTopBarMenu() {
    WalkingForRochesterTheme {
        TopBar(
            currentScreen = Destination(
                route = "test",
                title = R.string.open_navigation_drawer,
                showBackButton = false,
                showProfileButton = true
            )
        )
    }
}