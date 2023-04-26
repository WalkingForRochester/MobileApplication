package com.walkingforrochester.walkingforrochester.android.ui.composable.login

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facebook.login.LoginManager
import com.walkingforrochester.walkingforrochester.android.LocalFacebookCallbackManager
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.network.FacebookLoginCallback
import com.walkingforrochester.walkingforrochester.android.network.GoogleApiContract
import com.walkingforrochester.walkingforrochester.android.network.GoogleLoginCallback
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.LoadingOverlay
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRButton
import com.walkingforrochester.walkingforrochester.android.ui.state.LoginScreenEvent
import com.walkingforrochester.walkingforrochester.android.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel = hiltViewModel(),
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
    onRegisterPrefill: (String, String, String) -> Unit,
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
                    onRegisterPrefill(email, firstName, lastName)
                }
            }
        }
    }

    val context = LocalContext.current
    val activity = LocalActivityResultRegistryOwner.current

    val authResultLauncher =
        rememberLauncherForActivityResult(contract = GoogleApiContract()) { task ->
            GoogleLoginCallback(context, task, loginViewModel::continueWithGoogle)
        }


    LoadingOverlay(uiState.socialLoading)

    ConstraintLayout(
        modifier = modifier.fillMaxHeight()
    ) {
        val (socialButtons, orText, loginForm, loginButton, textButtons) = createRefs()
        SocialLoginButtons(
            modifier = Modifier.constrainAs(socialButtons) {
                bottom.linkTo(orText.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            onContinueWithGoogle = { authResultLauncher.launch(R.string.server_client_id) },
            onContinueWithFacebook = {
                LoginManager.getInstance().logInWithReadPermissions(
                    activity!!, callbackManager, listOf("email", "public_profile")
                )
            }
        )
        Text(
            modifier = Modifier
                .padding(16.dp)
                .constrainAs(orText) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            text = stringResource(R.string.or),
            color = Color.White,
        )
        LoginForm(
            modifier = Modifier.constrainAs(loginForm) {
                top.linkTo(orText.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            loginScreenState = uiState,
            onEmailAddressValueChange = { newEmailAddress ->
                loginViewModel.onEmailAddressValueChange(newEmailAddress)
            },
            onPasswordValueChange = { newPassword ->
                loginViewModel.onPasswordValueChange(newPassword)
            },
            onPasswordVisibilityChange = { loginViewModel.onTogglePasswordVisibility() }
        )
        WFRButton(
            modifier = Modifier
                .constrainAs(loginButton) {
                    top.linkTo(loginForm.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            label = R.string.sign_in,
            buttonColor = Color.Black,
            labelColor = Color.White,
            onClick = loginViewModel::onLoginClicked,
            loading = uiState.loading
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(textButtons) {
                    top.linkTo(loginButton.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = { onForgotPassword() }) {
                Text(
                    text = stringResource(R.string.forgot_password_question),
                    color = Color.White
                )
            }
            TextButton(onClick = { onRegister() }) {
                Text(text = stringResource(R.string.new_here_sign_up), color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    //LoginScreen(loginViewModel = LoginViewModel(RestApi.retrofitService, Dispatchers.Default))
}