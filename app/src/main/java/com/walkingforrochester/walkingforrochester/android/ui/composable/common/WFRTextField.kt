package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.password
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WFRTextField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes labelRes: Int,
    modifier: Modifier = Modifier,
    testTag: String = "",
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    validationError: String = "",
    trailingIcon: (@Composable () -> Unit)? = null,
    clearFieldIconEnabled: Boolean = true,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    nextFocusDirection: FocusDirection = FocusDirection.Down
) {
    val focusManager = LocalFocusManager.current
    TextField(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                if (testTag.isNotBlank()) {
                    testTagsAsResourceId = true
                    this.testTag = testTag
                }
            },
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(labelRes)) },
        singleLine = singleLine,
        readOnly = readOnly,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(
            onDone = keyboardActions.onDone,
            onGo = keyboardActions.onGo,
            onNext = {
                if (keyboardActions.onNext != null) {
                    keyboardActions.onNext?.invoke(this)
                } else {
                    focusManager.moveFocus(nextFocusDirection)
                }
            },
            onPrevious = keyboardActions.onPrevious,
            onSearch = keyboardActions.onSearch,
            onSend = keyboardActions.onSend
        ),
        trailingIcon = when {
            trailingIcon != null -> trailingIcon
            clearFieldIconEnabled && value.isNotEmpty() -> {
                { ClearTextButton(onClearText = { onValueChange("") }) }
            }

            else -> null
        },
        isError = validationError.isNotBlank(),
        supportingText = if (validationError.isNotBlank()) {
            { Text(text = validationError) }
        } else null
    )
}

@Composable
fun WFRPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes labelRes: Int,
    modifier: Modifier = Modifier,
    testTag: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    validationError: String = "",
) {
    var viewPassword by rememberSaveable { mutableStateOf(false) }
    WFRTextField(
        value = value,
        onValueChange = onValueChange,
        labelRes = labelRes,
        modifier = modifier.semantics { password() },
        testTag = testTag,
        visualTransformation = if (viewPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        validationError = validationError,
        trailingIcon = {
            PasswordVisibilityButton(
                visible = viewPassword,
                onToggleVisibility = { viewPassword = !viewPassword }
            )
        },
        clearFieldIconEnabled = false
    )
}

@Composable
fun WFROutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes labelRes: Int,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    validationError: String = "",
    trailingIcon: (@Composable () -> Unit)? = null,
    clearFieldIconEnabled: Boolean = true,
    singleLine: Boolean = true,
    readOnly: Boolean = false
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(labelRes)) },
        singleLine = singleLine,
        readOnly = readOnly,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        trailingIcon = when {
            trailingIcon != null -> trailingIcon
            clearFieldIconEnabled && value.isNotEmpty() -> {
                { ClearTextButton(onClearText = { onValueChange("") }) }
            }

            else -> null
        },
        isError = validationError.isNotEmpty(),
        supportingText = if (validationError.isNotEmpty()) {
            { Text(text = validationError) }
        } else null
    )
}

@Composable
private fun ClearTextButton(
    onClearText: () -> Unit
) {
    IconButton(onClick = onClearText) {
        Icon(painterResource(R.drawable.ic_cancel_24dp), contentDescription = null)
    }
}

@Composable
private fun PasswordVisibilityButton(
    visible: Boolean,
    onToggleVisibility: () -> Unit
) {
    val iconResId =
        if (visible) R.drawable.ic_visibility_off_24dp else R.drawable.ic_visibility_24dp
    val desc = if (visible) R.string.hide_password else R.string.show_password
    IconButton(onClick = onToggleVisibility) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = stringResource(desc)
        )
    }
}

@Composable
@PreviewLightDark
private fun PreviewWFRTextField() {
    WalkingForRochesterTheme {
        Column(Modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)) {
            WFRTextField("Test", onValueChange = {}, labelRes = R.string.first_name)
            Spacer(Modifier.height(4.dp))
            WFROutlinedTextField("", onValueChange = {}, labelRes = R.string.first_name)
            Spacer(Modifier.height(4.dp))
            WFRPasswordField("test", onValueChange = {}, labelRes = R.string.password)
        }
    }
}