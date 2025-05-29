package com.example.fedger.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Define ThemeSetting enum
enum class ThemeSetting {
    LIGHT, DARK, SYSTEM
}

// Create DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemePreferenceManager(private val context: Context) {

    // Define Preferences Key for theme setting
    private val themePreferenceKey = stringPreferencesKey("theme_preference")

    // Expose theme setting Flow
    val themeSetting: Flow<ThemeSetting> = context.dataStore.data
        .map { preferences ->
            when (preferences[themePreferenceKey]) {
                ThemeSetting.LIGHT.name -> ThemeSetting.LIGHT
                ThemeSetting.DARK.name -> ThemeSetting.DARK
                else -> ThemeSetting.SYSTEM // Default to SYSTEM
            }
        }

    // Save theme setting
    suspend fun setThemeSetting(setting: ThemeSetting) {
        context.dataStore.edit { preferences ->
            preferences[themePreferenceKey] = setting.name
        }
    }
}
