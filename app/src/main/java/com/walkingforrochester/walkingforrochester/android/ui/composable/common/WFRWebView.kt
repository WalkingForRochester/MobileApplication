package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import android.annotation.SuppressLint
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WFRWebView(modifier: Modifier = Modifier, url: String) {
    val state = rememberWebViewState(url)
    val visibility by animateFloatAsState(
        targetValue = if (state.isLoading) 0f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "webviewAnimation"
    )

    Box(modifier = modifier) {
        WebView(
            modifier = Modifier.alpha(visibility),
            state = state,
            onCreated = {
                it.settings.javaScriptEnabled = false
            }
        )
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center), color = Color.Black
            )
        }
    }
}

/*class CustomWebClient : AccompanistWebViewClient() {

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        Timber.d("starting: %s", url)
    }

    override fun onPageFinished(view: WebView, url: String?) {

        view.evaluateJavascript(
            """
                                (function() {
                                    try {
                                        document.getElementById('main-header').style.display='none';
                                        document.getElementById('main-footer').style.display='none';
                                    } catch (error) {
                                        console.info('WFR header and footer elements were not found');
                                    }
                                })();
                                """
        ) {
            //Timber.d("callback")
            //view.settings.javaScriptEnabled = false
        }
        super.onPageFinished(view, url)
    }
}*/