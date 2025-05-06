package com.walkingforrochester.walkingforrochester.android.ui.composable.registration

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.model.AccountProfile
import com.walkingforrochester.walkingforrochester.android.ui.PhoneNumberVisualTransformation
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.CommunityServiceCheckbox
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRPasswordField
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRTextField
import com.walkingforrochester.walkingforrochester.android.ui.state.RegistrationScreenState
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun RegistrationForm(
    uiState: RegistrationScreenState,
    registrationProfile: AccountProfile,
    modifier: Modifier = Modifier,
    onProfileChange: (AccountProfile) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onPasswordConfirmationChange: (String) -> Unit = {},
) {
    Column(
        modifier = modifier
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WFRTextField(
            value = registrationProfile.firstName,
            onValueChange = { onProfileChange(registrationProfile.copy(firstName = it)) },
            labelRes = R.string.first_name,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .semantics { contentType = ContentType.PersonFirstName },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
            validationError = errorMessage(uiState.firstNameValidationMessageId),
        )

        WFRTextField(
            value = registrationProfile.lastName,
            onValueChange = { onProfileChange(registrationProfile.copy(lastName = it)) },
            labelRes = R.string.last_name,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .semantics { contentType = ContentType.PersonLastName },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            validationError = errorMessage(uiState.lastNameValidationMessageId),
        )

        WFRTextField(
            value = registrationProfile.email,
            onValueChange = { onProfileChange(registrationProfile.copy(email = it)) },
            labelRes = R.string.email_address,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .semantics { contentType = ContentType.EmailAddress },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            validationError = errorMessage(uiState.emailValidationMessageId),
        )

        WFRTextField(
            value = registrationProfile.phoneNumber,
            onValueChange = { onProfileChange(registrationProfile.copy(phoneNumber = it)) },
            labelRes = R.string.phone_number,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .semantics { contentType = ContentType.PhoneNumber },
            visualTransformation = PhoneNumberVisualTransformation(LocalContext.current),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            validationError = errorMessage(uiState.phoneValidationMessageId),
        )

        WFRTextField(
            value = registrationProfile.nickname,
            onValueChange = { onProfileChange(registrationProfile.copy(nickname = it)) },
            labelRes = R.string.nickname,
            modifier = Modifier.padding(horizontal = 16.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
        )

        WFRPasswordField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            labelRes = R.string.password,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .semantics { contentType = ContentType.NewPassword },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            validationError = errorMessage(uiState.passwordValidationMessageId)
        )

        WFRPasswordField(
            value = uiState.confirmPassword,
            onValueChange = onPasswordConfirmationChange,
            labelRes = R.string.confirm_password,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .semantics { contentType = ContentType.NewPassword },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            validationError = errorMessage(uiState.confirmPasswordValidationMessageId)
        )

        CommunityServiceCheckbox(
            checked = registrationProfile.communityService,
            onCheckedChange = { onProfileChange(registrationProfile.copy(communityService = it)) },
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 8.dp),
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RegistrationFormPreview() {
    WalkingForRochesterTheme {
        Surface {
            RegistrationForm(
                uiState = RegistrationScreenState(
                    emailValidationMessageId = R.string.invalid_email
                ),
                registrationProfile = AccountProfile.DEFAULT_PROFILE.copy(
                    firstName = "Bob",
                    email = "test@"
                ),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}

@Composable
private fun errorMessage(@StringRes msgId: Int): String {
    return when {
        msgId != 0 -> stringResource(id = msgId)
        else -> ""
    }
}