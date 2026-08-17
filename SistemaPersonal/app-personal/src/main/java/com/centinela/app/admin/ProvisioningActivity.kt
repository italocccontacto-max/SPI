package com.centinela.app.admin

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.sistemapersonal.network.FirebaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.MessageDigest

class ProvisioningActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProvisioningScreen(
                yaEsDeviceOwner = DeviceOwnerManager.esDeviceOwner(applicationContext),
                remoteProvisioningConfigured = FirebaseConfig.provisioningUrl.isNotBlank(),
                generarQr = { generarBitmapQr(applicationContext) }
            )
        }
    }
}

private fun construirPayloadProvisioning(context: Context): String {
    val admin = ComponentName(context, AdminReceiver::class.java)
    val remoteUrl = FirebaseConfig.provisioningUrl.trim()
    val configuredChecksum = FirebaseConfig.provisioningSignatureChecksum.trim()
    val checksum = configuredChecksum.ifBlank { calcularChecksumFirmaPropia(context) }

    if (remoteUrl.isNotBlank() && checksum.isBlank()) {
        error("El provisioning remoto requiere PROVISIONING_SIGNATURE_CHECKSUM o una APK instalada firmada con la misma clave")
    }

    return JSONObject().apply {
        put("android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME", admin.flattenToString())
        put("android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION", remoteUrl)
        if (checksum.isNotBlank()) {
            put("android.app.extra.PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM", checksum)
        }
        put("android.app.extra.PROVISIONING_SKIP_ENCRYPTION", false)
        put("android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED", true)
        put("android.app.extra.PROVISIONING_LOCALE", "es_AR")
    }.toString()
}

private fun calcularChecksumFirmaPropia(context: Context): String = runCatching {
    val info = context.packageManager.getPackageInfo(
        context.packageName,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) PackageManager.GET_SIGNING_CERTIFICATES
        else @Suppress("DEPRECATION") PackageManager.GET_SIGNATURES
    )
    val firma = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        info.signingInfo?.apkContentsSigners?.firstOrNull()
    } else {
        @Suppress("DEPRECATION") info.signatures?.firstOrNull()
    } ?: return ""
    val digest = MessageDigest.getInstance("SHA-256").digest(firma.toByteArray())
    android.util.Base64.encodeToString(
        digest,
        android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING
    )
}.getOrDefault("")

private suspend fun generarBitmapQr(context: Context): Bitmap = withContext(Dispatchers.Default) {
    val payload = construirPayloadProvisioning(context)
    val tamaño = 720
    val matriz = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, tamaño, tamaño)
    Bitmap.createBitmap(tamaño, tamaño, Bitmap.Config.RGB_565).also { bitmap ->
        for (x in 0 until tamaño) for (y in 0 until tamaño) {
            bitmap.setPixel(x, y, if (matriz.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
}

@Composable
private fun ProvisioningScreen(
    yaEsDeviceOwner: Boolean,
    remoteProvisioningConfigured: Boolean,
    generarQr: suspend () -> Bitmap
) {
    var qr by remember { mutableStateOf<Bitmap?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (!yaEsDeviceOwner) {
            runCatching { generarQr() }
                .onSuccess { qr = it }
                .onFailure { error = it.message ?: "No se pudo generar el QR" }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF080808)).verticalScroll(rememberScrollState()).padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("PROVISIONING · DEVICE OWNER", color = Color(0xFFFFAB13), style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
        if (yaEsDeviceOwner) {
            Text("Este dispositivo ya está aprovisionado como Device Owner.", color = Color(0xFF00CC44), textAlign = TextAlign.Center)
            return@Column
        }

        Text(
            if (remoteProvisioningConfigured) {
                "Modo completo: el QR contiene una URL HTTPS de la APK firmada y el checksum de su firma. " +
                    "Usalo desde el asistente de configuración tras un factory reset."
            } else {
                "Modo local: el QR no descarga la APK. El paquete debe estar ya disponible en el dispositivo " +
                    "destino según el flujo de provisioning elegido. Para el modo remoto, configurá " +
                    "PROVISIONING_APK_DOWNLOAD_URL y PROVISIONING_SIGNATURE_CHECKSUM en el entorno de build."
            },
            color = Color(0xFFB9C8CE), textAlign = TextAlign.Center
        )

        when {
            error != null -> Text("Error generando el QR: $error", color = Color(0xFFCC0000), textAlign = TextAlign.Center)
            qr == null -> CircularProgressIndicator(color = Color(0xFFFFAB13))
            else -> Image(
                bitmap = qr!!.asImageBitmap(),
                contentDescription = "QR de aprovisionamiento de Device Owner",
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)
            )
        }

        Text(
            "Alternativa de laboratorio: adb shell dpm set-device-owner com.centinela.app/.admin.AdminReceiver. " +
                "Probá siempre provisioning y Lock Task en un dispositivo de prueba antes de producción.",
            color = Color(0xFF666666), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
    }
}
