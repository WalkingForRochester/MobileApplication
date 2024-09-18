package com.walkingforrochester.walkingforrochester.android.ui.composable.login

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.facebook.login.LoginManager
import com.walkingforrochester.walkingforrochester.android.LocalFacebookCallbackManager
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.network.FacebookLoginCallback
import com.walkingforrochester.walkingforrochester.android.network.GoogleCredentialUtil
import com.walkingforrochester.walkingforrochester.android.network.PasswordCredentialUtil
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.LoadingOverlay
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRButton
import com.walkingforrochester.walkingforrochester.android.ui.state.LoginScreenEvent
import com.walkingforrochester.walkingforrochester.android.viewmodel.LoginViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onForgotPassword: () -> Unit = {},
    onRegister: () -> Unit = {},
    onRegisterPrefill: (
        email: String,
        firstName: String,
        lastName: String,
        facebookId: String
    ) -> Unit = { _, _, _, _ -> },
    onLoginComplete: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()
    val callbackManager = LocalFacebookCallbackManager.current

    DisposableEffect(key1 = callbackManager) {
        LoginManager.getInstance().registerCallback(
            callbackManager,
            FacebookLoginCallback { result -> loginViewModel.continueWithFacebook(result) }
        )
        onDispose {
            LoginManager.getInstance().unregisterCallback(callbackManager)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            loginViewModel.eventFlow.collect { event ->
                when (event) {
                    LoginScreenEvent.LoginComplete -> {
                        LoginManager.getInstance().unregisterCallback(callbackManager)
                        onLoginComplete()
                    }

                    LoginScreenEvent.LoginCompleteManual -> {
                        LoginManager.getInstance().unregisterCallback(callbackManager)
                        PasswordCredentialUtil.savePasswordCredential(
                            context = context,
                            email = uiState.emailAddress,
                            password = uiState.password
                        )
                        onLoginComplete()
                    }

                    LoginScreenEvent.NeedsRegistration -> with(uiState) {
                        LoginManager.getInstance().unregisterCallback(callbackManager)
                        onRegisterPrefill(emailAddress, firstName, lastName, facebookId)
                    }
                }
            }
        }
    }

    val activity = LocalActivityResultRegistryOwner.current

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(context, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
            // small delay before showing password manager
            delay(250L)
            PasswordCredentialUtil.performPasswordSignIn(
                context = context,
                performLogin = { newEmail, newPassword ->
                    loginViewModel.onLogin(
                        newEmail,
                        newPassword
                    )
                }
            )
        }
    }

    LoadingOverlay(uiState.socialLoading)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // track autofill status to avoid prompting to save password if came from autofill
        var autofillEmail by remember { mutableStateOf(false) }
        var autofillPassword by remember { mutableStateOf(false) }

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
            onEmailAddressValueChange = { newEmailAddress, autofillPerformed ->
                loginViewModel.onEmailAddressValueChange(newEmailAddress)
                autofillEmail = autofillPerformed
            },
            onPasswordValueChange = { newPassword, autofillPerformed ->
                loginViewModel.onPasswordValueChange(newPassword)
                autofillPassword = autofillPerformed
            },
            onPasswordVisibilityChange = { loginViewModel.onTogglePasswordVisibility() }
        )
        Spacer(modifier = Modifier.height(24.dp))
        WFRButton(
            onClick = { loginViewModel.onLoginClicked(autofillData = autofillEmail && autofillPassword) },
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