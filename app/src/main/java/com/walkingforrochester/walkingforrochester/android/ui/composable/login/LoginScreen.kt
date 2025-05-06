package com.walkingforrochester.walkingforrochester.android.ui.composable.login

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.foundation.Image
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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalAutofillManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.LocalSnackbarHostState
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRButton
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRButtonDefaults
import com.walkingforrochester.walkingforrochester.android.ui.state.LoginScreenEvent
import com.walkingforrochester.walkingforrochester.android.ui.state.LoginScreenState
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import com.walkingforrochester.walkingforrochester.android.viewmodel.LoginViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onForgotPassword: () -> Unit = {},
    onRegister: (
        email: String?,
        firstName: String?,
        lastName: String?,
        facebookId: String?
    ) -> Unit = { _, _, _, _ -> },
    onLoginComplete: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()
    val callbackManager = LocalFacebookCallbackManager.current

    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(key1 = callbackManager) {
        LoginManager.getInstance().registerCallback(
            callbackManager,
            FacebookLoginCallback { result -> loginViewModel.continueWithFacebook(result) }
        )
        onDispose {
            LoginManager.getInstance().unregisterCallback(callbackManager)
        }
    }

    val autofillManager = LocalAutofillManager.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = LocalSnackbarHostState.current
    val activityContext = LocalActivity.current ?: context

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            loginViewModel.eventFlow.collect { event ->
                when (event) {
                    LoginScreenEvent.LoginComplete -> {
                        LoginManager.getInstance().unregisterCallback(callbackManager)
                        // Commit the autofill session, as nothing in theory should happen
                        autofillManager?.commit()
                        onLoginComplete()
                    }

                    LoginScreenEvent.LoginCompleteManual -> {
                        LoginManager.getInstance().unregisterCallback(callbackManager)

                        PasswordCredentialUtil.savePasswordCredential(
                            activityContext = activityContext,
                            email = uiState.emailAddress,
                            password = uiState.password
                        )

                        // Cancel autofill here as we already prompted saving the credential
                        // and no need to do it twice
                        autofillManager?.cancel()

                        onLoginComplete()
                    }

                    LoginScreenEvent.NeedsRegistration -> with(uiState) {
                        LoginManager.getInstance().unregisterCallback(callbackManager)
                        onRegister(emailAddress, firstName, lastName, facebookId)
                    }

                    LoginScreenEvent.UnexpectedError -> {
                        snackbarHostState.showSnackbar(context.getString(R.string.unexpected_error))
                    }
                }
            }
        }
    }

    LaunchedEffect(activityContext, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
            // small delay before showing password manager
            delay(250L)
            PasswordCredentialUtil.performPasswordSignIn(
                activityContext = activityContext,
                performLogin = { newEmail, newPassword ->
                    loginViewModel.onCredentialLogin(
                        newEmail,
                        newPassword
                    )
                }
            )
        }
    }

    val activityResultRegistryOwner = LocalActivityResultRegistryOwner.current

    LoginScreenContent(
        uiState = uiState,
        modifier = modifier,
        contentPadding = contentPadding,
        onForgotPassword = onForgotPassword,
        onRegister = { onRegister(null, null, null, null) },
        onContinueWithGoogle = {
            coroutineScope.launch {
                GoogleCredentialUtil.performSignIn(
                    activityContext = activityContext,
                    processCredential = loginViewModel::continueWithGoogle
                )
            }
        },
        onContinueWithFacebook = {
            activityResultRegistryOwner?.let {
                LoginManager.getInstance().logInWithReadPermissions(
                    it, callbackManager, listOf("email", "public_profile")
                )
            }
        },
        onEmailChanged = { loginViewModel.onEmailAddressValueChange(it) },
        onPasswordChanged = { loginViewModel.onPasswordValueChange(it) },
        onLoginClicked = { loginViewModel.onLoginClicked() }
    )
}

@Composable
fun LoginScreenContent(
    uiState: LoginScreenState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    onForgotPassword: () -> Unit = {},
    onRegister: () -> Unit = {},
    onContinueWithGoogle: () -> Unit = {},
    onContinueWithFacebook: () -> Unit = {},
    onEmailChanged: (String) -> Unit = {},
    onPasswordChanged: (String) -> Unit = {},
    onLoginClicked: () -> Unit = {}
) {
    // Manually add background here
    Image(
        modifier = Modifier.fillMaxSize(),
        painter = painterResource(R.drawable.rainbowbg),
        contentDescription = "background_image",
        contentScale = ContentScale.Crop
    )

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
            onContinueWithGoogle = onContinueWithGoogle,
            onContinueWithFacebook = onContinueWithFacebook,
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
                onEmailChanged(newEmailAddress)
                autofillEmail = autofillPerformed
            },
            onPasswordValueChange = { newPassword, autofillPerformed ->
                onPasswordChanged(newPassword)
                autofillPassword = autofillPerformed
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
        WFRButton(
            onClick = {
                onLoginClicked()
            },
            label = R.string.sign_in,
            testTag = "login_button",
            loading = uiState.loading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            contentPadding = WFRButtonDefaults.wideContentPadding
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(
                onClick = onForgotPassword,
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    text = stringResource(R.string.forgot_password_question),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.White
                )
            }
            TextButton(
                onClick = onRegister,
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

@Preview
@Composable
fun PreviewLoginScreen() {
    WalkingForRochesterTheme {
        LoginScreenContent(uiState = LoginScreenState())
    }
}