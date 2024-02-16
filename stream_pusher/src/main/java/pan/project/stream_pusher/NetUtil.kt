package pan.project.stream_pusher

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.text.format.Formatter
import android.util.Log
import android.widget.Toast
import java.io.IOException
import java.net.NetworkInterface
import java.net.ServerSocket
import java.util.*

object NetUtil {
    private fun getMobileIp(): String {
        var mobileIp = "0.0.0.0"
        NetworkInterface.getNetworkInterfaces().let {
            loo@ for (networkInterface in Collections.list(it)) {
                for (inetAddresses in Collections.list(networkInterface.inetAddresses)) {
                    if (!inetAddresses.isLoopbackAddress && !inetAddresses.isLinkLocalAddress) {
                        inetAddresses.hostAddress?.let { hostAddress ->
                            mobileIp = hostAddress
                        }
                        break@loo
                    }
                }
            }
        }
        return mobileIp
    }

    private fun getWifiIp(context: Context): String {
        var wifiIp = ""
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Android Q 以上建议使用getNetworkCapabilities API
        connectivityManager.run {
            activeNetwork?.let { network ->
                getNetworkCapabilities(network)?.transportInfo?.let { transportInfo ->
                    if (transportInfo is WifiInfo) {
                        wifiIp = Formatter.formatIpAddress(transportInfo.ipAddress)
                    } else if (transportInfo.javaClass.toString().contains("VpnTransportInfo")) {
                        Toast.makeText(context, "获取ip失败，请关闭vpn", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return wifiIp
    }


    fun getIp(context: Context): String {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.run {
            // Android M 以上建议使用getNetworkCapabilities API
            activeNetwork?.let { network ->
                getNetworkCapabilities(network)?.let { networkCapabilities ->
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                        when {
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                                // 通过手机流量
                                val mobileIp = getMobileIp()
                                Log.d("stream_pusher", "mobileIp: $mobileIp")
                                return mobileIp
                            }

                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                                // 通过WIFI
                                val wifiIp = getWifiIp(context)
                                Log.d("stream_pusher", "wifiIp: $wifiIp")
                                return wifiIp
                            }
                        }
                    }
                }
            }
        }
        return ""
    }

    fun getAvailablePort(startPort: Int): Int {
        var port = startPort
        while (true) {
            try {
                ServerSocket(port).use {
                    return it.localPort
                }
            } catch (ex: IOException) {
                port++
            }
        }
    }
}