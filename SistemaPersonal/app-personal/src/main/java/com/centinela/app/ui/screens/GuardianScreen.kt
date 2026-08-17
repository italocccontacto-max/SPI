package com.centinela.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.centinela.app.GuardianHomeActivity
import com.centinela.app.admin.DeviceOwnerManager
import com.centinela.app.capture.ScreenshotConsentActivity
import com.centinela.app.guardian.RootChecker
import com.centinela.app.ui.rememberRepo
import com.sistemapersonal.data.entity.StreakEntity
import com.sistemapersonal.ui.components.AngularPanel
import com.sistemapersonal.ui.components.GlowButton
import com.sistemapersonal.ui.components.InstrumentControl
import com.sistemapersonal.ui.components.InstrumentEvent
import com.sistemapersonal.ui.components.InstrumentMode
import com.sistemapersonal.ui.components.InstrumentOutcome
import com.sistemapersonal.ui.components.LocalVisualEnergy
import com.sistemapersonal.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun GuardianScreen() {
    val context = LocalContext.current
    val repo = rememberRepo()
    val scope = rememberCoroutineScope()

    val streak by repo.streakDao().observar().collectAsState(initial = StreakEntity())
    val streakData = streak ?: StreakEntity()
    val screenshotsHabilitado by repo.ajustes.screenshotsHabilitado.collectAsState(initial = false)
    val bloqueoTotalHabilitado by repo.ajustes.bloqueoTotalHabilitado.collectAsState(initial = false)
    val rootDetectado by repo.ajustes.rootVerificado.collectAsState(initial = false)
    val rootUltimaVerificacion by repo.ajustes.rootUltimaVerificacion.collectAsState(initial = 0L)
    val rootSeñales by repo.ajustes.rootSeñales.collectAsState(initial = "")
    val syncFamiliaHabilitado by repo.ajustes.syncFamiliaHabilitado.collectAsState(initial = false)
    val familyId by repo.ajustes.familyId.collectAsState(initial = null)
    val pairingCode by repo.ajustes.pairingCode.collectAsState(initial = null)
    val pairingCodeExpiresAt by repo.ajustes.pairingCodeExpiresAt.collectAsState(initial = null)
    var pairingError by remember { mutableStateOf<String?>(null) }
    var generandoCodigo by remember { mutableStateOf(false) }
    val esDeviceOwner = remember { DeviceOwnerManager.esDeviceOwner(context) }
    var verificandoRoot by remember { mutableStateOf(false) }
    val visualSystem = LocalVisualEnergy.current

    val screenshotsRecientes by repo.screenshotDao().recientes(20).collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("GUARDIÁN", color = Cyan, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(4.dp))
        Text(
            if (esDeviceOwner) "Device Owner activo — Sistema Personal es el launcher fijo del dispositivo."
            else "Device Owner NO aprovisionado — el bloqueo funciona en modo blando (evadible). Ver README.",
            color = if (esDeviceOwner) Ok else Warn,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(16.dp))

        AngularPanel(modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column {
                    Text("${streakData.diasConsecutivos}", color = Amber, style = MaterialTheme.typography.displayMedium)
                    Text("días de racha (Guardián)", color = Ink2, style = MaterialTheme.typography.labelMedium)
                }
                Column {
                    Text("${streakData.mejorRacha}", color = Ink1, style = MaterialTheme.typography.displayMedium)
                    Text("mejor racha", color = Ink2, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        GlowButton(
            "ABRIR CONFIGURACIÓN DETALLADA DEL GUARDIÁN",
            accent = Cyan, glow = CyanGlow,
            onClick = { context.startActivity(Intent(context, GuardianHomeActivity::class.java)) }
        )

        Spacer(Modifier.height(20.dp))
        Text("Estado de root (comprobación técnica)", color = Ink0, style = MaterialTheme.typography.titleMedium)
        Text(
            if (rootUltimaVerificacion == 0L) "Todavía no se corrió ninguna comprobación."
            else "Última comprobación: ${java.text.SimpleDateFormat("dd/MM HH:mm").format(java.util.Date(rootUltimaVerificacion))}",
            color = Ink2, style = MaterialTheme.typography.bodySmall
        )
        Text(
            if (rootDetectado) "Resultado: SE DETECTÓ ROOT" else "Resultado: NO se detectó root con las señales disponibles",
            color = if (rootDetectado) Ok else Ink1,
            style = MaterialTheme.typography.bodyMedium
        )
        if (rootSeñales.isNotBlank()) {
            Text("Señales: $rootSeñales", color = Ink2, style = MaterialTheme.typography.bodySmall)
        }
        Text(
            "Límite honesto: ninguna app puede garantizar el estado de root de forma absoluta " +
                "(herramientas como Magisk pueden ocultarse). Un \"SÍ\" es confiable; un \"NO\" " +
                "solo significa que estas señales no lo detectaron, no una garantía.",
            color = Ink3, style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(8.dp))
        GlowButton(
            if (verificandoRoot) "VERIFICANDO…" else "RE-VERIFICAR AHORA",
            accent = Cyan, glow = CyanGlow, enabled = !verificandoRoot,
            onClick = {
                verificandoRoot = true
                scope.launch {
                    val resultado = RootChecker.verificar(context)
                    repo.ajustes.registrarResultadoRoot(resultado.detectado, resultado.señales)
                    verificandoRoot = false
                }
            }
        )

        Spacer(Modifier.height(20.dp))
        Text("Bloqueo Total (enforcement fuerte)", color = Ink0, style = MaterialTheme.typography.titleMedium)
        Text(
            "Requiere Device Owner para enforcement fuerte. Root se muestra como telemetría " +
                "técnica independiente y no es requisito del bloqueo Android." ,
            color = Ink2, style = MaterialTheme.typography.bodySmall
        )
        FilaAjusteBooleano(
            titulo = "Habilitar Bloqueo Total",
            valor = bloqueoTotalHabilitado,
            enabled = esDeviceOwner,
            onCambio = { scope.launch { repo.ajustes.setBloqueoTotalHabilitado(it) } }
        )
        if (!esDeviceOwner) {
            Text("Desactivado porque falta Device Owner.", color = Warn, style = MaterialTheme.typography.labelSmall)
        }

        Spacer(Modifier.height(20.dp))
        Text("Captura de pantalla", color = Ink0, style = MaterialTheme.typography.titleMedium)
        Text(
            "Sesión de MediaProjection autorizada explícitamente por vos. Se puede revocar en " +
                "cualquier momento desde los ajustes del sistema.",
            color = Ink2, style = MaterialTheme.typography.bodySmall
        )
        FilaAjusteBooleano(
            titulo = "Captura activa",
            valor = screenshotsHabilitado,
            onCambio = { activar ->
                if (activar) {
                    context.startActivity(Intent(context, ScreenshotConsentActivity::class.java))
                } else {
                    scope.launch {
                        repo.ajustes.setScreenshotsHabilitado(false)
                        context.stopService(
                            com.centinela.app.capture.ScreenshotCaptureService.crearIntentDetener(context)
                        )
                    }
                }
            }
        )

        Spacer(Modifier.height(20.dp))
        Text("Sincronización con Sistema Familiar", color = Ink0, style = MaterialTheme.typography.titleMedium)
        Text(
            "DEPENDENCIA EXTERNA: requiere que completes la configuración de Firebase (ver " +
                "README) y que exista vinculación de familia. Local-first: si está apagado o " +
                "no hay conexión, todo sigue funcionando localmente.",
            color = Ink2, style = MaterialTheme.typography.bodySmall
        )
        FilaAjusteBooleano(
            titulo = "Compartir actividad con la familia",
            valor = syncFamiliaHabilitado,
            onCambio = { activar ->
                scope.launch {
                    if (activar) {
                        val result = com.centinela.app.sync.PersonalFamilyProvisioning.ensure(context)
                        if (result.isSuccess) {
                            repo.ajustes.setSyncFamiliaHabilitado(true)
                            (context.applicationContext as? com.centinela.app.SistemaPersonalApp)?.programarSyncAhora()
                        } else {
                            pairingError = "No se puede activar la sincronización hasta que Firebase esté disponible."
                            visualSystem.fail(0f, 0f)
                        }
                    } else {
                        repo.ajustes.setSyncFamiliaHabilitado(false)
                    }
                }
            }
        )
        Spacer(Modifier.height(8.dp))
        val codigoVigente = !pairingCode.isNullOrBlank() && (pairingCodeExpiresAt == null || pairingCodeExpiresAt!! > System.currentTimeMillis())
        when {
            familyId.isNullOrBlank() -> {
                GlowButton(
                    if (generandoCodigo) "CREANDO…" else "CREAR FAMILIA Y CÓDIGO",
                    accent = Cyan, glow = CyanGlow, enabled = !generandoCodigo,
                    controlId = "guardian.create-family",
                    onClick = {
                        generandoCodigo = true
                        pairingError = null
                        scope.launch {
                            val result = com.centinela.app.sync.PersonalFamilyProvisioning.ensure(context)
                            generandoCodigo = false
                            result.onFailure { pairingError = "No se pudo crear la familia. Verificá Firebase y la conexión."; visualSystem.instrumentEvent("guardian.create-family", InstrumentEvent.ERROR) }
                        }
                    }
                )
            }
            !codigoVigente -> {
                GlowButton(
                    if (generandoCodigo) "RENOVANDO…" else "GENERAR CÓDIGO DE VINCULACIÓN",
                    accent = Cyan, glow = CyanGlow, enabled = !generandoCodigo,
                    controlId = "guardian.generate-code",
                    onClick = {
                        generandoCodigo = true
                        pairingError = null
                        scope.launch {
                            val result = com.centinela.app.sync.PersonalFamilyProvisioning.renovarCodigo(context)
                            generandoCodigo = false
                            result.onFailure { pairingError = "No se pudo generar el código. Verificá Firebase y la conexión."; visualSystem.instrumentEvent("guardian.generate-code", InstrumentEvent.ERROR) }
                        }
                    }
                )
            }
            else -> {
                Text("Código de vinculación: $pairingCode", color = Cyan, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Temporal y de un solo uso. Expira a las ${pairingCodeExpiresAt?.let { java.text.SimpleDateFormat("HH:mm").format(java.util.Date(it)) } ?: "pronto"}.",
                    color = Ink2, style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(6.dp))
                GlowButton(
                    if (generandoCodigo) "RENOVANDO…" else "RENOVAR CÓDIGO",
                    accent = Cyan, glow = CyanGlow, enabled = !generandoCodigo,
                    controlId = "guardian.renew-code",
                    onClick = {
                        generandoCodigo = true
                        pairingError = null
                        scope.launch {
                            val result = com.centinela.app.sync.PersonalFamilyProvisioning.renovarCodigo(context)
                            generandoCodigo = false
                            result.onFailure { pairingError = "No se pudo renovar el código."; visualSystem.instrumentEvent("guardian.renew-code", InstrumentEvent.ERROR) }
                        }
                    }
                )
            }
        }
        pairingError?.let { Text(it, color = Warn, style = MaterialTheme.typography.bodySmall) }

        Spacer(Modifier.height(20.dp))
        Text("Capturas recientes (${screenshotsRecientes.size})", color = Ink0, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(screenshotsRecientes, key = { it.id }) { s ->
                AngularPanel(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(java.text.SimpleDateFormat("dd/MM HH:mm").format(java.util.Date(s.timestamp)), color = Ink1)
                        Text(if (s.uploaded) "sincronizada" else "solo local", color = if (s.uploaded) Ok else Ink3, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaAjusteBooleano(titulo: String, valor: Boolean, enabled: Boolean = true, onCambio: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(titulo, color = if (enabled) Ink1 else Ink3, style = MaterialTheme.typography.bodyMedium)
        InstrumentControl(
            controlId = "guardian.toggle.${titulo.hashCode()}",
            modifier = Modifier.width(56.dp).height(34.dp),
            accent = Cyan,
            enabled = enabled,
            mode = InstrumentMode.TOGGLE,
            value = if (valor) 1f else 0f,
            onValueChange = { onCambio(it > 0.5f) },
            onActivate = { InstrumentOutcome.SUCCESS }
        ) {

        }
    }
}
