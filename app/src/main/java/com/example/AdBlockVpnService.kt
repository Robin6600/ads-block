package com.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class AdBlockVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isRunning = false

    private var dnsProxy: com.example.vpn.DnsProxy? = null
    
    companion object {
        const val ACTION_START = "com.example.START_VPN"
        const val ACTION_STOP = "com.example.STOP_VPN"
        const val CHANNEL_ID = "AdBlockVpnChannel"
        const val NOTIFICATION_ID = 101
        
        var isVpnActive = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        try {
            createNotificationChannel()
        } catch (e: Exception) {
            Log.e("AdBlockVpn", "Failed to create channel", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            val action = intent?.action
            if (action == ACTION_STOP) {
                stopVpn()
                return START_NOT_STICKY
            }

            if (!isRunning) {
                // Start Foreground immediately
                val notification = createNotification()
                try {
                    if (Build.VERSION.SDK_INT >= 34) {
                        startForeground(
                            NOTIFICATION_ID, 
                            notification, 
                            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED
                        )
                    } else {
                        startForeground(NOTIFICATION_ID, notification)
                    }
                } catch (e: Exception) {
                    Log.e("AdBlockVpn", "Failed to startForeground", e)
                }
                
                startVpn()
            }
        } catch (e: Exception) {
            Log.e("AdBlockVpn", "Error in onStartCommand", e)
        }
        
        return START_STICKY
    }

    private fun startVpn() {
        try {
            val builder = Builder()
            builder.setSession("Disturb Robin")
            
            // Dummy address
            builder.addAddress("10.0.0.2", 32)
            
            // System will send DNS requests to this dummy IP
            builder.addDnsServer("10.0.0.3")
            
            // We ONLY route traffic destined for our dummy DNS server to the VPN.
            // This cleanly allows all other traffic to bypass the VPN and work natively!
            builder.addRoute("10.0.0.3", 32)
            
            val prefs = getSharedPreferences("adblock_prefs", Context.MODE_PRIVATE)
            val bypassed = prefs.getStringSet("bypassed_apps", emptySet()) ?: emptySet()
            for (packageName in bypassed) {
                try {
                    builder.addDisallowedApplication(packageName)
                } catch (e: Exception) {
                    Log.w("AdBlockVpn", "Cannot bypass app", e)
                }
            }

            vpnInterface = builder.establish()
            isRunning = true
            isVpnActive = true
            Log.d("AdBlockVpn", "VPN established.")
            
            dnsProxy = com.example.vpn.DnsProxy(this, prefs)
            
            val exceptionHandler = CoroutineExceptionHandler { _, exception ->
                Log.e("AdBlockVpn", "Unhandled coroutine exception", exception)
            }
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + exceptionHandler)
            scope.launch {
                val pfd = vpnInterface ?: return@launch
                val inputStream = java.io.FileInputStream(pfd.fileDescriptor)
                val outputStream = java.io.FileOutputStream(pfd.fileDescriptor)
                val buffer = ByteArray(32767)
                
                try {
                    while (isActive && isRunning) {
                        val length = inputStream.read(buffer)
                        if (length > 0) {
                            val packet = buffer.copyOf(length)
                            scope.launch {
                                try {
                                    dnsProxy?.handlePacket(packet, outputStream)
                                } catch (e: Exception) {
                                    Log.e("AdBlockVpn", "Error handling packet", e)
                                }
                            }
                        } else if (length < 0) {
                            Log.d("AdBlockVpn", "EOF reached. Breaking tunnel read loop.")
                            break
                        } else {
                            // length == 0. Yield to prevent any tight-spinning CPU usage in non-blocking settings
                            yield()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AdBlockVpn", "Tunnel error", e)
                }
            }
        } catch (e: Exception) {
            Log.e("AdBlockVpn", "Failed to establish VPN", e)
            stopVpn()
        }
    }

    private fun stopVpn() {
        isRunning = false
        isVpnActive = false
        dnsProxy?.flushBlockedCounter()
        scope.cancel()
        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            Log.e("AdBlockVpn", "Error closing VPN interface", e)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ad Blocker Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the ad blocker running in the background"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val stopIntent = Intent(this, AdBlockVpnService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🛡️ Disturb Robin Active")
            .setContentText("All ads are being blocked right now")
            .setSmallIcon(android.R.drawable.ic_secure) // Use placeholder
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
    }
}
