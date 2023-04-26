package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow


/**
 * Network utility to get current state of internet connection
 */

sealed class ConnectionState {
    object Available : ConnectionState()
    object Unavailable : ConnectionState()
}

val Context.currentConnectivityState: ConnectionState
    get() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return getCurrentConnectivityState(connectivityManager)
    }

private fun getCurrentConnectivityState(
    connectivityManager: ConnectivityManager
): ConnectionState = with(connectivityManager) {
    val connected =
        getNetworkCapabilities(activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            ?: false

    if (connected) ConnectionState.Available else ConnectionState.Unavailable
}

fun Context.observeConnectivityAsFlow() = callbackFlow {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val callback = networkCallback {
        trySend(getCurrentConnectivityState(connectivityManager))
    }

    val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()

    connectivityManager.registerNetworkCallback(networkRequest, callback)

    // Remove callback when not used
    awaitClose {
        // Remove listeners
        connectivityManager.unregisterNetworkCallback(callback)
    }
}


fun networkCallback(callback: () -> Unit): ConnectivityManager.NetworkCallback {
    return object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            callback()
        }

        override fun onLost(network: Network) {
            callback()
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            callback()
        }
    }
}

@Composable
fun connectivityState(): State<ConnectionState> {
    val context = LocalContext.current

    // Creates a State<ConnectionState> with current connectivity state as initial value
    return produceState(initialValue = context.currentConnectivityState) {
        // In a coroutine, can make suspend calls
        context.observeConnectivityAsFlow().collect { value = it }
    }
}

@Composable
fun NoConnectionOverlay() {
    Surface(modifier = Modifier.zIndex(Float.MAX_VALUE), color = Color.Black.copy(alpha = 0.5f)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No Internet connection. Make sure Wi-Fi or cellular data is turned on.",
                style = MaterialTheme.typography.headlineSmall.copy(color = Color.White),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(24.dp))
            CircularProgressIndicator(color = Color.White)
        }
    }
}