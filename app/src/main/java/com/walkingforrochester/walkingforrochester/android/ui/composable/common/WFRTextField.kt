package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.password
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.walkingforrochester.walkingforrochester.android.R

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
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes labelRes: Int,
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
        Icon(imageVector = Icons.Filled.Clear, contentDescription = null)
    }
}

@Composable
private fun PasswordVisibilityButton(
    visible: Boolean,
    onToggleVisibility: () -> Unit
) {
    val icon = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility
    val desc = if (visible) R.string.hide_password else R.string.show_password
    IconButton(onClick = onToggleVisibility) {
        Icon(
            painter = rememberVectorPainter(image = icon),
            contentDescription = stringResource(desc)
        )
    }
}