package com.walkingforrochester.walkingforrochester.android.ui.composable.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.R

@Composable
fun DeleteAccountDialog(
    modifier: Modifier = Modifier,
    hasCommunityService: Boolean = false,
    onDismissRequest: () -> Unit = {},
    onConfirmed: () -> Unit = {}
) {
    val confirmedEnabled = remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                    onConfirmed()
                },
                enabled = confirmedEnabled.value
            ) {
                Text(text = stringResource(id = R.string.delete_account))
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        title = {
            Text(text = stringResource(id = R.string.delete_account))
        },
        text = {
            DeleteDialogBody(
                confirmEnabled = confirmedEnabled,
                hasCommunityService = hasCommunityService
            )
        }
    )
}

@Composable
private fun DeleteDialogBody(
    confirmEnabled: MutableState<Boolean>,
    hasCommunityService: Boolean,
    modifier: Modifier = Modifier,
) {
    var confirmText by remember { mutableStateOf("") }
    val userConfirmed by remember {
        derivedStateOf { confirmText == "YES" }
    }
    confirmEnabled.value = userConfirmed

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
    ) {

        Text(
            text = stringResource(id = R.string.delete_account_msg),
            style = MaterialTheme.typography.bodyMedium
        )

        if (hasCommunityService) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.delete_account_service_msg),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = confirmText,
            onValueChange = { confirmText = it.uppercase() },
            modifier = Modifier.focusRequester(focusRequester),
            label = {
                Text(
                    text = stringResource(id = R.string.delete_account_confirm_text),
                )
            },
            singleLine = true
        )
    }
}

@Preview
@Composable
fun DialogPreview() {
    DeleteAccountDialog(
        hasCommunityService = false
    )
}