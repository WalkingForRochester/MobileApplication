package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WFRWebView(modifier: Modifier = Modifier, url: String) {
    val state = rememberWebViewState(url)
    val visibility by animateFloatAsState(
        targetValue = if (state.isLoading) 0f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    Box(modifier = modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.Black
            )
        }
        WebView(
            modifier = Modifier.alpha(visibility),
            state = state,
            client = object : AccompanistWebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    view?.apply {
                        evaluateJavascript(
                            """
                                (function() {
                                    try {
                                        document.getElementById('main-header').remove();
                                        document.getElementById('main-footer').remove();
                                    } catch (error) {
                                        console.info('WFR header and footer elements were not found');
                                    }
                                })();
                                """
                        ) {}
                    }
                }
            }, onCreated = {
                it.settings.javaScriptEnabled = true
            })
    }
}