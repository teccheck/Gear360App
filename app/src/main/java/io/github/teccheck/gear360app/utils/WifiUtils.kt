package io.github.teccheck.gear360app.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import androidx.core.content.getSystemService

object WifiUtils {

    fun getMacAddress(context: Context): String {
        val man = context.applicationContext.getSystemService<WifiManager>()
        return man?.connectionInfo?.macAddress ?: return ""
    }

    fun connectToWifi(context: Context, ssid: String, password: String, hidden: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val specifier = WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(password)
                .setIsHiddenSsid(hidden)
                .build()

            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .setNetworkSpecifier(specifier)
                .build();

            val manager =
                context.applicationContext.getSystemService<ConnectivityManager>() ?: return

            val callback = NetworkCallback(manager)
            manager.requestNetwork(request, callback)
        } else {
            val config = WifiConfiguration()
            config.SSID = ssid
            config.preSharedKey = password

            val manager = context.applicationContext.getSystemService<WifiManager>() ?: return

            val netId: Int = manager.addNetwork(config)
            manager.disconnect()
            manager.enableNetwork(netId, true)
            manager.reconnect()
        }
    }

    fun disconnectWifi(context: Context) {
        val manager =
            context.applicationContext.getSystemService<ConnectivityManager>() ?: return
    }

    private class NetworkCallback(private val manager: ConnectivityManager) :
        ConnectivityManager.NetworkCallback() {
        @Override
        override fun onAvailable(network: Network) {
            super.onAvailable(network);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.bindProcessToNetwork(network);
            } else {
                ConnectivityManager.setProcessDefaultNetwork(network);
            }
        }
    }
}