package com.centinela.app.contract

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.Calendar

class ScreenStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_ON) {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if (hour in 5..11) {
                context.startService(
                    Intent(context, MorningReadingCheckService::class.java)
                )
            }
        }
    }
}
