package com.walkingforrochester.walkingforrochester.android.ui.composable.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRButton
import com.walkingforrochester.walkingforrochester.android.ui.state.ProfileScreenEvent
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import com.walkingforrochester.walkingforrochester.android.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onLogoutComplete: () -> Unit
) {
    LaunchedEffect(Unit) {
        profileViewModel.eventFlow.collect { event ->
            when (event) {
                ProfileScreenEvent.Logout -> onLogoutComplete()
            }
        }
    }

    val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier)
        ProfileCard(
            modifier = Modifier.padding(horizontal = 8.dp),
            uiState = uiState,
            profileViewModel = profileViewModel
        )
        WFRButton(label = R.string.logout, onClick = profileViewModel::onLogout)
        Spacer(Modifier)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    WalkingForRochesterTheme {
        ProfileScreen(onLogoutComplete = {})
    }
}