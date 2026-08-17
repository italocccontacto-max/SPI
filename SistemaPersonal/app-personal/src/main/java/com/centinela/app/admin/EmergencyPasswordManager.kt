package com.centinela.app.admin

import android.content.Context
import android.util.Base64
import com.sistemapersonal.data.prefs.AjustesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class EmergencyPasswordManager(context: Context) {
    private val ajustes = AjustesDataStore(context)
    private val legacyPrefs = context.getSharedPreferences("centinela", Context.MODE_PRIVATE)

    suspend fun hayContraseñaConfigurada(): Boolean =
        ajustes.emergencyPasswordHash() != null || legacyPrefs.getString("lock_password", null) != null

    suspend fun guardar(passwordPlano: String) = withContext(Dispatchers.Default) {
        require(passwordPlano.length >= 6) { "La contraseña debe tener al menos 6 dígitos" }
        val salt = ByteArray(16).also(SecureRandom()::nextBytes)
        val hash = derivar(passwordPlano, salt)
        ajustes.setEmergencyPasswordHash("pbkdf2:$ITERATIONS:${base64(hash)}", base64(salt))
        legacyPrefs.edit().remove("lock_password").apply()
    }

    suspend fun verificar(passwordIngresado: String): Boolean = withContext(Dispatchers.Default) {
        val guardado = ajustes.emergencyPasswordHash()
        val saltB64 = ajustes.emergencyPasswordSalt()
        if (guardado?.startsWith("pbkdf2:") == true && saltB64 != null) {
            val parts = guardado.split(':')
            if (parts.size != 3) return@withContext false
            val iterations = parts[1].toIntOrNull() ?: return@withContext false
            val expected = runCatching { Base64.decode(parts[2], Base64.NO_WRAP) }.getOrNull() ?: return@withContext false
            val salt = runCatching { Base64.decode(saltB64, Base64.NO_WRAP) }.getOrNull() ?: return@withContext false
            val actual = derivar(passwordIngresado, salt, iterations)
            return@withContext MessageDigest.isEqual(actual, expected)
        }

        val legacyHash = guardado ?: return@withContext verificarLegacy(passwordIngresado)
        if (saltB64 == null) return@withContext verificarLegacy(passwordIngresado)
        val salt = runCatching { Base64.decode(saltB64, Base64.NO_WRAP) }.getOrNull() ?: return@withContext false
        val digest = MessageDigest.getInstance("SHA-256")
        val legacySimple = digest.digest(passwordIngresado.toByteArray(StandardCharsets.UTF_8))
        val legacySalted = digest.digest(salt + passwordIngresado.toByteArray(StandardCharsets.UTF_8))
        val simpleHex = legacySimple.joinToString("") { "%02x".format(it) }
        val saltedHex = legacySalted.joinToString("") { "%02x".format(it) }
        val ok = legacyHash.equals(simpleHex, ignoreCase = true) || legacyHash.equals(saltedHex, ignoreCase = true)
        if (ok) {
            guardar(passwordIngresado)
        }
        ok
    }

    private suspend fun verificarLegacy(password: String): Boolean {
        val legacy = legacyPrefs.getString("lock_password", null) ?: return false
        val ok = MessageDigest.isEqual(
            password.toByteArray(StandardCharsets.UTF_8),
            legacy.toByteArray(StandardCharsets.UTF_8)
        )
        if (ok) guardar(password)
        return ok
    }

    private fun derivar(password: String, salt: ByteArray, iterations: Int = ITERATIONS): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun base64(value: ByteArray) = Base64.encodeToString(value, Base64.NO_WRAP)

    companion object {
        private const val ITERATIONS = 600_000
        private const val KEY_BITS = 256
    }
}
