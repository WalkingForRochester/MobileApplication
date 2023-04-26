package com.walkingforrochester.walkingforrochester.android.ui.composable.newsfeed

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRWebView
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun NewsFeedScreen(modifier: Modifier = Modifier) {
    WFRWebView(modifier = modifier, url = "https://walkingforrochester.org/category/news/")
}

@Preview(showBackground = true)
@Composable
fun PreviewNewsFeedScreen() {
    WalkingForRochesterTheme {
        NewsFeedScreen()
    }
}