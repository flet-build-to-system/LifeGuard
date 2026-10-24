package com.lifeguard.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lifeguard.app.data.FocusSessionEntity
import com.lifeguard.app.data.LifeGuardDatabase
import com.lifeguard.app.data.PreferencesRepository
import com.lifeguard.app.data.UsageRepository
import com.lifeguard.app.model.AppState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LifeGuardViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = PreferencesRepository(application)
    private val dao = LifeGuardDatabase.get(application).dao()
    private val usageRepository = UsageRepository(application)
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()
    private var timerJob: Job? = null

    init {
        viewModelScope.launch { preferences.state.collect { _state.value = it.copy(focusRemainingSeconds = _state.value.focusRemainingSeconds, focusRunning = _state.value.focusRunning) } }
    }

    fun setLanguage(language: String) = patch { copy(language = language) }
    fun setDarkTheme(enabled: Boolean) = patch { copy(darkTheme = enabled) }
    fun setProtectionEnabled(enabled: Boolean) = patch { copy(protectionEnabled = enabled) }
    fun setProtectionProfile(profile: String) = patch { copy(protectionProfile = profile) }

    fun startFocus() {
        if (timerJob?.isActive == true) return
        patch { copy(focusRunning = true) }
        timerJob = viewModelScope.launch {
            while (_state.value.focusRemainingSeconds > 0) {
                delay(1_000)
                patch { copy(focusRemainingSeconds = focusRemainingSeconds - 1) }
            }
            completeFocus()
        }
    }

    fun pauseFocus() { timerJob?.cancel(); timerJob = null; patch { copy(focusRunning = false) } }
    fun resetFocus() { timerJob?.cancel(); timerJob = null; patch { copy(focusRunning = false, focusRemainingSeconds = 25 * 60) } }

    private fun completeFocus() {
        timerJob?.cancel(); timerJob = null
        patch { copy(focusRunning = false, focusRemainingSeconds = 25 * 60, completedSessions = completedSessions + 1) }
        viewModelScope.launch { dao.insertFocusSession(FocusSessionEntity(startedAt = System.currentTimeMillis(), durationSeconds = 25 * 60, completed = true)) }
    }

    fun usageAccessGranted(): Boolean = usageRepository.hasUsageAccess()
    fun todayScreenMinutes(): Int? = usageRepository.readToday().screenTimeMinutes

    fun resetLocalData() {
        timerJob?.cancel()
        _state.value = AppState(language = _state.value.language, darkTheme = _state.value.darkTheme)
        viewModelScope.launch { preferences.update { prefs -> prefs.clear() } }
    }

    private fun patch(change: AppState.() -> AppState) {
        val next = _state.value.change()
        _state.value = next
        viewModelScope.launch {
            preferences.update { prefs ->
                prefs[androidx.datastore.preferences.core.stringPreferencesKey("language")] = next.language
                prefs[androidx.datastore.preferences.core.booleanPreferencesKey("dark_theme")] = next.darkTheme
                prefs[androidx.datastore.preferences.core.booleanPreferencesKey("protection_enabled")] = next.protectionEnabled
                prefs[androidx.datastore.preferences.core.stringPreferencesKey("protection_profile")] = next.protectionProfile
                prefs[androidx.datastore.preferences.core.intPreferencesKey("completed_sessions")] = next.completedSessions
                prefs[androidx.datastore.preferences.core.intPreferencesKey("blocked_attempts")] = next.blockedAttempts
            }
        }
    }
}
