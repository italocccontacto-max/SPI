package com.centinela.app

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.sistemapersonal.data.repo.SistemaPersonalRepository
import kotlinx.coroutines.flow.first
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.centinela.app.LockActivity
import com.centinela.app.contract.NightlyContractActivity
import com.centinela.app.contract.ScreenStateReceiver

class GuardianHomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerScreenReceiver(this)
        setContent { CentinelaMain() }
    }

    @Composable
    fun CentinelaMain() {
        val hasUsage = hasUsagePermission()
        val hasOverlay = Settings.canDrawOverlays(this)
        val prefs = getSharedPreferences("centinela", MODE_PRIVATE)
        val isRunning = remember { mutableStateOf(prefs.getBoolean("guardian_active", false)) }

        CentinelaApp(
            hasUsagePermission = hasUsage,
            hasOverlayPermission = hasOverlay,
            isGuardianRunning = isRunning.value,
            onRequestUsage = {
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            },
            onRequestOverlay = {
                startActivity(Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                ))
            },
            onStartGuardian = {
                startGuardianService()
                isRunning.value = true
                prefs.edit().putBoolean("guardian_active", true).apply()
            },
            onOpenContract = {
                startActivity(Intent(this, NightlyContractActivity::class.java))
            },
            onOpenCustomQuestions = {
                startActivity(Intent(this, CustomQuestionsActivity::class.java))
            },
            onOpenSettings = {
                startActivity(Intent(this, SettingsActivity::class.java))
            },
            onOpenLock = {
                lifecycleScope.launch {
                    val lockMinutes = SistemaPersonalRepository.get(this@GuardianHomeActivity).ajustes.lockDurationMinutes.first()
                    val lockUntil = System.currentTimeMillis() + (lockMinutes * 60 * 1000L)
                    prefs.edit().putLong("lock_until", lockUntil).apply()
                    startActivity(Intent(this@GuardianHomeActivity, LockActivity::class.java))
                }
            }
        )
    }

    private fun hasUsagePermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun startGuardianService() {
        val intent = Intent(this, com.centinela.app.guardian.GuardianService::class.java)
        startForegroundService(intent)
    }

    fun registerScreenReceiver(context: Context) {
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        context.registerReceiver(ScreenStateReceiver(), filter)
    }
}

@Composable
fun CentinelaApp(
    hasUsagePermission: Boolean,
    hasOverlayPermission: Boolean,
    isGuardianRunning: Boolean,
    onRequestUsage: () -> Unit,
    onRequestOverlay: () -> Unit,
    onStartGuardian: () -> Unit,
    onOpenContract: () -> Unit,
    onOpenCustomQuestions: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLock: () -> Unit
) {
    val allGranted = hasUsagePermission && hasOverlayPermission

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080808)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
        ) {
            Text("⚔", fontSize = 56.sp)

            Text(
                text = "CENTINELA",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 10.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "TU GUARDIÁN DE ENFOQUE",
                color = Color(0xFF444444),
                fontSize = 11.sp,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            PermissionRow(
                label = "ACCESO DE USO",
                granted = hasUsagePermission,
                onClick = onRequestUsage
            )

            PermissionRow(
                label = "OVERLAY",
                granted = hasOverlayPermission,
                onClick = onRequestOverlay
            )

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(
                visible = allGranted,
                enter = fadeIn() + expandVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isGuardianRunning) {
                        Text(
                            text = "● GUARDIÁN ACTIVO",
                            color = Color(0xFF00CC44),
                            fontSize = 12.sp,
                            letterSpacing = 3.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        SamuraiButton(
                            text = "ACTIVAR CENTINELA",
                            onClick = onStartGuardian
                        )
                    }

                    MenuButton(text = "✦ CONTRATO NOCTURNO", onClick = onOpenContract)
                    MenuButton(text = "✦ MIS PREGUNTAS Y FRASES", onClick = onOpenCustomQuestions)
                    MenuButton(text = "✦ CONFIGURACIÓN", onClick = onOpenSettings)
                        MenuButton(text = "⚔ BLOQUEO TOTAL", onClick = onOpenLock)
                }
            }
        }
    }
}

@Composable
fun MenuButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF222222), RoundedCornerShape(0.dp))
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFF666666),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp
        )
    }
}

@Composable
fun PermissionRow(label: String, granted: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (granted) Color(0xFF1A1A1A) else Color(0xFF333333),
                shape = RoundedCornerShape(0.dp)
            )
            .background(if (isPressed) Color(0xFF111111) else Color(0xFF0D0D0D))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !granted,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (granted) Color(0xFF333333) else Color(0xFF999999),
            fontSize = 12.sp,
            letterSpacing = 3.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (granted) "✓" else "–",
            color = if (granted) Color(0xFF00CC44) else Color(0xFFCC0000),
            fontSize = 16.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun SamuraiButton(text: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (isPressed) 0.97f else 1f)
            .background(Color(0xFFCC0000))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 13.sp,
            letterSpacing = 4.sp,
            fontWeight = FontWeight.Black
        )
    }
}
