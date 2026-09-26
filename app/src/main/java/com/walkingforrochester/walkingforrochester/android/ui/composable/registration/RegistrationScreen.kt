package com.walkingforrochester.walkingforrochester.android.ui.composable.registration

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalAutofillManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ktx.isPhoneLandscape
import com.walkingforrochester.walkingforrochester.android.model.AccountProfile
import com.walkingforrochester.walkingforrochester.android.network.PasswordCredentialUtil
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.NavigationIcon
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRButton
import com.walkingforrochester.walkingforrochester.android.ui.state.RegistrationScreenEvent
import com.walkingforrochester.walkingforrochester.android.ui.state.RegistrationScreenState
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import com.walkingforrochester.walkingforrochester.android.viewmodel.RegistrationViewModel
import timber.log.Timber

@Composable
fun RegistrationScreen(
    modifier: Modifier = Modifier,
    profile: AccountProfile = AccountProfile.DEFAULT_PROFILE,
    onNavigateBack: () -> Unit = {},
    onRegistrationComplete: () -> Unit = {},
    registrationViewModel: RegistrationViewModel = hiltViewModel()
) {
    LaunchedEffect(profile, registrationViewModel) {
        registrationViewModel.prefill(profile)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by registrationViewModel.uiState.collectAsStateWithLifecycle()
    val registrationProfile by registrationViewModel.registrationProfile.collectAsStateWithLifecycle()
    val autofillManager = LocalAutofillManager.current
    val activityContext = LocalActivity.current
    val resources = LocalResources.current

    LaunchedEffect(
        uiState.event,
    ) {
        when (uiState.event) {
            RegistrationScreenEvent.None -> {
                Timber.d("Showing registration screen")
            }

            RegistrationScreenEvent.RegistrationComplete -> {
                activityContext?.let {
                    PasswordCredentialUtil.savePasswordCredential(
                        activityContext = it,
                        email = registrationProfile.email,
                        password = uiState.password
                    )
                }

                // Cancel autofill as we are saving via credential manager
                autofillManager?.cancel()
                onRegistrationComplete()
            }

            RegistrationScreenEvent.UnexpectedError -> {
                snackbarHostState.showSnackbar(resources.getString(R.string.unexpected_error))
                registrationViewModel.clearEvent()
            }
        }
    }

    RegistrationContent(
        uiState = uiState,
        registrationProfile = registrationProfile,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onProfileChange = { registrationViewModel.onProfileChange(it) },
        onPasswordChange = { registrationViewModel.onPasswordChange(it) },
        onPasswordConfirmationChange = { registrationViewModel.onPasswordConfirmationChange(it) },
        onSignUp = { registrationViewModel.onSignUp() },
    )
}

@Composable
fun RegistrationContent(
    uiState: RegistrationScreenState,
    registrationProfile: AccountProfile,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateBack: () -> Unit = {},
    onProfileChange: (AccountProfile) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onPasswordConfirmationChange: (String) -> Unit = {},
    onSignUp: () -> Unit = {},
) {
    val isPhoneLandscape = currentWindowAdaptiveInfoV2().windowSizeClass.isPhoneLandscape()

    @OptIn(ExperimentalMaterial3Api::class)
    val scrollBehavior =
        if (isPhoneLandscape) TopAppBarDefaults.enterAlwaysScrollBehavior()
        else TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.sign_up)) },
                navigationIcon = { NavigationIcon(onClick = onNavigateBack) },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { contentPadding ->
        @OptIn(ExperimentalMaterial3Api::class)
        Column(
            modifier = modifier
                .fillMaxHeight()
                .imePadding()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            RegistrationForm(
                uiState = uiState,
                registrationProfile = registrationProfile,
                onProfileChange = onProfileChange,
                onPasswordChange = onPasswordChange,
                onPasswordConfirmationChange = onPasswordConfirmationChange
            )
            Spacer(modifier = Modifier.height(24.dp))
            Spacer(modifier = Modifier.weight(1f))
            WFRButton(
                label = R.string.sign_up,
                onClick = onSignUp,
                loading = uiState.loading
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewRegistrationContent() {
    WalkingForRochesterTheme {
        Surface {
            RegistrationContent(
                uiState = RegistrationScreenState(),
                registrationProfile = AccountProfile.DEFAULT_PROFILE
            )
        }
    }
}
