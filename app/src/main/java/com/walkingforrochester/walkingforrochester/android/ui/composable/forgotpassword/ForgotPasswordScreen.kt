package com.walkingforrochester.walkingforrochester.android.ui.composable.forgotpassword

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRButton
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRTextField
import com.walkingforrochester.walkingforrochester.android.ui.state.ForgotPasswordScreenEvent
import com.walkingforrochester.walkingforrochester.android.viewmodel.ForgotPasswordViewModel

@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    forgotPasswordViewModel: ForgotPasswordViewModel = hiltViewModel(),
    onPasswordResetComplete: () -> Unit
) {
    val uiState by forgotPasswordViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        forgotPasswordViewModel.eventFlow.collect { event ->
            when (event) {
                ForgotPasswordScreenEvent.PasswordReset -> onPasswordResetComplete()
            }
        }
    }

    Column(
        modifier = modifier
            .padding(8.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
                )
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WFRTextField(
            value = uiState.email,
            onValueChange = forgotPasswordViewModel::onEmailChange,
            labelRes = R.string.email_address,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email, imeAction = ImeAction.Next
            ),
            validationError = uiState.emailValidationMessage,
            validationErrorColor = Color.Red,
            supportingText = if (uiState.internalCode.isNotEmpty()) stringResource(R.string.forgot_password_info)
            else "",
            supportingTextColor = Color.Black,
            readOnly = uiState.internalCode.isNotEmpty(),
            clearFieldIconEnabled = true
        )
        if (uiState.internalCode.isNotEmpty()) {
            WFRTextField(
                value = uiState.code,
                onValueChange = forgotPasswordViewModel::onCodeChange,
                labelRes = R.string.code,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Next
                ),
                validationError = uiState.codeValidationMessage,
                validationErrorColor = Color.Red,
                readOnly = uiState.codeVerified,
                clearFieldIconEnabled = true
            )
        }
        if (uiState.codeVerified) {
            WFRTextField(
                value = uiState.password,
                onValueChange = forgotPasswordViewModel::onPasswordChange,
                labelRes = R.string.password,
                visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password, imeAction = ImeAction.Next
                ),
                validationError = uiState.passwordValidationMessage,
                validationErrorColor = Color.Red,
                trailingIcon = {
                    IconButton(onClick = { forgotPasswordViewModel.onPasswordVisibilityChange() }) {
                        Icon(
                            painter = painterResource(id = if (uiState.passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
                            contentDescription = stringResource(R.string.toggle_password_visibility)
                        )
                    }
                }
            )
            WFRTextField(
                value = uiState.confirmPassword,
                onValueChange = forgotPasswordViewModel::onConfirmPasswordChange,
                labelRes = R.string.confirm_password,
                visualTransformation = if (uiState.confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
                ),
                validationError = uiState.confirmPasswordValidationMessage,
                validationErrorColor = Color.Red,
                trailingIcon = {
                    IconButton(onClick = { forgotPasswordViewModel.onConfirmPasswordVisibilityChange() }) {
                        Icon(
                            painter = painterResource(id = if (uiState.confirmPasswordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
                            contentDescription = stringResource(R.string.toggle_password_visibility)
                        )
                    }
                }
            )
        }

        if (uiState.internalCode.isEmpty()) {
            WFRButton(
                label = R.string.send_code,
                buttonColor = Color.Black,
                labelColor = Color.White,
                onClick = { forgotPasswordViewModel.sendCode() },
                loading = uiState.loading
            )
        } else if (!uiState.codeVerified) {
            WFRButton(
                label = R.string.verify_code,
                buttonColor = Color.Black,
                labelColor = Color.White,
                onClick = { forgotPasswordViewModel.verifyCode() },
                loading = uiState.loading
            )
        } else {
            WFRButton(
                label = R.string.reset_password,
                buttonColor = Color.Black,
                labelColor = Color.White,
                onClick = { forgotPasswordViewModel.resetPassword() },
                loading = uiState.loading
            )
        }
    }
}