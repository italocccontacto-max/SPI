package com.centinela.app

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sistemapersonal.data.repo.SistemaPersonalRepository
import kotlinx.coroutines.launch

fun getInstalledApps(context: Context): List<Pair<String, String>> =
    context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
        .filter { it.packageName != "com.centinela.app" }
        .map { it.packageName to context.packageManager.getApplicationLabel(it).toString() }
        .sortedBy { it.second }

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SettingsScreen(onDone = { finish() }) }
    }
}

@Composable
fun SettingsScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { SistemaPersonalRepository.get(context) }
    val apps = remember { getInstalledApps(context) }
    val scope = rememberCoroutineScope()

    val blockedAppsFlow by repo.ajustes.appsProhibidas.collectAsState(initial = emptySet())
    val thresholdFlow by repo.ajustes.umbralMinutos.collectAsState(initial = 20)
    val blockDurationFlow by repo.ajustes.duracionBloqueoMinutos.collectAsState(initial = 10)
    val debtActionFlow by repo.ajustes.debtAction.collectAsState(initial = "")
    val debtMinutesFlow by repo.ajustes.debtMinutesPerUnit.collectAsState(initial = 10)
    val hourlyValueFlow by repo.ajustes.hourlyValue.collectAsState(initial = 0f)
    val lockDurationFlow by repo.ajustes.lockDurationMinutes.collectAsState(initial = 30)

    var blockedApps by remember { mutableStateOf(emptySet<String>()) }
    var thresholdMinutes by remember { mutableStateOf(20f) }
    var blockDurationMinutes by remember { mutableStateOf(10f) }
    var hourlyValueText by remember { mutableStateOf("") }
    var debtAction by remember { mutableStateOf("") }
    var debtMinutesPerUnit by remember { mutableStateOf(10f) }
    var lockDurationTotal by remember { mutableStateOf(30f) }

    LaunchedEffect(blockedAppsFlow) { blockedApps = blockedAppsFlow }
    LaunchedEffect(thresholdFlow) { thresholdMinutes = thresholdFlow.toFloat() }
    LaunchedEffect(blockDurationFlow) { blockDurationMinutes = blockDurationFlow.toFloat() }
    LaunchedEffect(debtActionFlow) { debtAction = debtActionFlow }
    LaunchedEffect(debtMinutesFlow) { debtMinutesPerUnit = debtMinutesFlow.toFloat() }
    LaunchedEffect(hourlyValueFlow) { hourlyValueText = if (hourlyValueFlow == 0f) "" else hourlyValueFlow.toString() }
    LaunchedEffect(lockDurationFlow) { lockDurationTotal = lockDurationFlow.toFloat() }

    val passwordManager = remember { com.centinela.app.admin.EmergencyPasswordManager(context) }
    var lockPasswordInput by remember { mutableStateOf("") }
    var hayPasswordGuardada by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { hayPasswordGuardada = passwordManager.hayContraseñaConfigurada() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFF080808)).padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("CONFIGURACIÓN", color = Color(0xFF444444), fontSize = 11.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 6.sp)
            Spacer(Modifier.height(24.dp))
        }

        item {
            Text("TIEMPO ANTES DEL BLOQUEO", color = Color(0xFF666666), fontSize = 11.sp,
                letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("${thresholdMinutes.toInt()} MINUTOS", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Slider(
                value = thresholdMinutes,
                onValueChange = { thresholdMinutes = it },
                onValueChangeFinished = { scope.launch { repo.ajustes.setUmbralMinutos(thresholdMinutes.toInt()) } },
                valueRange = 5f..60f, steps = 10,
                colors = androidx.compose.material3.SliderDefaults.colors(
                    thumbColor = Color(0xFFCC0000), activeTrackColor = Color(0xFFCC0000), inactiveTrackColor = Color(0xFF222222))
            )
            Spacer(Modifier.height(16.dp))
        }

        item {
            Text("DURACIÓN DEL BLOQUEO DE APP", color = Color(0xFF666666), fontSize = 11.sp,
                letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("${blockDurationMinutes.toInt()} MINUTOS", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Slider(
                value = blockDurationMinutes,
                onValueChange = { blockDurationMinutes = it },
                onValueChangeFinished = { scope.launch { repo.ajustes.setDuracionBloqueoMinutos(blockDurationMinutes.toInt()) } },
                valueRange = 1f..60f, steps = 58,
                colors = androidx.compose.material3.SliderDefaults.colors(
                    thumbColor = Color(0xFFCC0000), activeTrackColor = Color(0xFFCC0000), inactiveTrackColor = Color(0xFF222222))
            )
            Spacer(Modifier.height(16.dp))
        }

        item {
            Text("BLOQUEO TOTAL DEL CELULAR", color = Color(0xFF666666), fontSize = 11.sp,
                letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Contraseña de emergencia (dásela a tu mamá)", color = Color(0xFF444444), fontSize = 11.sp)
            if (hayPasswordGuardada) {
                Text("Ya hay una contraseña configurada. Escribí una nueva para reemplazarla.", color = Color(0xFF336633), fontSize = 10.sp)
            }
            Spacer(Modifier.height(8.dp))
            BasicTextField(
                value = lockPasswordInput,
                onValueChange = { lockPasswordInput = it.filter(Char::isDigit).take(32) },
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF222222)).background(Color(0xFF0D0D0D)).padding(16.dp),
                textStyle = TextStyle(color = Color.White, fontSize = 14.sp, letterSpacing = 4.sp),
                cursorBrush = SolidColor(Color(0xFFFFFF00)),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                decorationBox = { inner ->
                    if (lockPasswordInput.isEmpty()) Text(
                        if (hayPasswordGuardada) "•••••• (sin cambios)" else "Contraseña numérica...",
                        color = Color(0xFF333333), fontSize = 14.sp)
                    inner()
                }
            )
            Box(
                modifier = Modifier.padding(top = 8.dp)
                    .background(if (lockPasswordInput.isNotBlank()) Color(0xFF1A2E1A) else Color(0xFF151515))
                    .clickable(enabled = lockPasswordInput.length >= 6) {
                        scope.launch {
                            passwordManager.guardar(lockPasswordInput)
                            hayPasswordGuardada = true
                            lockPasswordInput = ""
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text("GUARDAR CONTRASEÑA", color = if (lockPasswordInput.length >= 6) Color(0xFF66CC66) else Color(0xFF444444),
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text("DURACIÓN DEL BLOQUEO TOTAL", color = Color(0xFF666666), fontSize = 11.sp,
                letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("${lockDurationTotal.toInt()} MINUTOS", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Slider(
                value = lockDurationTotal,
                onValueChange = { lockDurationTotal = it },
                onValueChangeFinished = { scope.launch { repo.ajustes.setLockDurationMinutes(lockDurationTotal.toInt()) } },
                valueRange = 10f..480f, steps = 46,
                colors = androidx.compose.material3.SliderDefaults.colors(
                    thumbColor = Color(0xFFCC0000), activeTrackColor = Color(0xFFCC0000), inactiveTrackColor = Color(0xFF222222))
            )
            Spacer(Modifier.height(16.dp))
        }

        item {
            Text("EL COSTO DECLARADO", color = Color(0xFF666666), fontSize = 11.sp,
                letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("¿Cuánto vale tu hora? (en tu moneda)", color = Color(0xFF444444), fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF222222)).background(Color(0xFF0D0D0D)).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$", color = Color(0xFFCC0000), fontSize = 18.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    value = hourlyValueText,
                    onValueChange = { v ->
                        hourlyValueText = v.filter { it.isDigit() || it == '.' }
                        scope.launch { repo.ajustes.setHourlyValue(hourlyValueText.toFloatOrNull() ?: 0f) }
                    },
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    cursorBrush = SolidColor(Color(0xFFFFFF00)),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    decorationBox = { inner -> if (hourlyValueText.isEmpty()) Text("0.00", color = Color(0xFF333333), fontSize = 18.sp); inner() }
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        item {
            Text("LA DEUDA DE ATENCIÓN", color = Color(0xFF666666), fontSize = 11.sp,
                letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Acción que pagás por cada ${debtMinutesPerUnit.toInt()} min de distracción", color = Color(0xFF444444), fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            BasicTextField(
                value = debtAction,
                onValueChange = { debtAction = it; scope.launch { repo.ajustes.setDebtAction(it) } },
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF222222)).background(Color(0xFF0D0D0D)).padding(16.dp),
                textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                cursorBrush = SolidColor(Color(0xFFFFFF00)),
                decorationBox = { inner -> if (debtAction.isEmpty()) Text("Ej: 10 flexiones, leer 5 páginas...", color = Color(0xFF333333), fontSize = 14.sp); inner() }
            )
            Spacer(Modifier.height(8.dp))
            Text("${debtMinutesPerUnit.toInt()} MIN = 1 acción", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
            Slider(
                value = debtMinutesPerUnit,
                onValueChange = { debtMinutesPerUnit = it },
                onValueChangeFinished = { scope.launch { repo.ajustes.setDebtMinutesPerUnit(debtMinutesPerUnit.toInt()) } },
                valueRange = 5f..60f, steps = 10,
                colors = androidx.compose.material3.SliderDefaults.colors(
                    thumbColor = Color(0xFFCC0000), activeTrackColor = Color(0xFFCC0000), inactiveTrackColor = Color(0xFF222222))
            )
            Spacer(Modifier.height(16.dp))
        }

        item {
            Text("APPS VIGILADAS", color = Color(0xFF666666), fontSize = 11.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
        }

        items(apps) { (packageName, appName) ->
            val isBlocked = packageName in blockedApps
            Row(
                modifier = Modifier.fillMaxWidth()
                    .border(1.dp, if (isBlocked) Color(0xFF330000) else Color(0xFF1A1A1A))
                    .background(if (isBlocked) Color(0xFF110000) else Color(0xFF0D0D0D))
                    .clickable {
                        blockedApps = if (isBlocked) blockedApps - packageName else blockedApps + packageName
                        scope.launch { repo.ajustes.setAppsProhibidas(blockedApps) }
                    }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(appName, color = if (isBlocked) Color.White else Color(0xFF666666), fontSize = 13.sp,
                    fontWeight = if (isBlocked) FontWeight.Bold else FontWeight.Normal)
                Text(if (isBlocked) "✕" else "+", color = if (isBlocked) Color(0xFFCC0000) else Color(0xFF333333),
                    fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text("DEVICE OWNER / MODO QUIOSCO", color = Color(0xFF666666), fontSize = 11.sp,
                letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            val esDeviceOwner = remember { com.centinela.app.admin.DeviceOwnerManager.esDeviceOwner(context) }
            Text(
                if (esDeviceOwner) "Activo — Sistema Personal es el launcher fijo de este dispositivo."
                else "No activo. Sin esto, el enforcement fuerte no puede garantizarse.",
                color = if (esDeviceOwner) Color(0xFF00CC44) else Color(0xFF888888), fontSize = 12.sp, lineHeight = 18.sp
            )
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(48.dp).border(1.dp, Color(0xFF444444)).clickable {
                    context.startActivity(android.content.Intent(context, com.centinela.app.admin.ProvisioningActivity::class.java))
                },
                contentAlignment = Alignment.Center
            ) {
                Text(if (esDeviceOwner) "VER ESTADO / CÓMO DESACTIVAR" else "VER APROVISIONAMIENTO QR",
                    color = Color(0xFFAAAAAA), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(16.dp))
        }

        item {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(56.dp).background(Color(0xFFCC0000)).clickable { onDone() },
                contentAlignment = Alignment.Center
            ) {
                Text("GUARDAR Y SALIR", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
