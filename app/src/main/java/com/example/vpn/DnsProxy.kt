package com.example.vpn

import android.content.Context
import android.content.SharedPreferences
import android.net.VpnService
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DnsProxy(private val vpnService: VpnService, private val prefs: SharedPreferences) {

    private val adguardDnsIp = "94.140.14.14"
    private val writeMutex = Mutex()
    
    suspend fun handlePacket(packet: ByteArray, outputStream: FileOutputStream) = withContext(Dispatchers.IO) {
        // Minimum check for IPv4
        if (packet.size < 20) return@withContext
        val version = (packet[0].toInt() and 0xF0) ushr 4
        if (version != 4) return@withContext
        val ihl = (packet[0].toInt() and 0x0F) * 4
        if (ihl < 20 || packet.size < ihl + 8) return@withContext
        
        // Protocol must be UDP
        val protocol = packet[9].toInt() and 0xFF
        if (protocol != 17) return@withContext
        
        // Dest port must be 53
        val destPort = ((packet[ihl + 2].toInt() and 0xFF) shl 8) or (packet[ihl + 3].toInt() and 0xFF)
        if (destPort != 53) return@withContext
        
        val udpLen = ((packet[ihl + 4].toInt() and 0xFF) shl 8) or (packet[ihl + 5].toInt() and 0xFF)
        val dnsPayloadLen = udpLen - 8
        if (dnsPayloadLen <= 0 || ihl + 8 + dnsPayloadLen > packet.size) return@withContext
        
        val dnsPayload = ByteArray(dnsPayloadLen)
        System.arraycopy(packet, ihl + 8, dnsPayload, 0, dnsPayloadLen)
        
        try {
            DatagramSocket().use { socket ->
                vpnService.protect(socket)
                val ip = InetAddress.getByName(adguardDnsIp)
                val sendPacket = DatagramPacket(dnsPayload, dnsPayload.size, ip, 53)
                socket.soTimeout = 2000
                socket.send(sendPacket)
                
                val recvBuffer = ByteArray(4096)
                val recvPacket = DatagramPacket(recvBuffer, recvBuffer.size)
                socket.receive(recvPacket)
                
                val responsePayload = recvBuffer.copyOf(recvPacket.length)
                
                // Check if blocked (0.0.0.0 or NXDOMAIN)
                if (isAdBlocked(responsePayload)) {
                    incrementBlockedCounter()
                }
                
                val responseIpPacket = buildDnsResponsePacket(packet, responsePayload, ihl)
                if (responseIpPacket.isNotEmpty()) {
                    writeMutex.withLock {
                        outputStream.write(responseIpPacket)
                    }
                }
            }
        } catch (e: Exception) {
            // Can ignore timeout exceptions silently
        }
    }

    private fun isAdBlocked(response: ByteArray): Boolean {
        if (response.size < 4) return false
        val flags = ((response[2].toInt() and 0xFF) shl 8) or (response[3].toInt() and 0xFF)
        if ((flags and 0x0F) == 3) {
            return true
        }
        for (i in 0 until response.size - 3) {
            if (response[i].toInt() == 0 && response[i+1].toInt() == 0 && response[i+2].toInt() == 0 && response[i+3].toInt() == 0) {
                if (i >= 2 && response[i-2].toInt() == 0 && response[i-1].toInt() == 4) {
                    return true
                }
            }
        }
        return false
    }

    private fun incrementBlockedCounter() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayKey = "ads_blocked_$today"
        val totalKey = "ads_blocked_total"
        
        synchronized(prefs) {
            val currentToday = prefs.getInt(todayKey, 0)
            val currentTotal = prefs.getInt(totalKey, 0)
            prefs.edit()
                .putInt(todayKey, currentToday + 1)
                .putInt(totalKey, currentTotal + 1)
                .apply()
        }
    }
    
    fun flushBlockedCounter() {
        // Instant updates used, flush is no-op
    }

    private fun buildDnsResponsePacket(requestPacket: ByteArray, dnsResponse: ByteArray, ihl: Int): ByteArray {
        val destIpOffset = 16
        val srcIpOffset = 12
        val destPortOffset = ihl + 2
        val srcPortOffset = ihl
        
        val response = ByteArray(20 + 8 + dnsResponse.size)
        
        // IPv4 Header
        response[0] = 0x45
        response[1] = 0x00
        val totalLen = response.size
        response[2] = (totalLen shr 8).toByte()
        response[3] = totalLen.toByte()
        response[4] = requestPacket[4] // ID
        response[5] = requestPacket[5]
        response[6] = 0x00 // Flags
        response[7] = 0x00
        response[8] = 64 // TTL
        response[9] = 17 // UDP
        
        // Checksum 0
        response[10] = 0x00
        response[11] = 0x00
        
        // Swap IPs
        System.arraycopy(requestPacket, destIpOffset, response, srcIpOffset, 4)
        System.arraycopy(requestPacket, srcIpOffset, response, destIpOffset, 4)
        
        // Compute checksum
        var sum = 0
        for (i in 0 until 10) {
            val word = ((response[i * 2].toInt() and 0xFF) shl 8) or (response[i * 2 + 1].toInt() and 0xFF)
            sum += word
        }
        while ((sum shr 16) > 0) {
            sum = (sum and 0xFFFF) + (sum shr 16)
        }
        sum = sum.inv() and 0xFFFF
        response[10] = (sum shr 8).toByte()
        response[11] = sum.toByte()
        
        // UDP Header
        System.arraycopy(requestPacket, destPortOffset, response, 20, 2)
        System.arraycopy(requestPacket, srcPortOffset, response, 22, 2)
        val udpLen = 8 + dnsResponse.size
        response[24] = (udpLen shr 8).toByte()
        response[25] = udpLen.toByte()
        response[26] = 0x00 // No UDP checksum
        response[27] = 0x00
        
        // DNS Payload
        System.arraycopy(dnsResponse, 0, response, 28, dnsResponse.size)
        return response
    }
}
