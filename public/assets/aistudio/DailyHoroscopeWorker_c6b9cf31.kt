package com.aryaleelastudios.kundaliguru.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aryaleelastudios.kundaliguru.astro.AstroEngine
import com.aryaleelastudios.kundaliguru.astro.PredictionsRepository
import com.aryaleelastudios.kundaliguru.data.local.KundaliDatabase
import com.aryaleelastudios.kundaliguru.data.model.AyanamsaType
import com.aryaleelastudios.kundaliguru.data.preferences.UserPreferencesRepository
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

            val db = KundaliDatabase.getDatabase(applicationContext)
            val persons = db.personDao().getAllPersonsList()
            if (persons.isEmpty()) return Result.success()

            val specificPersonId = prefs.notificationPersonIdFlow.first()
            val targetPerson = persons.find { it.id == specificPersonId } ?: persons.first()

            val predictionsRepo = PredictionsRepository(applicationContext)
            val kundali = AstroEngine.computeKundali(
                birthDateStr = targetPerson.birthDate,
                birthTimeStr = targetPerson.birthTime,
                isTimeUnknown = targetPerson.isTimeUnknown,
                lat = targetPerson.latitude,
                lon = targetPerson.longitude,
                tzId = targetPerson.timezoneId,
                ayanamsaType = AyanamsaType.LAHIRI,
                predictionsRepo = predictionsRepo
            )

            val lang = prefs.languageFlow.first()
            val isHindi = lang == "hi"
            val rashiName = if (isHindi) kundali.lagna.rashiNameHi else kundali.lagna.rashiNameEn
            val teaser = if (isHindi) kundali.dailyPredictionHi else kundali.dailyPredictionEn

            NotificationHelper.showDailyNotification(
                context = applicationContext,
                personName = targetPerson.name,
                rashiName = rashiName,
                teaser = teaser
            )

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
