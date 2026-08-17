package com.centinela.familiar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FamiliarMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val titulo = message.notification?.title ?: "Sistema Familiar"
        val cuerpo = message.notification?.body ?: return
        mostrarNotificacion(titulo, cuerpo)
    }

    override fun onNewToken(token: String) {

        val familyId = getSharedPreferences("sistema_familiar", MODE_PRIVATE).getString("family_id", null) ?: return
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        com.google.firebase.database.FirebaseDatabase.getInstance()
            .getReference("familias/$familyId/tokens/$uid")
            .setValue(token)
    }

    private fun mostrarNotificacion(titulo: String, cuerpo: String) {
        val channelId = "sistema_familiar_eventos"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Eventos familiares", NotificationManager.IMPORTANCE_DEFAULT)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()
        getSystemService(NotificationManager::class.java).notify(System.currentTimeMillis().toInt(), notification)
    }
}
