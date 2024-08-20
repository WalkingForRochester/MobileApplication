package com.walkingforrochester.walkingforrochester.android.ui.composable.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.password
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRTextField
import com.walkingforrochester.walkingforrochester.android.ui.modifier.autofill
import com.walkingforrochester.walkingforrochester.android.ui.state.LoginScreenState

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginForm(
    modifier: Modifier = Modifier,
    loginScreenState: LoginScreenState,
    onEmailAddressValueChange: (String, Boolean) -> Unit,
    onPasswordValueChange: (String, Boolean) -> Unit,
    onPasswordVisibilityChange: () -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WFRTextField(
            modifier = Modifier.autofill(
                autofillTypes = listOf(AutofillType.EmailAddress),
                onFill = { email -> onEmailAddressValueChange(email, true) }
            ),
            value = loginScreenState.emailAddress,
            onValueChange = { email -> onEmailAddressValueChange(email, false) },
            labelRes = R.string.email_address,
            testTag = "login_email",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            validationError = loginScreenState.emailAddressValidationMessage,
            clearFieldIconEnabled = true
        )
        WFRTextField(
            modifier = Modifier
                .autofill(
                    autofillTypes = listOf(AutofillType.Password),
                    onFill = { password -> onPasswordValueChange(password, true) }
                )
                .semantics { password() },
            value = loginScreenState.password,
            onValueChange = { password -> onPasswordValueChange(password, false) },
            labelRes = R.string.password,
            testTag = "login_password",
            visualTransformation = if (loginScreenState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            validationError = loginScreenState.authenticationErrorMessage,
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