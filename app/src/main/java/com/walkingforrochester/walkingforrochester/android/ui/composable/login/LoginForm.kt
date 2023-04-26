package com.walkingforrochester.walkingforrochester.android.ui.composable.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRTextField
import com.walkingforrochester.walkingforrochester.android.ui.state.LoginScreenState

@Composable
fun LoginForm(
    modifier: Modifier = Modifier,
    loginScreenState: LoginScreenState,
    onEmailAddressValueChange: (String) -> Unit,
    onPasswordValueChange: (String) -> Unit,
    onPasswordVisibilityChange: () -> Unit
) {
    Column(modifier = modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        WFRTextField(
            value = loginScreenState.emailAddress,
            onValueChange = onEmailAddressValueChange,
            labelRes = R.string.email_address,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            validationError = loginScreenState.emailAddressValidationMessage,
            clearFieldIconEnabled = true
        )
        WFRTextField(
            value = loginScreenState.password,
            onValueChange = onPasswordValueChange,
            labelRes = R.string.password,
            visualTransformation = if (loginScreenState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            validationError = loginScreenState.authenticationErrorMessage.ifEmpty { loginScreenState.passwordValidationMessage },
            trailingIcon = {
                IconButton(onClick = onPasswordVisibilityChange) {
                    Icon(
                        painter = painterResource(id = if (loginScreenState.passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
                        contentDescription = stringResource(R.string.toggle_password_visibility)
                    )
                }
            },
        )
    }
}