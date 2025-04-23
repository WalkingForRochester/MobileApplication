package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.getSystemService
import com.walkingforrochester.walkingforrochester.android.R
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber


/**
 * Network utility to get current state of internet connection
 */

enum class ConnectionState {
    Available,
    Unavailable
}

private fun determineCurrentConnectivityState(
    connectivityManager: ConnectivityManager?
): ConnectionState {
    if (connectivityManager == null) return ConnectionState.Unavailable
    if (!connectivityManager.isDefaultNetworkActive) return ConnectionState.Unavailable
    val activeNetwork = connectivityManager.activeNetwork
    val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
    return determineCurrentConnectivityState(networkCapabilities)
}

private fun determineCurrentConnectivityState(
    networkCapabilities: NetworkCapabilities?
): ConnectionState {
    val hasInternet =
        networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    val validated =
        networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true

    // Verifies a mobile network isn't temporarily disconnected...
    val notSuspended = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED) == true
    } else {
        true // Assume true on older platforms
    }

    return when {
        hasInternet && validated && notSuspended -> ConnectionState.Available
        else -> ConnectionState.Unavailable
    }
}

fun Context.observeConnectivityAsFlow() = callbackFlow {
    val connectivityManager: ConnectivityManager? = applicationContext.getSystemService()

    // Start off with an initial value
    trySend(determineCurrentConnectivityState(connectivityManager))

    val callback = networkCallback { state ->
        trySend(state)
    }

    connectivityManager?.registerDefaultNetworkCallback(callback)

    // Remove callback when not used
    awaitClose {
        // Remove listeners
        connectivityManager?.unregisterNetworkCallback(callback)
    }
}


fun networkCallback(callback: (ConnectionState) -> Unit): ConnectivityManager.NetworkCallback {
    return object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // Have a new network, so report available
            callback(ConnectionState.Available)
        }

        override fun onLost(network: Network) {
            // All networks lost, so report unavailable
            callback(ConnectionState.Unavailable)
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            // Verify default network still valid...
            callback(determineCurrentConnectivityState(networkCapabilities))
        }
    }
}

@Composable
fun NoConnectionOverlay() {
    Surface(
        modifier = Modifier.zIndex(Float.MAX_VALUE),
        color = Color.Black.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.connectivity_loss),
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall.copy(lineBreak = LineBreak.Heading),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(24.dp))
            CircularProgressIndicator(color = Color.White)
        }
    }
}