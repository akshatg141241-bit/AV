package com.aryaleelastudios.kundaliguru.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aryaleelastudios.kundaliguru.data.model.AyanamsaType
import com.aryaleelastudios.kundaliguru.data.model.ChartStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
        val LANGUAGE = stringPreferencesKey("language")
        val USER_NAME = stringPreferencesKey("user_name")
        val CHART_STYLE = stringPreferencesKey("chart_style")
        val AYANAMSA = stringPreferencesKey("ayanamsa")
        val SELECTED_PERSON_ID = longPreferencesKey("selected_person_id")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")
        val NOTIFICATION_MINUTE = intPreferencesKey("notification_minute")
        val NOTIFICATION_PERSON_ID = longPreferencesKey("notification_person_id")
    }

    val isOnboardedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_ONBOARDED] ?: false
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LANGUAGE] ?: "en"
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_NAME] ?: ""
    }

    val chartStyleFlow: Flow<ChartStyle> = context.dataStore.data.map { preferences ->
        val raw = preferences[PreferencesKeys.CHART_STYLE] ?: ChartStyle.NORTH_INDIAN.name
        runCatching { ChartStyle.valueOf(raw) }.getOrDefault(ChartStyle.NORTH_INDIAN)
    }

    val ayanamsaFlow: Flow<AyanamsaType> = context.dataStore.data.map { preferences ->
        val raw = preferences[PreferencesKeys.AYANAMSA] ?: AyanamsaType.LAHIRI.name
        runCatching { AyanamsaType.valueOf(raw) }.getOrDefault(AyanamsaType.LAHIRI)
    }

    val selectedPersonIdFlow: Flow<Long?> = context.dataStore.data.map { preferences ->
        val id = preferences[PreferencesKeys.SELECTED_PERSON_ID] ?: -1L
        if (id > 0) id else null
    }

    val notificationsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
    }

    val notificationTimeFlow: Flow<Pair<Int, Int>> = context.dataStore.data.map { preferences ->
        val hour = preferences[PreferencesKeys.NOTIFICATION_HOUR] ?: 6
        val minute = preferences[PreferencesKeys.NOTIFICATION_MINUTE] ?: 0
        Pair(hour, minute)
    }

    val notificationPersonIdFlow: Flow<Long?> = context.dataStore.data.map { preferences ->
        val id = preferences[PreferencesKeys.NOTIFICATION_PERSON_ID] ?: -1L
        if (id > 0) id else null
    }

    suspend fun setOnboarded(onboarded: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.IS_ONBOARDED] = onboarded }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[PreferencesKeys.LANGUAGE] = lang }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[PreferencesKeys.USER_NAME] = name }
    }

    suspend fun setChartStyle(style: ChartStyle) {
        context.dataStore.edit { it[PreferencesKeys.CHART_STYLE] = style.name }
    }

    suspend fun setAyanamsa(ayanamsa: AyanamsaType) {
        context.dataStore.edit { it[PreferencesKeys.AYANAMSA] = ayanamsa.name }
    }

    suspend fun setSelectedPersonId(personId: Long?) {
        context.dataStore.edit { it[PreferencesKeys.SELECTED_PERSON_ID] = personId ?: -1L }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setNotificationTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[PreferencesKeys.NOTIFICATION_HOUR] = hour
            it[PreferencesKeys.NOTIFICATION_MINUTE] = minute
        }
    }

    suspend fun setNotificationPersonId(personId: Long?) {
        context.dataStore.edit { it[PreferencesKeys.NOTIFICATION_PERSON_ID] = personId ?: -1L }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
