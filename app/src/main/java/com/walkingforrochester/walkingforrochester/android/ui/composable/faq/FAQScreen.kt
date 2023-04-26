package com.walkingforrochester.walkingforrochester.android.ui.composable.faq

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRWebView

@Composable
fun FAQScreen(modifier: Modifier = Modifier) {
    WFRWebView(modifier = modifier, url = "https://walkingforrochester.org/faq/")
}