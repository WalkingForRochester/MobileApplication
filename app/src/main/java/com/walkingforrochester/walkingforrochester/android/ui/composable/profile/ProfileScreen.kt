package com.walkingforrochester.walkingforrochester.android.ui.composable.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.network.GoogleCredentialUtil
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.LocalSnackbarHostState
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRButton
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFROutlinedButton
import com.walkingforrochester.walkingforrochester.android.ui.state.ProfileScreenEvent
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import com.walkingforrochester.walkingforrochester.android.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onLogoutComplete: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val snackbarHostState = LocalSnackbarHostState.current
    LaunchedEffect(Unit) {
        profileViewModel.eventFlow.collect { event ->
            when (event) {
                ProfileScreenEvent.Logout -> {
                    GoogleCredentialUtil.performSignOut(context = context)
                    onLogoutComplete()
                }

                ProfileScreenEvent.AccountDeleted -> {
                    GoogleCredentialUtil.performSignOut(context = context)
                    onLogoutComplete()
                }

                ProfileScreenEvent.UnexpectedError -> {
                    snackbarHostState.showSnackbar(context.getString(R.string.unexpected_error))
                }
            }
        }
    }

    val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    var openAlertDialog by remember { mutableStateOf(false) }

    if (openAlertDialog) {
        DeleteAccountDialog(
            hasCommunityService = uiState.communityService,
            onDismissRequest = { openAlertDialog = false },
            onConfirmed = { profileViewModel.onDeleteAccount() }
        )
    }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .imePadding()
    ) {
        Spacer(Modifier)
        ProfileCard(
            modifier = Modifier.padding(horizontal = 8.dp),
            uiState = uiState,
            profileViewModel = profileViewModel
        )
        if (uiState.editProfile) {
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            Spacer(
                modifier = Modifier.height(16.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            WFROutlinedButton(
                onClick = { openAlertDialog = true },
                label = R.string.delete_account,
                modifier = Modifier.widthIn(min = 200.dp)
            )
            Spacer(Modifier.height(12.dp))
            WFRButton(
                onClick = profileViewModel::onLogout,
                label = R.string.logout,
                modifier = Modifier.widthIn(min = 200.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    WalkingForRochesterTheme {
        ProfileScreen()
    }
}