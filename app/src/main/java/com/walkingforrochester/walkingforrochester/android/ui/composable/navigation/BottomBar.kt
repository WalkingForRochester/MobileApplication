package com.walkingforrochester.walkingforrochester.android.ui.composable.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    menuItems: List<Destination>,
    currentScreen: Destination,
    onScreenSelected: (Destination) -> Unit,
) {
    if (currentScreen.showBottomBar) {
        NavigationBar(modifier = modifier) {
            menuItems.forEach {
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = it.icon!!),
                            contentDescription = it.title
                        )
                    },
                    label = { Text(it.title) },
                    selected = currentScreen == it,
                    onClick = { onScreenSelected(it) }
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewBottomBar() {
    WalkingForRochesterTheme {
        //BottomBar()
    }
}