package com.centinela.app.contract

import com.sistemapersonal.data.repo.SistemaPersonalRepository
import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.*

class MorningReadingCheckService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch {
            val dao = SistemaPersonalRepository.get(applicationContext).intentionContractDao()
            val latest = dao.getLatest()
            if (latest != null && !latest.wasRead) {
                val activityIntent = Intent(applicationContext, MorningReadingActivity::class.java)
                activityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(activityIntent)
            }
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
