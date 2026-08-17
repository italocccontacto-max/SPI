package com.centinela.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.sistemapersonal.data.repo.SistemaPersonalRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class DebtActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("centinela", MODE_PRIVATE)
        val distractionMinutes = intent.getLongExtra("distraction_minutes", 0L)
        lifecycleScope.launch {
            val ajustes = SistemaPersonalRepository.get(this@DebtActivity).ajustes
            val debtAction = ajustes.debtAction.first()
            val debtMinutesPerUnit = ajustes.debtMinutesPerUnit.first()
            val unitsOwed = maxOf(1L, distractionMinutes / debtMinutesPerUnit)
            setContent {
                DebtScreen(
                    debtAction = debtAction,
                    unitsOwed = unitsOwed,
                    distractionMinutes = distractionMinutes,
                    onDebtPaid = {
                        prefs.edit().putLong("debt_paid_at", System.currentTimeMillis()).apply()
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun DebtScreen(
    debtAction: String,
    unitsOwed: Long,
    distractionMinutes: Long,
    onDebtPaid: () -> Unit
) {
    var unitsDone by remember { mutableStateOf(0L) }
    val allDone = unitsDone >= unitsOwed

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF080808))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Text("DEUDA DE ATENCIÓN", color = Color(0xFFCC0000), fontSize = 11.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 6.sp)

            Text(
                "Gastaste $distractionMinutes minutos.\nTienes $unitsOwed ${if (unitsOwed == 1L) "acción" else "acciones"} que pagar.",
                color = Color(0xFF666666), fontSize = 14.sp,
                textAlign = TextAlign.Center, lineHeight = 22.sp
            )

            Box(
                modifier = Modifier.fillMaxWidth()
                    .border(1.dp, Color(0xFF330000))
                    .background(Color(0xFF0D0000))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LA DEUDA", color = Color(0xFF660000), fontSize = 9.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (debtAction.isNotBlank()) debtAction
                        else "Ve a Configuración y define tu acción de deuda.",
                        color = Color.White, fontSize = 20.sp,
                        fontWeight = FontWeight.Black, textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("× $unitsOwed ${if (unitsOwed == 1L) "vez" else "veces"}",
                        color = Color(0xFFCC0000), fontSize = 32.sp,
                        fontWeight = FontWeight.Black)
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(8.dp)
                .background(Color(0xFF1A1A1A))) {
                Box(modifier = Modifier
                    .fillMaxWidth(if (unitsOwed > 0) unitsDone.toFloat() / unitsOwed.toFloat() else 0f)
                    .height(8.dp)
                    .background(Color(0xFF00CC44)))
            }

            Text("$unitsDone / $unitsOwed completadas",
                color = if (allDone) Color(0xFF00CC44) else Color(0xFF444444),
                fontSize = 14.sp, fontWeight = FontWeight.Bold)

            if (!allDone) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                        .border(1.dp, Color(0xFF00CC44))
                        .background(Color.Transparent)
                        .clickable { unitsDone++ },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓ COMPLETÉ UNA", color = Color(0xFF00CC44),
                        fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                        .background(Color(0xFF00CC44))
                        .clickable { onDebtPaid() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("DEUDA SALDADA — CONTINUAR", color = Color.Black,
                        fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
