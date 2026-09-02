package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context, this)
                    val profile = db.userProfileDao().getUserProfileDirect()
                    if (profile != null && profile.isCompleted) {
                        AlarmScheduler.scheduleProfileAlarms(context, profile)
                        Log.d("BootReceiver", "Restored all alarms for profile: ${profile.name}")
                    }
                    val enabledCustomAlarms = db.alarmDao().getEnabledAlarmsDirect()
                    for (alarm in enabledCustomAlarms) {
                        AlarmScheduler.scheduleCustomAlarm(context, alarm)
                        Log.d("BootReceiver", "Restored custom alarm: ${alarm.title} (${alarm.id})")
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Failed to restore alarms on reboot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
