package com.lifeguard.app.data

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import android.os.Build
import com.lifeguard.app.model.DashboardSnapshot

class UsageRepository(private val context: Context) {
    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun readToday(): DashboardSnapshot {
        if (!hasUsageAccess()) return DashboardSnapshot(usagePermissionGranted = false)
        val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val start = end - 24 * 60 * 60 * 1000L
        val total = manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
            .sumOf { (it.totalTimeInForeground / 60_000L).toInt() }
        return DashboardSnapshot(screenTimeMinutes = total, usagePermissionGranted = true)
    }
}
