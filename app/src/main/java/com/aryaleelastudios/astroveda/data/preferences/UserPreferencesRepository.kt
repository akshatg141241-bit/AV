package com.aryaleelastudios.astroveda.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aryaleelastudios.astroveda.data.model.AyanamsaType
import com.aryaleelastudios.astroveda.data.model.ChartStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.astroVedaDataStore: DataStore<Preferences> by preferencesDataStore(name = "astroveda_preferences")

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.applicationContext.astroVedaDataStore)

    private object PreferencesKeys {
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val LANGUAGE = stringPreferencesKey("language")
        val USER_NAME = stringPreferencesKey("user_name")
        val CHART_STYLE = stringPreferencesKey("chart_style")
        val AYANAMSA = stringPreferencesKey("ayanamsa")
        val SELECTED_PERSON_ID = longPreferencesKey("selected_person_id")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")
        val NOTIFICATION_MINUTE = intPreferencesKey("notification_minute")
        val NOTIFICATION_PERSON_ID = longPreferencesKey("notification_person_id")
        val IS_PREMIUM_UNLOCKED = booleanPreferencesKey("is_premium_unlocked")
    }

    val onboardingCompleteFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ONBOARDING_COMPLETE] ?: false
    }

    val languageFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LANGUAGE] ?: "en"
    }

    val userNameFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_NAME] ?: ""
    }

    val chartStyleFlow: Flow<ChartStyle> = dataStore.data.map { preferences ->
        val raw = preferences[PreferencesKeys.CHART_STYLE] ?: ChartStyle.NORTH_INDIAN.name
        runCatching { ChartStyle.valueOf(raw) }.getOrDefault(ChartStyle.NORTH_INDIAN)
    }

    val ayanamsaFlow: Flow<AyanamsaType> = dataStore.data.map { preferences ->
        val raw = preferences[PreferencesKeys.AYANAMSA] ?: AyanamsaType.LAHIRI.name
        runCatching { AyanamsaType.valueOf(raw) }.getOrDefault(AyanamsaType.LAHIRI)
    }

    val selectedPersonIdFlow: Flow<Long?> = dataStore.data.map { preferences ->
        val id = preferences[PreferencesKeys.SELECTED_PERSON_ID] ?: -1L
        if (id > 0) id else null
    }

    val notificationsEnabledFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
    }

    val notificationTimeFlow: Flow<Pair<Int, Int>> = dataStore.data.map { preferences ->
        val hour = preferences[PreferencesKeys.NOTIFICATION_HOUR] ?: 6
        val minute = preferences[PreferencesKeys.NOTIFICATION_MINUTE] ?: 0
        Pair(hour, minute)
    }

    val notificationPersonIdFlow: Flow<Long?> = dataStore.data.map { preferences ->
        val id = preferences[PreferencesKeys.NOTIFICATION_PERSON_ID] ?: -1L
        if (id > 0) id else null
    }

    // Unused monetization flag for future one-time unlock (v1 is completely free)
    val isPremiumUnlockedFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_PREMIUM_UNLOCKED] ?: true
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETE] = complete }
    }

    suspend fun setLanguage(lang: String) {
        dataStore.edit { it[PreferencesKeys.LANGUAGE] = lang }
    }

    suspend fun setUserName(name: String) {
        dataStore.edit { it[PreferencesKeys.USER_NAME] = name }
    }

    suspend fun setChartStyle(style: ChartStyle) {
        dataStore.edit { it[PreferencesKeys.CHART_STYLE] = style.name }
    }

    suspend fun setAyanamsa(ayanamsa: AyanamsaType) {
        dataStore.edit { it[PreferencesKeys.AYANAMSA] = ayanamsa.name }
    }

    suspend fun setSelectedPersonId(personId: Long?) {
        dataStore.edit { it[PreferencesKeys.SELECTED_PERSON_ID] = personId ?: -1L }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setNotificationTime(hour: Int, minute: Int) {
        dataStore.edit {
            it[PreferencesKeys.NOTIFICATION_HOUR] = hour
            it[PreferencesKeys.NOTIFICATION_MINUTE] = minute
        }
    }

    suspend fun setNotificationPersonId(personId: Long?) {
        dataStore.edit { it[PreferencesKeys.NOTIFICATION_PERSON_ID] = personId ?: -1L }
    }

    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
