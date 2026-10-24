package com.lifeguard.app.service

import android.content.Intent
import android.net.VpnService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.ParcelFileDescriptor

class LifeGuardVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1001, notification())
        if (vpnInterface == null) {
            vpnInterface = Builder()
                .setSession("LifeGuard DNS Filter")
                .addAddress("10.8.0.2", 32)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("10.8.0.1")
                .establish()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        vpnInterface?.close()
        vpnInterface = null
        super.onDestroy()
    }

    private fun notification(): Notification {
        val channelId = "lifeguard_protection"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(NotificationChannel(channelId, "LifeGuard protection", NotificationManager.IMPORTANCE_LOW))
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId).setContentTitle("LifeGuard protection").setContentText("Local DNS filtering is active").setSmallIcon(android.R.drawable.ic_lock_lock).build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this).setContentTitle("LifeGuard protection").setContentText("Local DNS filtering is active").setSmallIcon(android.R.drawable.ic_lock_lock).build()
        }
    }
}
