package com.walkingforrochester.walkingforrochester.android.ui.composable.navigation

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
import com.walkingforrochester.walkingforrochester.android.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    currentScreen: Destination,
    onNavigationButtonClick: () -> Unit,
    onBackButtonClick: () -> Unit,
    onProfileButtonClick: () -> Unit
) {
    if (currentScreen.showTopBar) {
        CenterAlignedTopAppBar(
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
            title = {
                Text(text = stringResource(id = currentScreen.title))
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
            modifier = modifier,
        )
    }
}

//@Preview
//@Composable
//fun PreviewTopBar() {
//    WalkingForRochesterTheme {
//        TopBar(
//            title = { Text("Title") }
//        )
//    }
//}