package com.aryaleelastudios.astroveda

import android.app.Application
import com.aryaleelastudios.astroveda.notifications.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AstroVedaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
