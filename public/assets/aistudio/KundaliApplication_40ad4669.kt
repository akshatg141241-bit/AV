package com.aryaleelastudios.kundaliguru

import android.app.Application
import com.aryaleelastudios.kundaliguru.notifications.NotificationHelper

class KundaliApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
