package com.walkingforrochester.walkingforrochester.android.ui.composable.forgotpassword

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.network.PasswordCredentialUtil
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.LocalSnackbarHostState
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRButton
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRPasswordField
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRTextField
import com.walkingforrochester.walkingforrochester.android.ui.modifier.autofill
import com.walkingforrochester.walkingforrochester.android.ui.state.ForgotPasswordScreenEvent
import com.walkingforrochester.walkingforrochester.android.ui.state.ForgotPasswordScreenState
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import com.walkingforrochester.walkingforrochester.android.viewmodel.ForgotPasswordViewModel
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    onPasswordResetComplete: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    forgotPasswordViewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by forgotPasswordViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val snackbarHostState = LocalSnackbarHostState.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner, context) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            forgotPasswordViewModel.eventFlow.collect { event ->
                when (event) {
                    ForgotPasswordScreenEvent.PasswordReset -> {
                        PasswordCredentialUtil.savePasswordCredential(
                            context = context,
                            email = uiState.email,
                            password = uiState.password
                        )

                        lifecycleOwner.lifecycleScope.launch {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.password_reset_done)
                            )
                        }
                        onPasswordResetComplete()
                    }

                    ForgotPasswordScreenEvent.UnexpectedError -> {

                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.unexpected_error),
                        )
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
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        when {
            uiState.internalCode.isEmpty() -> {
                RequestCode(
                    uiState = uiState,
                    onEmailChange = { email -> forgotPasswordViewModel.onEmailChange(email) },
                    onRequestCode = { forgotPasswordViewModel.requestCode() }
                )
            }

            uiState.codeVerified -> {
                ChangePassword(
                    uiState = uiState,
                    onPasswordChange = { newPassword ->
                        forgotPasswordViewModel.onPasswordChange(newPassword)
                    },
                    onConfirmPasswordChange = { newPassword ->
                        forgotPasswordViewModel.onConfirmPasswordChange(newPassword)
                    },
                    onResetPassword = { forgotPasswordViewModel.resetPassword() }
                )
            }

            else -> {
                VerifyCode(
                    uiState = uiState,
                    onCodeChange = { code -> forgotPasswordViewModel.onCodeChange(code) },
                    onVerifyCode = { forgotPasswordViewModel.verifyCode() }
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.RequestCode(
    uiState: ForgotPasswordScreenState,
    onEmailChange: (String) -> Unit = {},
    onRequestCode: () -> Unit = {}
) {
    WFRTextField(
        value = uiState.email,
        onValueChange = onEmailChange,
        labelRes = R.string.email_address,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email, imeAction = ImeAction.Done
        ),
        validationError = getErrorMessage(uiState.emailValidationMessageId),
        clearFieldIconEnabled = true
    )

    Text(
        text = stringResource(id = R.string.forgot_password_info),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        style = MaterialTheme.typography.bodyMedium
    )

    Spacer(modifier = Modifier.weight(1f))

    WFRButton(
        onClick = onRequestCode,
        label = R.string.request_code,
        modifier = Modifier.padding(top = 8.dp),
        loading = uiState.loading
    )
}

@Composable
@Preview(heightDp = 250)
private fun RequestCodePreview() {
    WalkingForRochesterTheme {
        Surface {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RequestCode(uiState = ForgotPasswordScreenState())
            }
        }
    }
}

@Composable
private fun getErrorMessage(@StringRes msgId: Int): String {
    return when (msgId) {
        0 -> ""
        else -> stringResource(id = msgId)
    }
}

@Composable
private fun EmailMessage(
    email: String,
    @StringRes msgResId: Int
) {
    Text(
        text = email,
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.titleMedium
    )
    Text(
        text = stringResource(id = msgResId),
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun ColumnScope.VerifyCode(
    uiState: ForgotPasswordScreenState,
    onCodeChange: (String) -> Unit = {},
    onVerifyCode: () -> Unit = {}
) {
    EmailMessage(
        email = uiState.email,
        msgResId = R.string.forgot_password_check_email
    )

    Spacer(modifier = Modifier.height(8.dp))

    WFRTextField(
        value = uiState.code,
        onValueChange = onCodeChange,
        labelRes = R.string.enter_code_desc,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done
        ),
        validationError = getErrorMessage(msgId = uiState.codeValidationMessageId),
        readOnly = uiState.codeVerified,
        clearFieldIconEnabled = true
    )

    Spacer(modifier = Modifier.weight(1f))

    WFRButton(
        onClick = onVerifyCode,
        label = R.string.verify_code,
        modifier = Modifier.padding(top = 8.dp),
        loading = uiState.loading
    )
}

@Composable
@Preview(heightDp = 250, uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun VerifyCodePreview() {
    WalkingForRochesterTheme {
        Surface {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VerifyCode(uiState = ForgotPasswordScreenState(email = "test@email.com"))
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ColumnScope.ChangePassword(
    uiState: ForgotPasswordScreenState,
    onPasswordChange: (String) -> Unit = {},
    onConfirmPasswordChange: (String) -> Unit = {},
    onResetPassword: () -> Unit = {}
) {
    EmailMessage(
        email = uiState.email,
        msgResId = R.string.forgot_password_change
    )
    Spacer(modifier = Modifier.height(8.dp))

    WFRPasswordField(
        value = uiState.password,
        onValueChange = onPasswordChange,
        labelRes = R.string.password,
        modifier = Modifier.autofill(
            autofillTypes = listOf(AutofillType.NewPassword),
            onFill = onPasswordChange
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password, imeAction = ImeAction.Next
        ),
        validationError = getErrorMessage(uiState.passwordValidationMessageId)
    )

    WFRPasswordField(
        value = uiState.confirmPassword,
        onValueChange = onConfirmPasswordChange,
        labelRes = R.string.confirm_password,
        modifier = Modifier.autofill(
            autofillTypes = listOf(AutofillType.NewPassword),
            onFill = onPasswordChange
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
        ),
        validationError = getErrorMessage(uiState.confirmPasswordValidationMessageId),
    )

    Spacer(modifier = Modifier.weight(1f))

    WFRButton(
        label = R.string.change_password,
        onClick = onResetPassword,
        loading = uiState.loading
    )
}

@Composable
@Preview(heightDp = 300)
private fun ChangePasswordPreview() {
    WalkingForRochesterTheme {
        Surface {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ChangePassword(
                    uiState = ForgotPasswordScreenState(
                        email = "test@email.com",
                        password = "test",
                        confirmPassword = "test"
                    )
                )
            }
        }
    }
}
