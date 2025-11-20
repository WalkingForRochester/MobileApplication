package com.walkingforrochester.walkingforrochester.android.ui.composable.login

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRPasswordField
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRTextField
import com.walkingforrochester.walkingforrochester.android.ui.state.LoginScreenState

@Composable
fun LoginForm(
    modifier: Modifier = Modifier,
    loginScreenState: LoginScreenState,
    onEmailAddressValueChange: (String) -> Unit,
    onPasswordValueChange: (String) -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WFRTextField(
            value = loginScreenState.emailAddress,
            onValueChange = { email -> onEmailAddressValueChange(email) },
            labelRes = R.string.email_address,
            modifier = Modifier.semantics { contentType = ContentType.EmailAddress },
            testTag = "login_email",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            validationError = errorMessage(loginScreenState.emailAddressValidationMessageId),
        )
        WFRPasswordField(
            value = loginScreenState.password,
            onValueChange = { password -> onPasswordValueChange(password) },
            labelRes = R.string.password,
            modifier = Modifier.semantics { contentType = ContentType.Password },
            testTag = "login_password",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            validationError = errorMessage(
                msgId = loginScreenState.authenticationErrorMessageId,
                msg = loginScreenState.authenticationErrorMessage
            )
        )
    }
}

@Composable
private fun errorMessage(@StringRes msgId: Int, msg: String = ""): String {
    return when {
        msg.isNotBlank() -> msg
        msgId != 0 -> stringResource(id = msgId)
        else -> ""
    }
}