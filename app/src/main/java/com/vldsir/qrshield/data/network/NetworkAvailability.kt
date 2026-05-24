package com.vldsir.qrshield.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest

/**
 * Checks whether the device has an internet-capable network transport.
 *
 * Two-tier check:
 *  1. Fast path — active/default network reported by the OS.
 *  2. Fallback  — scan every available network in case `activeNetwork` or
 *     `getNetworkCapabilities()` returns null (common on OneUI / MIUI / some
 *     Android 12+ builds even while the device is genuinely online).
 *
 * NOTE: NET_CAPABILITY_VALIDATED is intentionally omitted. Samsung (OneUI) and
 * several custom ROMs do not set it reliably on otherwise working connections,
 * which causes false "offline" results. Transport type + NET_CAPABILITY_INTERNET
 * is a reliable enough proxy for "there is an interface that can reach the internet."
 */
class NetworkAvailability(private val context: Context) {

    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun isOnline(): Boolean {
        // Fast path: active default network
        cm.activeNetwork
            ?.let { cm.getNetworkCapabilities(it) }
            ?.let { caps -> if (caps.hasInternetAccess()) return true }

        // Fallback: any available network — use the non-deprecated method form
        return cm.getAllNetworks().any { network ->
            cm.getNetworkCapabilities(network)?.hasInternetAccess() == true
        }
    }

    /**
     * Register a [ConnectivityManager.NetworkCallback] to receive live connectivity
     * events. Pair every [addCallback] call with [removeCallback] (e.g. in
     * `ViewModel.onCleared()`).
     */
    fun addCallback(callback: ConnectivityManager.NetworkCallback) {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(request, callback)
        } catch (_: Exception) { /* ignore — not critical */ }
    }

    fun removeCallback(callback: ConnectivityManager.NetworkCallback) {
        try { cm.unregisterNetworkCallback(callback) } catch (_: Exception) { }
    }

    /**
     * A network qualifies as "internet-capable" when it has a usable transport and
     * the OS believes it can route to the internet.
     */
    private fun NetworkCapabilities.hasInternetAccess(): Boolean =
        (hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
         hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
         hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) &&
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
