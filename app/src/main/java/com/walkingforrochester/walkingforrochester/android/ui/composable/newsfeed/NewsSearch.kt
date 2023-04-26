package com.walkingforrochester.walkingforrochester.android.ui.composable.newsfeed

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsSearch(modifier: Modifier = Modifier) {
    var query by rememberSaveable { mutableStateOf("") }
    SearchBar(
        query = query,
        onQueryChange = { text ->
            query = text
        },
        onSearch = {},
        active = false,
        onActiveChange = { },
        leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = "Search") },
        placeholder = { Text("Search title, month or year") },
        modifier = modifier
    ) {

    }
}

@Preview
@Composable
fun PreviewNewsSearch() {
    WalkingForRochesterTheme {
        NewsSearch()
    }
}