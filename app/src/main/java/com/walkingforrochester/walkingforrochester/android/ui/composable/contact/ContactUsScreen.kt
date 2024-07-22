package com.walkingforrochester.walkingforrochester.android.ui.composable.contact

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRWebView

@Composable
fun ContactUsScreen(modifier: Modifier = Modifier) {
    WFRWebView(modifier = modifier, url = "https://walkingforrochester.org/faq/")
}