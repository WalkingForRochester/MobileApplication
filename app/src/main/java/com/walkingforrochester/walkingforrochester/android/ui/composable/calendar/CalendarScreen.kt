package com.walkingforrochester.walkingforrochester.android.ui.composable.calendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRWebView

@Composable
fun GuidelinesScreen(modifier: Modifier = Modifier) {
    WFRWebView(
        modifier = modifier,
        url = "https://outlook.office365.com/owa/calendar/283b0c4cca3d464cbb94b9f9281041e0@walkingforrochester.org/6b15951a8ef44694b1c2c59c83e88bd213340231303999867966/calendar.html"
    )
}