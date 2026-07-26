package com.walkingforrochester.walkingforrochester.android.ktx

import androidx.window.core.layout.WindowSizeClass

fun WindowSizeClass.isPortraitMode(): Boolean {
    return isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)
        && !isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
}