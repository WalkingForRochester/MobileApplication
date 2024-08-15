package com.walkingforrochester.walkingforrochester.android.ui.composable.login

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facebook.login.LoginManager
import com.walkingforrochester.walkingforrochester.android.LocalFacebookCallbackManager
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.network.FacebookLoginCallback
import com.walkingforrochester.walkingforrochester.android.network.GoogleCredentialUtil
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.LoadingOverlay
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRButton
import com.walkingforrochester.walkingforrochester.android.ui.state.LoginScreenEvent
import com.walkingforrochester.walkingforrochester.android.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel = hiltViewModel(),
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
    onRegisterPrefill: (String?, String?, String?, String?) -> Unit,
    onLoginComplete: () -> Unit
) {
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()
    val callbackManager = LocalFacebookCallbackManager.current

    LaunchedEffect(Unit) {
        LoginManager.getInstance().registerCallback(
            callbackManager, FacebookLoginCallback(loginViewModel::continueWithFacebook)
        )
        loginViewModel.eventFlow.collect { event ->
            when (event) {
                LoginScreenEvent.LoginComplete -> {
                    LoginManager.getInstance().unregisterCallback(callbackManager)
                    onLoginComplete()
                }

                LoginScreenEvent.NeedsRegistration -> with(uiState.registrationScreenState) {
                    LoginManager.getInstance().unregisterCallback(callbackManager)
                    onRegisterPrefill(email, firstName, lastName, facebookId ?: "")
                }
            }
        }
    }

    val context = LocalContext.current
    val activity = LocalActivityResultRegistryOwner.current

    val coroutineScope = rememberCoroutineScope()

    LoadingOverlay(uiState.socialLoading)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        SocialLoginButtons(
            onContinueWithGoogle = {
                coroutineScope.launch {
                    GoogleCredentialUtil.performSignIn(
                        context,
                        loginViewModel::continueWithGoogle
                    )
                }
            },
            onContinueWithFacebook = {
                activity?.let {
                    LoginManager.getInstance().logInWithReadPermissions(
                        it, callbackManager, listOf("email", "public_profile")
                    )
                }
            },
            modifier = Modifier.padding(top = 20.dp),
        )
        Text(
            modifier = Modifier.padding(16.dp),
            text = stringResource(R.string.or),
            color = Color.White,
        )
        LoginForm(
            loginScreenState = uiState,
            onEmailAddressValueChange = { newEmailAddress ->
                loginViewModel.onEmailAddressValueChange(newEmailAddress)
            },
            onPasswordValueChange = { newPassword ->
                loginViewModel.onPasswordValueChange(newPassword)
            },
            onPasswordVisibilityChange = { loginViewModel.onTogglePasswordVisibility() }
        )
        Spacer(modifier = Modifier.height(24.dp))
        WFRButton(
            onClick = loginViewModel::onLoginClicked,
            label = R.string.sign_in,
            testTag = "login_button",
            buttonColor = Color.Black,
            labelColor = Color.White,
            loading = uiState.loading
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(
                onClick = { onForgotPassword() },
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    text = stringResource(R.string.forgot_password_question),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.White
                )
            }
            TextButton(
                onClick = { onRegister() },
                Modifier.height(48.dp)
            ) {
                Text(
                    text = stringResource(R.string.new_here_sign_up),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    //LoginScreen(loginViewModel = LoginViewModel(RestApi.retrofitService, Dispatchers.Default))
}