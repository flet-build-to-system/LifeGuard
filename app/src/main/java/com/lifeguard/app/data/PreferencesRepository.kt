package com.lifeguard.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lifeguard.app.model.AppState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.lifeGuardDataStore by preferencesDataStore("lifeguard_preferences")

class PreferencesRepository(private val context: Context) {
    private object Keys {
        val language = stringPreferencesKey("language")
        val darkTheme = booleanPreferencesKey("dark_theme")
        val protectionEnabled = booleanPreferencesKey("protection_enabled")
        val protectionProfile = stringPreferencesKey("protection_profile")
        val completedSessions = intPreferencesKey("completed_sessions")
        val blockedAttempts = intPreferencesKey("blocked_attempts")
    }

    val state: Flow<AppState> = context.lifeGuardDataStore.data.map { prefs ->
        AppState(
            language = prefs[Keys.language] ?: "ar",
            darkTheme = prefs[Keys.darkTheme] ?: true,
            protectionEnabled = prefs[Keys.protectionEnabled] ?: true,
            protectionProfile = prefs[Keys.protectionProfile] ?: "balanced",
            completedSessions = prefs[Keys.completedSessions] ?: 4,
            blockedAttempts = prefs[Keys.blockedAttempts] ?: 7,
        )
    }

    suspend fun update(transform: (MutablePreferences) -> Unit) {
        context.lifeGuardDataStore.edit(transform)
    }
}
