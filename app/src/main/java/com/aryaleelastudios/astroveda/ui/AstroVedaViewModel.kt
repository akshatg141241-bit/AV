package com.aryaleelastudios.astroveda.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aryaleelastudios.astroveda.astro.AstroEngine
import com.aryaleelastudios.astroveda.data.local.PersonDao
import com.aryaleelastudios.astroveda.data.local.PersonEntity
import com.aryaleelastudios.astroveda.data.model.AyanamsaType
import com.aryaleelastudios.astroveda.data.model.ChartStyle
import com.aryaleelastudios.astroveda.data.model.KundaliData
import com.aryaleelastudios.astroveda.data.model.YearForecast
import com.aryaleelastudios.astroveda.data.preferences.UserPreferencesRepository
import com.aryaleelastudios.astroveda.data.repository.ContentRepository
import com.aryaleelastudios.astroveda.notifications.NotificationHelper
import com.aryaleelastudios.astroveda.pdf.PdfExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class AstroVedaViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val personDao: PersonDao,
    private val prefs: UserPreferencesRepository,
    val contentRepo: ContentRepository
) : ViewModel() {

    val onboardingComplete: StateFlow<Boolean?> = prefs.onboardingCompleteFlow.stateIn(
        viewModelScope, SharingStarted.Eagerly, null
    )

    val language: StateFlow<String> = prefs.languageFlow.stateIn(
        viewModelScope, SharingStarted.Eagerly, "en"
    )

    val userName: StateFlow<String> = prefs.userNameFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ""
    )

    val chartStyle: StateFlow<ChartStyle> = prefs.chartStyleFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ChartStyle.NORTH_INDIAN
    )

    val ayanamsa: StateFlow<AyanamsaType> = prefs.ayanamsaFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), AyanamsaType.LAHIRI
    )

    val notificationsEnabled: StateFlow<Boolean> = prefs.notificationsEnabledFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )

    val notificationTime: StateFlow<Pair<Int, Int>> = prefs.notificationTimeFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(6, 0)
    )

    val notificationPersonId: StateFlow<Long?> = prefs.notificationPersonIdFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    // Unused monetization flag preserved for future releases
    val isPremiumUnlocked: StateFlow<Boolean> = prefs.isPremiumUnlockedFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )

    val allPersons: StateFlow<List<PersonEntity>> = personDao.getAllPersons().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val selectedPersonId: StateFlow<Long?> = prefs.selectedPersonIdFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val selectedPerson: StateFlow<PersonEntity?> = combine(allPersons, selectedPersonId) { persons, selectedId ->
        if (persons.isEmpty()) null
        else if (selectedId != null) persons.find { it.id == selectedId } ?: persons.first()
        else persons.first()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val kundaliData: StateFlow<KundaliData?> = combine(
        selectedPerson,
        ayanamsa,
        language
    ) { person, ayanamsaType, langCode ->
        if (person == null) {
            null
        } else {
            AstroEngine.computeKundali(
                birthDateStr = person.birthDate,
                birthTimeStr = person.birthTime,
                isTimeUnknown = person.isTimeUnknown,
                lat = person.latitude,
                lon = person.longitude,
                tzId = person.timezoneId,
                ayanamsaType = ayanamsaType,
                langCode = langCode,
                contentRepo = contentRepo,
                person = person
            )
        }
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val year2027Forecast: StateFlow<YearForecast?> = combine(
        selectedPerson,
        kundaliData,
        ayanamsa,
        language
    ) { person, kundali, ayanamsaType, langCode ->
        if (person == null) {
            null
        } else {
            AstroEngine.computeYearForecast(
                person = person,
                year = 2027,
                ayanamsaType = ayanamsaType,
                langCode = langCode,
                contentRepo = contentRepo,
                kundali = kundali
            )
        }
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val personRashis: StateFlow<Map<Long, String>> = combine(
        allPersons,
        ayanamsa,
        language
    ) { persons, ayanamsaType, langCode ->
        persons.associate { person ->
            val kundali = AstroEngine.computeKundali(
                birthDateStr = person.birthDate,
                birthTimeStr = person.birthTime,
                isTimeUnknown = person.isTimeUnknown,
                lat = person.latitude,
                lon = person.longitude,
                tzId = person.timezoneId,
                ayanamsaType = ayanamsaType,
                langCode = langCode,
                contentRepo = contentRepo,
                person = person
            )
            val moonRashi = kundali.planets.firstOrNull { it.canonicalName == "Moon" }?.rashiName
                ?: kundali.lagna.rashiName
            person.id to moonRashi
        }
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    suspend fun exportMasterPdf(
        context: Context,
        uri: Uri,
        person: PersonEntity,
        kundali: KundaliData,
        chartStyle: ChartStyle,
        langCode: String,
        yearForecast: YearForecast? = year2027Forecast.value
    ): Boolean = withContext(Dispatchers.IO) {
        PdfExporter.exportToUri(
            context = context,
            uri = uri,
            person = person,
            kundali = kundali,
            langCode = langCode,
            chartStyle = chartStyle,
            yearForecast = yearForecast
        )
    }

    fun completeOnboarding(name: String, onCompleted: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setUserName(name)
            prefs.setOnboardingComplete(true)
            withContext(Dispatchers.Main) {
                onCompleted()
            }
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setLanguage(lang)
        }
    }

    fun setUserName(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setUserName(name)
        }
    }

    fun setChartStyle(style: ChartStyle) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setChartStyle(style)
        }
    }

    fun setAyanamsa(type: AyanamsaType) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setAyanamsa(type)
        }
    }

    fun selectPerson(person: PersonEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setSelectedPersonId(person.id)
        }
    }

    fun savePerson(person: PersonEntity, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = if (person.id == 0L) {
                personDao.insertPerson(person)
            } else {
                personDao.updatePerson(person)
                person.id
            }
            prefs.setSelectedPersonId(id)
            withContext(Dispatchers.Main) {
                onSaved(id)
            }
        }
    }

    fun duplicatePerson(person: PersonEntity, duplicatedName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val copy = person.copy(
                id = 0,
                name = duplicatedName,
                createdAt = System.currentTimeMillis()
            )
            val newId = personDao.insertPerson(copy)
            prefs.setSelectedPersonId(newId)
        }
    }

    fun deletePerson(person: PersonEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            personDao.deletePerson(person)
            val remaining = personDao.getAllPersonsList()
            if (remaining.isNotEmpty()) {
                prefs.setSelectedPersonId(remaining.first().id)
            } else {
                prefs.setSelectedPersonId(null)
            }
        }
    }

    fun updateNotifications(enabled: Boolean, hour: Int, minute: Int, notifPersonId: Long? = notificationPersonId.value) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setNotificationsEnabled(enabled)
            prefs.setNotificationTime(hour, minute)
            prefs.setNotificationPersonId(notifPersonId)
            if (enabled) {
                NotificationHelper.scheduleDailyNotification(appContext, hour, minute)
            } else {
                NotificationHelper.cancelDailyNotification(appContext)
            }
        }
    }

    fun updateNotificationPerson(personId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setNotificationPersonId(personId)
        }
    }

    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            personDao.clearAll()
            prefs.clearAll()
            NotificationHelper.cancelDailyNotification(appContext)
        }
    }

    fun exportAllPersonsJson(): String {
        val persons = allPersons.value
        val jsonArray = JSONArray()
        persons.forEach { p ->
            val obj = JSONObject().apply {
                put("name", p.name)
                put("gender", p.gender)
                put("birthDate", p.birthDate)
                put("birthTime", p.birthTime)
                put("isTimeUnknown", p.isTimeUnknown)
                put("birthPlace", p.birthPlace)
                put("latitude", p.latitude)
                put("longitude", p.longitude)
                put("timezoneId", p.timezoneId)
                put("maritalStatus", p.maritalStatus)
                put("occupation", p.occupation)
            }
            jsonArray.put(obj)
        }
        val root = JSONObject().apply {
            put("app", "AstroVeda")
            put("version", "1.0")
            put("exportedAt", System.currentTimeMillis())
            put("persons", jsonArray)
        }
        return root.toString(2)
    }

    fun importPersonsFromJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            val array = root.optJSONArray("persons") ?: JSONArray()
            val list = mutableListOf<PersonEntity>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    PersonEntity(
                        id = 0,
                        name = obj.optString("name", "Unknown"),
                        gender = obj.optString("gender", "Prefer not to say"),
                        birthDate = obj.optString("birthDate", "1995-01-01"),
                        birthTime = obj.optString("birthTime", "12:00"),
                        isTimeUnknown = obj.optBoolean("isTimeUnknown", false),
                        birthPlace = obj.optString("birthPlace", "New Delhi, Delhi"),
                        latitude = obj.optDouble("latitude", 28.6139),
                        longitude = obj.optDouble("longitude", 77.2090),
                        timezoneId = obj.optString("timezoneId", "Asia/Kolkata"),
                        maritalStatus = obj.optString("maritalStatus", "Single"),
                        occupation = obj.optString("occupation", "Other")
                    )
                )
            }
            if (list.isNotEmpty()) {
                viewModelScope.launch(Dispatchers.IO) {
                    personDao.insertAll(list)
                    val all = personDao.getAllPersonsList()
                    if (all.isNotEmpty()) {
                        prefs.setSelectedPersonId(all.first().id)
                    }
                }
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }
}
