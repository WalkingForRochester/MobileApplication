package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun WFRTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes labelRes: Int,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    supportingText: String = "",
    supportingTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    validationError: String = "",
    validationErrorColor: Color = MaterialTheme.colorScheme.error,
    trailingIcon: (@Composable () -> Unit)? = null,
    clearFieldIconEnabled: Boolean = false,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    nextFocusDirection: FocusDirection = FocusDirection.Down
) {
    val focusManager = LocalFocusManager.current
    TextField(
        modifier = modifier.fillMaxWidth(),
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
        trailingIcon = trailingIcon ?: if (clearFieldIconEnabled && value.isNotEmpty()) {
            {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(imageVector = Icons.Filled.Clear, contentDescription = null)
                }
            }
        } else null,
        isError = validationError.isNotEmpty(),
        supportingText = if (validationError.isNotEmpty()) {
            { Text(text = validationError, color = validationErrorColor) }
        } else if (supportingText.isNotEmpty()) {
            { Text(text = supportingText, color = supportingTextColor) }
        } else null
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
    supportingText: String = "",
    validationError: String = "",
    trailingIcon: (@Composable () -> Unit)? = null,
    clearFieldIconEnabled: Boolean = false,
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
        trailingIcon = trailingIcon ?: if (clearFieldIconEnabled) {
            {
                if (value.isNotEmpty()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(imageVector = Icons.Filled.Clear, contentDescription = null)
                    }
                }
            }
        } else null,
        isError = validationError.isNotEmpty(),
        supportingText = if (validationError.isNotEmpty()) {
            { Text(text = validationError) }
        } else if (supportingText.isNotEmpty()) {
            { Text(text = supportingText) }
        } else null
    )
}