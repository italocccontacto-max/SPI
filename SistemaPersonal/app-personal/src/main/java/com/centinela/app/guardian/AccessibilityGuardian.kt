package com.centinela.app.guardian

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.accessibility.AccessibilityEvent

class AccessibilityGuardian : AccessibilityService() {

    private val prefs by lazy { getSharedPreferences("centinela", Context.MODE_PRIVATE) }
    private var ultimoPaquete: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val event = event ?: return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val paquete = event.packageName?.toString() ?: return
        if (paquete == ultimoPaquete) return

        ultimoPaquete = paquete
        prefs.edit()
            .putString(KEY_FG_PACKAGE, paquete)
            .putLong(KEY_FG_SINCE, System.currentTimeMillis())
            .apply()
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs.edit().putBoolean(KEY_ACCESSIBILITY_ACTIVA, true).apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        prefs.edit().putBoolean(KEY_ACCESSIBILITY_ACTIVA, false).apply()
    }

    companion object {
        const val KEY_FG_PACKAGE = "session_fg_package"
        const val KEY_FG_SINCE = "session_fg_since"
        const val KEY_ACCESSIBILITY_ACTIVA = "accessibility_activa"
    }
}
