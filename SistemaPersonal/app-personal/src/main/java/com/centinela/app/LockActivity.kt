package com.centinela.app

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.centinela.app.admin.AdminReceiver
import com.centinela.app.admin.EmergencyPasswordManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LockActivity : ComponentActivity() {

    private lateinit var dpm: DevicePolicyManager
    private lateinit var adminComponent: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        })

        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, AdminReceiver::class.java)

        val prefs = getSharedPreferences("centinela", MODE_PRIVATE)
        val lockUntil = prefs.getLong("lock_until", 0L)
        val now = System.currentTimeMillis()

        if (now >= lockUntil) {
            if (dpm.isDeviceOwnerApp(packageName)) stopLockTask()
            finish()
            return
        }

        if (dpm.isDeviceOwnerApp(packageName)) {
            dpm.setLockTaskPackages(adminComponent, arrayOf(packageName))
            startLockTask()
        }

        val passwordManager = EmergencyPasswordManager(applicationContext)

        setContent {
            var hayPassword by remember { mutableStateOf<Boolean?>(null) }
            LaunchedEffect(Unit) { hayPassword = passwordManager.hayContraseñaConfigurada() }

            LockScreen(
                lockUntil = lockUntil,
                mostrarCampoPassword = hayPassword == true,
                onIntentarDesbloqueo = { intento, onResultado ->
                    lifecycleScope.launch {
                        val ok = passwordManager.verificar(intento)
                        onResultado(ok)
                    }
                },
                onUnlock = {
                    if (dpm.isDeviceOwnerApp(packageName)) stopLockTask()
                    prefs.edit().putLong("lock_until", 0L).apply()
                    finish()
                }
            )
        }
    }
}

@Composable
fun LockScreen(
    lockUntil: Long,
    mostrarCampoPassword: Boolean,
    onIntentarDesbloqueo: (String, (Boolean) -> Unit) -> Unit,
    onUnlock: () -> Unit
) {
    var remainingMs by remember { mutableStateOf(maxOf(0L, lockUntil - System.currentTimeMillis())) }
    var passwordInput by remember { mutableStateOf("") }
    var wrongPassword by remember { mutableStateOf(false) }
    var attempts by remember { mutableStateOf(0) }
    var verificando by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (remainingMs > 0) {
            delay(1000L)
            remainingMs = maxOf(0L, lockUntil - System.currentTimeMillis())
            if (remainingMs == 0L) onUnlock()
        }
    }

    val hours = remainingMs / 3_600_000
    val minutes = (remainingMs % 3_600_000) / 60_000
    val seconds = (remainingMs % 60_000) / 1_000

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF080808)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Text("⚔", fontSize = 48.sp)

            Text("CENTINELA", color = Color(0xFFCC0000), fontSize = 11.sp,
                fontWeight = FontWeight.Black, letterSpacing = 8.sp)

            Text("MODO BLOQUEO ACTIVO", color = Color(0xFF444444),
                fontSize = 10.sp, letterSpacing = 4.sp)

            Box(
                modifier = Modifier.fillMaxWidth()
                    .border(1.dp, Color(0xFF1A1A1A))
                    .background(Color(0xFF0D0D0D))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds)
                        else "%02d:%02d".format(minutes, seconds),
                        color = if (remainingMs < 60_000) Color(0xFFCC0000) else Color.White,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("TIEMPO RESTANTE", color = Color(0xFF333333),
                        fontSize = 9.sp, letterSpacing = 3.sp)
                }
            }

            if (mostrarCampoPassword) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("o ingresá la contraseña de emergencia",
                        color = Color(0xFF333333), fontSize = 11.sp)

                    BasicTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            wrongPassword = false
                        },
                        modifier = Modifier.fillMaxWidth()
                            .border(1.dp, if (wrongPassword) Color(0xFFCC0000) else Color(0xFF222222))
                            .background(Color(0xFF0D0D0D))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp,
                            letterSpacing = 8.sp),
                        cursorBrush = SolidColor(Color(0xFFFFFF00)),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        decorationBox = { inner ->
                            if (passwordInput.isEmpty()) Text("••••••",
                                color = Color(0xFF333333), fontSize = 16.sp)
                            inner()
                        }
                    )

                    if (wrongPassword) Text(
                        "Incorrecta${if (attempts > 2) " — $attempts intentos" else ""}",
                        color = Color(0xFFCC0000), fontSize = 11.sp
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                            .background(Color(0xFF1A1A1A))
                            .clickable(enabled = !verificando) {
                                verificando = true
                                onIntentarDesbloqueo(passwordInput) { ok ->
                                    verificando = false
                                    if (ok) {
                                        onUnlock()
                                    } else {
                                        wrongPassword = true
                                        attempts++
                                        passwordInput = ""
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (verificando) "VERIFICANDO…" else "DESBLOQUEAR", color = Color(0xFF666666),
                            fontSize = 12.sp, fontWeight = FontWeight.Black,
                            letterSpacing = 3.sp)
                    }
                }
            }
        }
    }
}
