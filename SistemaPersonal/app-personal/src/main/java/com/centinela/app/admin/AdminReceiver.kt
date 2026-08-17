package com.centinela.app.admin

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

class AdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {

        DeviceOwnerManager.configurarComoLauncherFijo(context)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        DeviceOwnerManager.liberarLauncherFijo(context)
    }
}
