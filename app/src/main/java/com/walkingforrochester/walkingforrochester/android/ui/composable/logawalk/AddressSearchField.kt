package com.walkingforrochester.walkingforrochester.android.ui.composable.logawalk

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.FullScreen
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import com.walkingforrochester.walkingforrochester.android.viewmodel.AddressSearchFieldViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressSearchField(
    modifier: Modifier = Modifier,
    addressSearchFieldViewModel: AddressSearchFieldViewModel = hiltViewModel()
) {
    val uiState by addressSearchFieldViewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    if (uiState.addressSearchActive) {
        FullScreen {
            SearchBar(
                query = uiState.addressQuery,
                onQueryChange = addressSearchFieldViewModel::onAddressQueryChange,
                onSearch = {
                    uiState.predictedLocations.firstOrNull()?.let {
                        addressSearchFieldViewModel.onAddressSelect(it.placeId)
                    }
                },
                active = true,
                onActiveChange = addressSearchFieldViewModel::onAddressSearchActiveChange,
                leadingIcon = {
                    IconButton(onClick = {
                        addressSearchFieldViewModel.onAddressSearchActiveChange(
                            false
                        )
                    }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                trailingIcon = if (uiState.addressQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { addressSearchFieldViewModel.onAddressQueryChange("") }) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = null)
                        }
                    }
                } else null,
                placeholder = { Text(stringResource(id = R.string.enter_an_address)) },
                modifier = modifier.focusRequester(focusRequester)
            ) {
                ProvideTextStyle(value = MaterialTheme.typography.titleMedium) {
                    uiState.predictedLocations.forEach {
                        it.structuredFormatting
                        val builder = AnnotatedString.Builder()
                        builder.append(it.description)

                        if (it.matchedSubstrings.isNotEmpty()) {
                            it.matchedSubstrings.forEach { matchedSubstring ->
                                with(matchedSubstring) {
                                    builder.addStyle(
                                        SpanStyle(color = MaterialTheme.colorScheme.primary),
                                        offset,
                                        offset + length
                                    )
                                }
                            }
                        }

                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .clickable { addressSearchFieldViewModel.onAddressSelect(it.placeId) }) {
                            Text(
                                modifier = Modifier.padding(16.dp),
                                text = builder.toAnnotatedString(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Divider()
                    }
                }

                if (uiState.loading) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        }
    } else {
        SearchBar(
            query = uiState.addressQuery,
            onQueryChange = { },
            onSearch = {},
            active = false,
            onActiveChange = addressSearchFieldViewModel::onAddressSearchActiveChange,
            leadingIcon = {
                if (uiState.loading) {
                    CircularProgressIndicator()
                } else {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = stringResource(R.string.search)
                    )
                }
            },
            placeholder = { Text(stringResource(R.string.enter_an_address)) },
            modifier = modifier
        ) {

        }
    }
}

@Preview(showBackground = true, heightDp = 320, widthDp = 320)
@Composable
fun PreviewAddressSearchField() {
    WalkingForRochesterTheme {
        AddressSearchField()
    }
}