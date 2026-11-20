package com.aryaleelastudios.kundaliguru.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aryaleelastudios.kundaliguru.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val prefs = UserPreferencesRepository(context)
                    val isEnabled = prefs.notificationsEnabledFlow.first()
                    if (isEnabled) {
                        val (hour, minute) = prefs.notificationTimeFlow.first()
                        NotificationHelper.scheduleDailyNotification(context, hour, minute)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
