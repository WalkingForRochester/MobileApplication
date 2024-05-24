package com.walkingforrochester.walkingforrochester.android.ui.composable.registration

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.PhoneNumberVisualTransformation
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.CommunityServiceCheckbox
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRTextField
import com.walkingforrochester.walkingforrochester.android.ui.state.RegistrationScreenState
import com.walkingforrochester.walkingforrochester.android.viewmodel.RegistrationViewModel
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationForm(
    modifier: Modifier = Modifier,
    registrationViewModel: RegistrationViewModel,
    uiState: RegistrationScreenState
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
            modifier = Modifier.padding(horizontal = 8.dp),
            value = uiState.firstName,
            onValueChange = registrationViewModel::onFirstNameChange,
            labelRes = R.string.first_name,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            validationError = uiState.firstNameValidationMessage,
            validationErrorColor = Color.Red,
            clearFieldIconEnabled = true
        )
        WFRTextField(
            modifier = Modifier.padding(horizontal = 8.dp),
            value = uiState.lastName,
            onValueChange = registrationViewModel::onLastNameChange,
            labelRes = R.string.last_name,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            validationError = uiState.lastNameValidationMessage,
            validationErrorColor = Color.Red,
            clearFieldIconEnabled = true
        )
        WFRTextField(
            modifier = Modifier.padding(horizontal = 8.dp),
            value = uiState.email,
            onValueChange = registrationViewModel::onEmailChange,
            labelRes = R.string.email_address,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            validationError = uiState.emailValidationMessage,
            validationErrorColor = Color.Red,
            clearFieldIconEnabled = true
        )
        WFRTextField(
            modifier = Modifier.padding(horizontal = 8.dp),
            value = uiState.phone,
            onValueChange = registrationViewModel::onPhoneChange,
            labelRes = R.string.phone_number,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            visualTransformation = PhoneNumberVisualTransformation(),
            validationError = uiState.phoneValidationMessage,
            validationErrorColor = Color.Red,
            clearFieldIconEnabled = true
        )
        WFRTextField(
            modifier = Modifier.padding(horizontal = 8.dp),
            value = uiState.nickname,
            onValueChange = registrationViewModel::onNicknameChange,
            labelRes = R.string.nickname,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = {
                registrationViewModel.toggleDatePicker()
            }),
            clearFieldIconEnabled = true
        )

        val datePickerState = rememberDatePickerState()
        if (uiState.showDatePicker) {
            DatePickerDialog(
                onDismissRequest = registrationViewModel::toggleDatePicker,
                confirmButton = {
                    Button(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            registrationViewModel.onDateOfBirthChange(
                                Instant.ofEpochMilli(it).atOffset(ZoneOffset.UTC).toLocalDate()
                            )
                        }
                        registrationViewModel.toggleDatePicker()
                    }) {
                        Text(stringResource(R.string.confirm))
                    }
                }) {
                DatePicker(
                    state = datePickerState,
                    dateValidator = { it < System.currentTimeMillis() }
                )
            }
        }
        WFRTextField(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .clickable(onClick = registrationViewModel::toggleDatePicker),
            value = uiState.dateOfBirth.atStartOfDay().format(registrationViewModel.dateFormatter),
            onValueChange = { },
            labelRes = R.string.date_of_birth,
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = registrationViewModel::toggleDatePicker) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = stringResource(R.string.dob_date_picker)
                    )
                }
            },
            validationError = uiState.dateOfBirthValidationMessage,
            validationErrorColor = Color.Red,
        )

        WFRTextField(
            modifier = Modifier.padding(horizontal = 8.dp),
            value = uiState.password,
            onValueChange = registrationViewModel::onPasswordChange,
            labelRes = R.string.password,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { registrationViewModel.onPasswordVisibilityChange() }) {
                    Icon(
                        painter = painterResource(id = if (uiState.passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
                        contentDescription = stringResource(R.string.toggle_password_visibility)
                    )
                }
            },
            validationError = uiState.passwordValidationMessage,
            validationErrorColor = Color.Red,
        )
        WFRTextField(
            modifier = Modifier.padding(horizontal = 8.dp),
            value = uiState.confirmPassword,
            onValueChange = registrationViewModel::onPasswordConfirmationChange,
            labelRes = R.string.confirm_password,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            visualTransformation = if (uiState.confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { registrationViewModel.onConfirmPasswordVisibilityChange() }) {
                    Icon(
                        painter = painterResource(id = if (uiState.confirmPasswordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
                        contentDescription = stringResource(R.string.toggle_password_visibility)
                    )
                }
            },
            validationError = uiState.confirmPasswordValidationMessage,
            validationErrorColor = Color.Red,
        )
        CommunityServiceCheckbox(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 8.dp),
            checked = uiState.communityService,
            onCheckedChange = registrationViewModel::onCommunityServiceChange
        )
    }
}