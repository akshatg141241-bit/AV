package com.aryaleelastudios.kundaliguru.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aryaleelastudios.kundaliguru.astro.AstroEngine
import com.aryaleelastudios.kundaliguru.astro.PredictionsRepository
import com.aryaleelastudios.kundaliguru.data.local.KundaliDatabase
import com.aryaleelastudios.kundaliguru.data.local.PersonEntity
import com.aryaleelastudios.kundaliguru.data.model.AyanamsaType
import com.aryaleelastudios.kundaliguru.data.model.ChartStyle
import com.aryaleelastudios.kundaliguru.data.model.KundaliData
import com.aryaleelastudios.kundaliguru.data.preferences.UserPreferencesRepository
import com.aryaleelastudios.kundaliguru.notifications.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class KundaliViewModel(application: Application) : AndroidViewModel(application) {

    private val db = KundaliDatabase.getDatabase(application)
    private val personDao = db.personDao()
    private val prefs = UserPreferencesRepository(application)
    val predictionsRepo = PredictionsRepository(application)

    val isOnboarded: StateFlow<Boolean?> = prefs.isOnboardedFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val language: StateFlow<String> = prefs.languageFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "en"
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

    val allPersons: StateFlow<List<PersonEntity>> = personDao.getAllPersons().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val selectedPersonId: StateFlow<Long?> = prefs.selectedPersonIdFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    // Current selected Person
    val selectedPerson: StateFlow<PersonEntity?> = combine(allPersons, selectedPersonId) { persons, selectedId ->
        if (persons.isEmpty()) null
        else if (selectedId != null) persons.find { it.id == selectedId } ?: persons.first()
        else persons.first()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Computed Kundali data for selected person
    val kundaliData: StateFlow<KundaliData?> = combine(selectedPerson, ayanamsa) { person, ayanamsaType ->
        if (person == null) null
        else {
            AstroEngine.computeKundali(
                birthDateStr = person.birthDate,
                birthTimeStr = person.birthTime,
                isTimeUnknown = person.isTimeUnknown,
                lat = person.latitude,
                lon = person.longitude,
                tzId = person.timezoneId,
                ayanamsaType = ayanamsaType,
                predictionsRepo = predictionsRepo
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun completeOnboarding(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setUserName(name)
            prefs.setOnboarded(true)
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
            onSaved(id)
        }
    }

    fun duplicatePerson(person: PersonEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val copy = person.copy(
                id = 0,
                name = "${person.name} (Copy)",
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

    fun updateNotifications(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setNotificationsEnabled(enabled)
            prefs.setNotificationTime(hour, minute)
            if (enabled) {
                NotificationHelper.scheduleDailyNotification(getApplication(), hour, minute)
            } else {
                NotificationHelper.cancelDailyNotification(getApplication())
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            personDao.clearAll()
            prefs.clearAll()
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
            put("app", "Kundali Guru")
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
                        birthPlace = obj.optString("birthPlace", "Varanasi, Uttar Pradesh"),
                        latitude = obj.optDouble("latitude", 25.3176),
                        longitude = obj.optDouble("longitude", 82.9739),
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
            } else false
        } catch (e: Exception) {
            false
        }
    }
}
