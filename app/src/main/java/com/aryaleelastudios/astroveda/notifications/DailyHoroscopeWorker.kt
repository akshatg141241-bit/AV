package com.aryaleelastudios.astroveda.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aryaleelastudios.astroveda.astro.AstroEngine
import com.aryaleelastudios.astroveda.data.local.AstroVedaDatabase
import com.aryaleelastudios.astroveda.data.preferences.UserPreferencesRepository
import com.aryaleelastudios.astroveda.data.repository.ContentRepository
import kotlinx.coroutines.flow.first

class DailyHoroscopeWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val prefs = UserPreferencesRepository(applicationContext)
            val isEnabled = prefs.notificationsEnabledFlow.first()
            if (!isEnabled) return Result.success()

            val db = AstroVedaDatabase.getDatabase(applicationContext)
            val persons = db.personDao().getAllPersonsList()
            if (persons.isEmpty()) return Result.success()

            val specificPersonId = prefs.notificationPersonIdFlow.first()
            val targetPerson = persons.find { it.id == specificPersonId } ?: persons.first()
            val lang = prefs.languageFlow.first()
            val ayanamsa = prefs.ayanamsaFlow.first()

            val contentRepo = ContentRepository(applicationContext)
            val kundali = AstroEngine.computeKundali(
                birthDateStr = targetPerson.birthDate,
                birthTimeStr = targetPerson.birthTime,
                isTimeUnknown = targetPerson.isTimeUnknown,
                lat = targetPerson.latitude,
                lon = targetPerson.longitude,
                tzId = targetPerson.timezoneId,
                ayanamsaType = ayanamsa,
                langCode = lang,
                contentRepo = contentRepo,
                person = targetPerson
            )

            val moonSign = kundali.planets.firstOrNull { it.canonicalName == "Moon" }?.rashiName
                ?: kundali.lagna.rashiName

            NotificationHelper.showDailyNotification(
                context = applicationContext,
                personName = targetPerson.name,
                rashiName = moonSign,
                teaser = kundali.dailyPrediction.text,
                luckyColor = kundali.luckyElements.color,
                luckyNumber = kundali.luckyElements.number,
                langCode = lang
            )

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
