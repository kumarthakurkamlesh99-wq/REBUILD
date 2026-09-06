package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rebuild_settings")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        const val PREFS_NAME = "rebuild_settings"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    private val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey(KEY_ONBOARDING_COMPLETED)
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val USER_NAME = stringPreferencesKey("user_name")
        val GOAL_HOURS = stringPreferencesKey("goal_hours")
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
    }

    /**
     * Synchronous check of the single persistent onboarding flag (source of truth).
     * Instant check with 0ms latency for splash navigation.
     */
    fun isOnboardingCompletedSync(): Boolean {
        return sharedPrefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    /**
     * Synchronous permanent commit of the onboarding completion flag.
     */
    fun setOnboardingCompletedSync(completed: Boolean) {
        sharedPrefs.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, completed)
            .commit()
    }

    /**
     * Reactive flow of the single persistent onboarding flag.
     */
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ONBOARDING_COMPLETED]
            ?: sharedPrefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    /**
     * Save onboarding completion status permanently to both SharedPreferences and DataStore.
     */
    suspend fun setOnboardingCompleted(completed: Boolean) {
        setOnboardingCompletedSync(completed)
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
            }
        } catch (e: Exception) {
            // Persistent storage via sharedPrefs is already committed
        }
    }

    /**
     * Reset onboarding state when app data is cleared or for testing.
     */
    suspend fun clearOnboardingState() {
        sharedPrefs.edit().remove(KEY_ONBOARDING_COMPLETED).commit()
        try {
            context.dataStore.edit { preferences ->
                preferences.remove(PreferencesKeys.ONBOARDING_COMPLETED)
            }
        } catch (e: Exception) {
            // Ignored
        }
    }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DARK_THEME] ?: true // Default dark liquid glass theme
    }

    val isNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
    }

    val isSoundEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SOUND_ENABLED] ?: true
    }

    val isVibrationEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true
    }

    val userName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_NAME] ?: "Agent"
    }

    val dailyGoalHours: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.GOAL_HOURS] ?: "6"
    }

    val geminiApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.GEMINI_API_KEY] ?: ""
    }

    suspend fun setGeminiApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GEMINI_API_KEY] = key
        }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_THEME] = enabled
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
        }
    }

    suspend fun setDailyGoalHours(hours: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GOAL_HOURS] = hours
        }
    }
}
