package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout.LayoutParams
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun WFRWebView(modifier: Modifier = Modifier, url: String) {
    val urlState = remember { url }
    var loading = remember { mutableStateOf<Boolean>(false) }

    Box(modifier = modifier) {
        val background = MaterialTheme.colorScheme.background
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams =
                        LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                    setBackgroundColor(background.toArgb())
                    webChromeClient = ProgressClient(loading)
                }
            },
            update = { webView ->
                webView.settings.javaScriptEnabled = false
                webView.loadUrl(urlState)
            }
        )

        AnimatedVisibility(
            loading.value,
            modifier = Modifier.align(Alignment.Center)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

class ProgressClient(val loadingState: MutableState<Boolean>) : WebChromeClient() {
    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        loadingState.value = newProgress != 100
    }
}