package com.lifeguard.app.model

data class AppState(
    val language: String = "ar",
    val darkTheme: Boolean = true,
    val protectionEnabled: Boolean = true,
    val protectionProfile: String = "balanced",
    val focusRunning: Boolean = false,
    val focusRemainingSeconds: Int = 25 * 60,
    val completedSessions: Int = 4,
    val blockedAttempts: Int = 7,
)

data class DashboardSnapshot(
    val screenTimeMinutes: Int? = null,
    val unlocks: Int? = null,
    val focusMinutes: Int = 0,
    val blockedAttempts: Int = 0,
    val usagePermissionGranted: Boolean = false,
)

data class FocusTask(val title: String, val durationMinutes: Int, val done: Boolean = false)
