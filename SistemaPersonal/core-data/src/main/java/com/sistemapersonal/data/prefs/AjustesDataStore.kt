package com.sistemapersonal.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "ajustes_sistema_personal")

class AjustesDataStore(private val context: Context) {

    private object Keys {
        val UMBRAL_MINUTOS = intPreferencesKey("umbral_bloqueado_minutos")
        val DURACION_BLOQUEO_MINUTOS = intPreferencesKey("duracion_bloqueo_minutos")
        val INTERVALO_SCREENSHOTS_SEG = intPreferencesKey("intervalo_screenshots_seg")
        val SCREENSHOTS_HABILITADO = booleanPreferencesKey("screenshots_habilitado")
        val EMERGENCY_PASSWORD_HASH = stringPreferencesKey("emergency_password_hash")
        val EMERGENCY_PASSWORD_SALT = stringPreferencesKey("emergency_password_salt")
        val APPS_PROHIBIDAS = stringSetPreferencesKey("apps_prohibidas")
        val ROOT_VERIFICADO = booleanPreferencesKey("root_verificado")
        val ROOT_ULTIMA_VERIFICACION = longPreferencesKey("root_ultima_verificacion")
        val ROOT_SEÑALES = stringPreferencesKey("root_señales")
        val BLOQUEO_TOTAL_HABILITADO = booleanPreferencesKey("bloqueo_total_habilitado")
        val SYNC_FAMILIA_HABILITADO = booleanPreferencesKey("sync_familia_habilitado")
        val FAMILY_ID = stringPreferencesKey("family_id")
        val PAIRING_CODE = stringPreferencesKey("pairing_code")
        val PAIRING_CODE_EXPIRES_AT = longPreferencesKey("pairing_code_expires_at")
        val BLOCK_DURATION_MINUTES = intPreferencesKey("guardian_block_duration_minutes")
        val LOCK_DURATION_MINUTES = intPreferencesKey("guardian_lock_duration_minutes")
        val DEBT_ACTION = stringPreferencesKey("guardian_debt_action")
        val DEBT_MINUTES_PER_UNIT = intPreferencesKey("guardian_debt_minutes_per_unit")
        val HOURLY_VALUE = floatPreferencesKey("guardian_hourly_value")
    }

    val umbralMinutos: Flow<Int> = context.dataStore.data.map { it[Keys.UMBRAL_MINUTOS] ?: 20 }
    val duracionBloqueoMinutos: Flow<Int> = context.dataStore.data.map { it[Keys.DURACION_BLOQUEO_MINUTOS] ?: 10 }
    val intervaloScreenshotsSeg: Flow<Int> = context.dataStore.data.map { it[Keys.INTERVALO_SCREENSHOTS_SEG] ?: 60 }
    val screenshotsHabilitado: Flow<Boolean> = context.dataStore.data.map { it[Keys.SCREENSHOTS_HABILITADO] ?: false }
    val appsProhibidas: Flow<Set<String>> = context.dataStore.data.map {
        it[Keys.APPS_PROHIBIDAS] ?: DEFAULT_BLOCKED_APPS
    }
    val rootVerificado: Flow<Boolean> = context.dataStore.data.map { it[Keys.ROOT_VERIFICADO] ?: false }
    val rootUltimaVerificacion: Flow<Long> = context.dataStore.data.map { it[Keys.ROOT_ULTIMA_VERIFICACION] ?: 0L }
    val rootSeñales: Flow<String> = context.dataStore.data.map { it[Keys.ROOT_SEÑALES] ?: "" }
    val bloqueoTotalHabilitado: Flow<Boolean> = context.dataStore.data.map { it[Keys.BLOQUEO_TOTAL_HABILITADO] ?: false }
    val syncFamiliaHabilitado: Flow<Boolean> = context.dataStore.data.map { it[Keys.SYNC_FAMILIA_HABILITADO] ?: false }
    val familyId: Flow<String?> = context.dataStore.data.map { it[Keys.FAMILY_ID] }
    val pairingCode: Flow<String?> = context.dataStore.data.map { it[Keys.PAIRING_CODE] }
    val pairingCodeExpiresAt: Flow<Long?> = context.dataStore.data.map { it[Keys.PAIRING_CODE_EXPIRES_AT] }
    val debtAction: Flow<String> = context.dataStore.data.map { it[Keys.DEBT_ACTION] ?: "" }
    val debtMinutesPerUnit: Flow<Int> = context.dataStore.data.map { it[Keys.DEBT_MINUTES_PER_UNIT] ?: 10 }
    val lockDurationMinutes: Flow<Int> = context.dataStore.data.map { it[Keys.LOCK_DURATION_MINUTES] ?: 30 }
    val hourlyValue: Flow<Float> = context.dataStore.data.map { it[Keys.HOURLY_VALUE] ?: 0f }
    val blockDurationMinutes: Flow<Int> = context.dataStore.data.map { it[Keys.BLOCK_DURATION_MINUTES] ?: 10 }

    suspend fun guardarProvisionFamilia(familyId: String, pairingCode: String, expiresAt: Long? = null) = context.dataStore.edit {
        it[Keys.FAMILY_ID] = familyId
        it[Keys.PAIRING_CODE] = pairingCode
        if (expiresAt != null) it[Keys.PAIRING_CODE_EXPIRES_AT] = expiresAt
        else it.remove(Keys.PAIRING_CODE_EXPIRES_AT)
    }

    suspend fun actualizarCodigoVinculacion(pairingCode: String, expiresAt: Long) = context.dataStore.edit {
        it[Keys.PAIRING_CODE] = pairingCode
        it[Keys.PAIRING_CODE_EXPIRES_AT] = expiresAt
    }

    suspend fun borrarProvisionFamilia() = context.dataStore.edit {
        it.remove(Keys.FAMILY_ID)
        it.remove(Keys.PAIRING_CODE)
        it.remove(Keys.PAIRING_CODE_EXPIRES_AT)
        it[Keys.SYNC_FAMILIA_HABILITADO] = false
    }

    suspend fun setUmbralMinutos(v: Int) = context.dataStore.edit { it[Keys.UMBRAL_MINUTOS] = v.coerceIn(1, 240) }
    suspend fun setDuracionBloqueoMinutos(v: Int) = context.dataStore.edit { it[Keys.DURACION_BLOQUEO_MINUTOS] = v.coerceIn(1, 120) }
    suspend fun setIntervaloScreenshots(v: Int) = context.dataStore.edit { it[Keys.INTERVALO_SCREENSHOTS_SEG] = v.coerceIn(15, 3600) }
    suspend fun setScreenshotsHabilitado(v: Boolean) = context.dataStore.edit { it[Keys.SCREENSHOTS_HABILITADO] = v }
    suspend fun setAppsProhibidas(v: Set<String>) = context.dataStore.edit { it[Keys.APPS_PROHIBIDAS] = v }
    suspend fun registrarResultadoRoot(detectado: Boolean, señales: List<String>) = context.dataStore.edit {
        it[Keys.ROOT_VERIFICADO] = detectado
        it[Keys.ROOT_ULTIMA_VERIFICACION] = System.currentTimeMillis()
        it[Keys.ROOT_SEÑALES] = señales.joinToString("; ")
    }
    suspend fun setBloqueoTotalHabilitado(v: Boolean) = context.dataStore.edit { it[Keys.BLOQUEO_TOTAL_HABILITADO] = v }
    suspend fun setSyncFamiliaHabilitado(v: Boolean) = context.dataStore.edit { it[Keys.SYNC_FAMILIA_HABILITADO] = v }
    suspend fun setDebtAction(v: String) = context.dataStore.edit { it[Keys.DEBT_ACTION] = v }
    suspend fun setDebtMinutesPerUnit(v: Int) = context.dataStore.edit { it[Keys.DEBT_MINUTES_PER_UNIT] = v.coerceAtLeast(1) }
    suspend fun setLockDurationMinutes(v: Int) = context.dataStore.edit { it[Keys.LOCK_DURATION_MINUTES] = v.coerceIn(10, 480) }
    suspend fun setHourlyValue(v: Float) = context.dataStore.edit { it[Keys.HOURLY_VALUE] = v.coerceAtLeast(0f) }

    suspend fun migrarLegacySiCorresponde() {
        val prefs = context.getSharedPreferences("centinela", Context.MODE_PRIVATE)
        context.dataStore.edit { current ->
            if (!current.contains(Keys.APPS_PROHIBIDAS) && prefs.contains("blocked_apps")) {
                current[Keys.APPS_PROHIBIDAS] = prefs.getStringSet("blocked_apps", DEFAULT_BLOCKED_APPS) ?: DEFAULT_BLOCKED_APPS
            }
            if (!current.contains(Keys.UMBRAL_MINUTOS) && prefs.contains("usage_threshold_minutes")) {
                current[Keys.UMBRAL_MINUTOS] = prefs.getInt("usage_threshold_minutes", 20)
            }
            if (!current.contains(Keys.DURACION_BLOQUEO_MINUTOS) && prefs.contains("block_duration_minutes")) {
                current[Keys.DURACION_BLOQUEO_MINUTOS] = prefs.getInt("block_duration_minutes", 10)
            }
            if (!current.contains(Keys.DEBT_ACTION) && prefs.contains("debt_action")) {
                current[Keys.DEBT_ACTION] = prefs.getString("debt_action", "") ?: ""
            }
            if (!current.contains(Keys.DEBT_MINUTES_PER_UNIT) && prefs.contains("debt_minutes_per_unit")) {
                current[Keys.DEBT_MINUTES_PER_UNIT] = prefs.getInt("debt_minutes_per_unit", 10)
            }
            if (!current.contains(Keys.LOCK_DURATION_MINUTES) && prefs.contains("lock_duration_minutes")) {
                current[Keys.LOCK_DURATION_MINUTES] = prefs.getInt("lock_duration_minutes", 30)
            }
            if (!current.contains(Keys.HOURLY_VALUE) && prefs.contains("hourly_value")) {
                current[Keys.HOURLY_VALUE] = prefs.getFloat("hourly_value", 0f)
            }
        }

        prefs.edit()
            .remove("blocked_apps")
            .remove("usage_threshold_minutes")
            .remove("block_duration_minutes")
            .remove("debt_action")
            .remove("debt_minutes_per_unit")
            .remove("lock_duration_minutes")
            .remove("hourly_value")
            .apply()
    }

    suspend fun emergencyPasswordHash(): String? = context.dataStore.data.map { it[Keys.EMERGENCY_PASSWORD_HASH] }.first()
    suspend fun emergencyPasswordSalt(): String? = context.dataStore.data.map { it[Keys.EMERGENCY_PASSWORD_SALT] }.first()
    suspend fun setEmergencyPasswordHash(hash: String, salt: String) = context.dataStore.edit {
        it[Keys.EMERGENCY_PASSWORD_HASH] = hash
        it[Keys.EMERGENCY_PASSWORD_SALT] = salt
    }

    companion object {
        val DEFAULT_BLOCKED_APPS = setOf(
            "com.google.android.youtube", "com.instagram.android",
            "com.zhiliaoapp.musically", "com.twitter.android", "com.facebook.katana"
        )
    }
}
