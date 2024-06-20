package com.walkingforrochester.walkingforrochester.android.ui.composable.login

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facebook.login.LoginManager
import com.walkingforrochester.walkingforrochester.android.LocalFacebookCallbackManager
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.network.FacebookLoginCallback
import com.walkingforrochester.walkingforrochester.android.network.GoogleApiContract
import com.walkingforrochester.walkingforrochester.android.network.googleLoginCallback
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

    val authResultLauncher =
        rememberLauncherForActivityResult(contract = GoogleApiContract()) { task ->
            googleLoginCallback(context, task, loginViewModel::continueWithGoogle)
        }


    LoadingOverlay(uiState.socialLoading)

    ConstraintLayout(
        modifier = modifier.fillMaxHeight()
    ) {
        val (spacerTop, socialButtons, orText, loginForm, loginButton, textButtons, spacerBottom) = createRefs()
        Spacer(modifier = Modifier.constrainAs(spacerTop) {
            top.linkTo(parent.top, margin = 20.dp)
            bottom.linkTo(socialButtons.top)
            height = Dimension.fillToConstraints
        })
        SocialLoginButtons(
            modifier = Modifier.constrainAs(socialButtons) {
                top.linkTo(spacerTop.bottom)
                bottom.linkTo(orText.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            onContinueWithGoogle = { authResultLauncher.launch(null) },
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
                    top.linkTo(socialButtons.bottom)
                    bottom.linkTo(loginForm.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            text = stringResource(R.string.or),
            color = Color.White,
        )
        LoginForm(
            modifier = Modifier.constrainAs(loginForm) {
                top.linkTo(orText.bottom)
                bottom.linkTo(loginButton.top)
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
            onClick = loginViewModel::onLoginClicked,
            label = R.string.sign_in,
            modifier = Modifier
                .constrainAs(loginButton) {
                    top.linkTo(loginForm.bottom, margin = 24.dp)
                    bottom.linkTo(textButtons.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            testTag = "login:button",
            buttonColor = Color.Black,
            labelColor = Color.White,

            loading = uiState.loading
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(textButtons) {
                    top.linkTo(loginButton.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(spacerBottom.top)
                },
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = { onForgotPassword() },
                modifier = Modifier.height(48.dp)) {
                Text(
                    text = stringResource(R.string.forgot_password_question),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.White
                )
            }
            TextButton(onClick = { onRegister() },
                Modifier.height(48.dp)) {
                Text(text = stringResource(R.string.new_here_sign_up),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.White)
            }
        }
        Spacer(modifier = Modifier.constrainAs(spacerBottom) {
            top.linkTo(textButtons.bottom, margin = 20.dp)
            bottom.linkTo(parent.bottom)
            height = Dimension.fillToConstraints
        })
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    //LoginScreen(loginViewModel = LoginViewModel(RestApi.retrofitService, Dispatchers.Default))
}