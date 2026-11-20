package com.aryaleelastudios.kundaliguru.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.InvalidationTracker
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

@Database(entities = [PersonEntity::class], version = 1, exportSchema = false)
abstract class KundaliDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao

    companion object {
        @Volatile
        private var INSTANCE: KundaliDatabase? = null

        fun getDatabase(context: Context): KundaliDatabase {
            return INSTANCE ?: synchronized(this) {
                val db: KundaliDatabase = try {
                    Room.databaseBuilder(
                        context.applicationContext,
                        KundaliDatabase::class.java,
                        "kundali_guru.db"
                    ).fallbackToDestructiveMigration().build()
                } catch (e: Throwable) {
                    PersistentKundaliDatabase(context.applicationContext)
                }
                INSTANCE = db
                db
            }
        }
    }
}

private class PersistentKundaliDatabase(private val context: Context) : KundaliDatabase() {
    private val dao = PersistentPersonDao(context)

    override fun personDao(): PersonDao = dao

    override fun createOpenHelper(config: androidx.room.DatabaseConfiguration): SupportSQLiteOpenHelper {
        throw UnsupportedOperationException()
    }

    override fun createInvalidationTracker(): InvalidationTracker {
        return InvalidationTracker(this, emptyMap(), emptyMap(), "persons")
    }

    override fun clearAllTables() {
        dao.clearSync()
    }
}

private class PersistentPersonDao(private val context: Context) : PersonDao {
    private val file = File(context.filesDir, "persons_store.json")
    private val personsState = MutableStateFlow<List<PersonEntity>>(emptyList())

    init {
        loadFromDisk()
    }

    @Synchronized
    private fun loadFromDisk() {
        if (!file.exists()) {
            personsState.value = emptyList()
            return
        }
        try {
            val content = file.readText()
            val array = JSONArray(content)
            val list = mutableListOf<PersonEntity>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    PersonEntity(
                        id = obj.optLong("id", System.currentTimeMillis()),
                        name = obj.optString("name", ""),
                        gender = obj.optString("gender", "Prefer not to say"),
                        birthDate = obj.optString("birthDate", "1995-01-01"),
                        birthTime = obj.optString("birthTime", "12:00"),
                        isTimeUnknown = obj.optBoolean("isTimeUnknown", false),
                        birthPlace = obj.optString("birthPlace", ""),
                        latitude = obj.optDouble("latitude", 0.0),
                        longitude = obj.optDouble("longitude", 0.0),
                        timezoneId = obj.optString("timezoneId", "Asia/Kolkata"),
                        maritalStatus = obj.optString("maritalStatus", "Single"),
                        occupation = obj.optString("occupation", "Other"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
            personsState.value = list.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            personsState.value = emptyList()
        }
    }

    @Synchronized
    private fun saveToDisk(list: List<PersonEntity>) {
        try {
            val array = JSONArray()
            list.forEach { p ->
                val obj = JSONObject().apply {
                    put("id", p.id)
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
                    put("createdAt", p.createdAt)
                }
                array.put(obj)
            }
            file.writeText(array.toString())
            personsState.value = list.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getAllPersons(): Flow<List<PersonEntity>> = personsState

    override fun getPersonById(id: Long): Flow<PersonEntity?> {
        return personsState.map { list -> list.find { it.id == id } }
    }

    override suspend fun getPersonByIdSync(id: Long): PersonEntity? {
        return personsState.value.find { it.id == id }
    }

    override suspend fun getAllPersonsList(): List<PersonEntity> {
        return personsState.value
    }

    override suspend fun insertPerson(person: PersonEntity): Long {
        val current = personsState.value.toMutableList()
        val newId = if (person.id > 0) person.id else (current.maxOfOrNull { it.id } ?: 0L) + 1L
        val savedPerson = person.copy(id = newId)
        val idx = current.indexOfFirst { it.id == newId }
        if (idx >= 0) {
            current[idx] = savedPerson
        } else {
            current.add(0, savedPerson)
        }
        saveToDisk(current)
        return newId
    }

    override suspend fun insertAll(persons: List<PersonEntity>) {
        val current = personsState.value.toMutableList()
        var maxId = current.maxOfOrNull { it.id } ?: 0L
        persons.forEach { p ->
            val id = if (p.id > 0) p.id else ++maxId
            val item = p.copy(id = id)
            val idx = current.indexOfFirst { it.id == id }
            if (idx >= 0) current[idx] = item else current.add(0, item)
        }
        saveToDisk(current)
    }

    override suspend fun updatePerson(person: PersonEntity) {
        insertPerson(person)
    }

    override suspend fun deletePerson(person: PersonEntity) {
        deletePersonById(person.id)
    }

    override suspend fun deletePersonById(id: Long) {
        val current = personsState.value.filter { it.id != id }
        saveToDisk(current)
    }

    override suspend fun clearAll() {
        clearSync()
    }

    fun clearSync() {
        saveToDisk(emptyList())
    }
}
