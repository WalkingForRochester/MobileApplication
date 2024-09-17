package com.walkingforrochester.walkingforrochester.android.ui.composable.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    onProfileButtonClick: () -> Unit = {}
) {
    if (currentScreen.showTopBar) {
        CenterAlignedTopAppBar(
            title = {
                Text(text = stringResource(id = currentScreen.title))
            },
            modifier = modifier,
            navigationIcon = {
                IconButton(onClick = { if (currentScreen.showBackButton) onBackButtonClick() else onNavigationButtonClick() }) {
                    Icon(
                        imageVector = if (currentScreen.showBackButton) Icons.AutoMirrored.Filled.ArrowBack else Icons.Filled.Menu,
                        contentDescription = if (currentScreen.showBackButton) stringResource(R.string.back_button) else stringResource(
                            R.string.open_navigation_drawer
                        )
                    )
                }
            },
            actions = {
                if (currentScreen.showProfileButton) {
                    IconButton(onClick = onProfileButtonClick) {
                        Icon(
                            Icons.Filled.AccountCircle,
                            contentDescription = stringResource(R.string.open_profile_screen)
                        )
                    }
                }
            },
            windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
        )
    }
}

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