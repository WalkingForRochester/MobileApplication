package com.walkingforrochester.walkingforrochester.android.ui.composable.registration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRButton
import com.walkingforrochester.walkingforrochester.android.ui.state.RegistrationScreenEvent
import com.walkingforrochester.walkingforrochester.android.viewmodel.RegistrationViewModel

@Composable
fun RegistrationScreen(
    modifier: Modifier = Modifier,
    email: String? = null,
    firstName: String? = null,
    lastName: String? = null,
    registrationViewModel: RegistrationViewModel = hiltViewModel(),
    onRegistrationComplete: () -> Unit
) {
    LaunchedEffect(Unit) {
        registrationViewModel.prefill(email, firstName, lastName)
        registrationViewModel.eventFlow.collect { event ->
            when (event) {
                RegistrationScreenEvent.RegistrationComplete -> onRegistrationComplete()
            }
        }
    }

    val uiState by registrationViewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier)
        RegistrationForm(registrationViewModel = registrationViewModel, uiState = uiState)
        WFRButton(
            label = R.string.sign_up,
            buttonColor = Color.Black,
            labelColor = Color.White,
            onClick = registrationViewModel::onSignUp,
            loading = uiState.loading
        )
        Spacer(Modifier)
    }
}

