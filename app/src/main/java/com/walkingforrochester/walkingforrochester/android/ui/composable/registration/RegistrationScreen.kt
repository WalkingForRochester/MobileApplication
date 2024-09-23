package com.walkingforrochester.walkingforrochester.android.ui.composable.registration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.network.PasswordCredentialUtil
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.LocalSnackbarHostState
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRButton
import com.walkingforrochester.walkingforrochester.android.ui.state.RegistrationScreenEvent
import com.walkingforrochester.walkingforrochester.android.ui.state.RegistrationScreenState
import com.walkingforrochester.walkingforrochester.android.viewmodel.RegistrationViewModel

@Composable
fun RegistrationScreen(
    modifier: Modifier = Modifier,
    initState: RegistrationScreenState? = null,
    onRegistrationComplete: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    registrationViewModel: RegistrationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by registrationViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(initState, registrationViewModel) {
        initState?.let {
            registrationViewModel.prefill(it)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = LocalSnackbarHostState.current

    LaunchedEffect(lifecycleOwner, registrationViewModel) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            registrationViewModel.eventFlow.collect { event ->
                when (event) {
                    RegistrationScreenEvent.RegistrationComplete -> {
                        PasswordCredentialUtil.savePasswordCredential(
                            context = context,
                            email = uiState.email,
                            password = uiState.password
                        )
                        onRegistrationComplete()
                    }

                    RegistrationScreenEvent.UnexpectedError -> {
                        snackbarHostState.showSnackbar(context.getString(R.string.unexpected_error))
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .imePadding(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        RegistrationForm(registrationViewModel = registrationViewModel, uiState = uiState)
        Spacer(modifier = Modifier.height(24.dp))
        Spacer(modifier = Modifier.weight(1f))
        WFRButton(
            label = R.string.sign_up,
            onClick = registrationViewModel::onSignUp,
            loading = uiState.loading
        )
        Spacer(Modifier.height(24.dp))
    }
}

