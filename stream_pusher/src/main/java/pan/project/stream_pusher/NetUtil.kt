package pan.project.stream_pusher

import java.io.IOException
import java.net.NetworkInterface
import java.net.ServerSocket

object NetUtil {
    private const val VPN_INTERFACE = "tun"
    private const val DEFAULT_IP = "0.0.0.0"
    enum class IpType {
        IPv4, IPv6, All
    }
    private var ipType = IpType.All

    fun getIp(): String {
        val interfaces: List<NetworkInterface> = NetworkInterface.getNetworkInterfaces().toList()
        val vpnInterfaces = interfaces.filter { it.displayName.contains(VPN_INTERFACE) }
        val address: String by lazy { interfaces.findAddress().firstOrNull() ?: DEFAULT_IP }
        return if (vpnInterfaces.isNotEmpty()) {
            val vpnAddresses = vpnInterfaces.findAddress()
            vpnAddresses.firstOrNull() ?: address
        } else {
            address
        }
    }
    private fun List<NetworkInterface>.findAddress(): List<String?> = this.asSequence()
        .map { addresses -> addresses.inetAddresses.asSequence() }
        .flatten()
        .filter { address -> !address.isLoopbackAddress }
        .map { it.hostAddress }
        .filter { address ->
            //exclude invalid IPv6 addresses
            address?.startsWith("fe80") != true && // Exclude link-local addresses
                    address?.startsWith("fc00") != true && // Exclude unique local addresses
                    address?.startsWith("fd00") != true // Exclude unique local addresses
        }
        .filter { address ->
            when (ipType) {
                IpType.IPv4 -> address?.contains(":") == false
                IpType.IPv6 -> address?.contains(":") == true
                IpType.All -> true
            }
        }
        .toList()
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