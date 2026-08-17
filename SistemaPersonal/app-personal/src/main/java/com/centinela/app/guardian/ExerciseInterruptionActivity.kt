package com.centinela.app.guardian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.sistemapersonal.data.entity.EntrenamientoLogEntity
import com.sistemapersonal.data.repo.SistemaPersonalRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExerciseInterruptionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val instruccion = intent.getStringExtra("instruccion") ?: "HAZ 10 FLEXIONES"
        val tipo = intent.getStringExtra("tipo") ?: "flexiones"
        val cantidad = intent.getIntExtra("cantidad", 10)
        val segundos = intent.getIntExtra("segundos", 20)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {  }
        })

        setContent {
            ExerciseInterruptionScreen(
                instruccion = instruccion,
                segundosTotales = segundos,
                onCompletado = {
                    lifecycleScope.launch {
                        val repo = SistemaPersonalRepository.get(applicationContext)
                        val fecha = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
                        repo.entrenamientoDao().insertar(
                            EntrenamientoLogEntity(
                                fecha = fecha,
                                tipo = tipo,
                                cantidadOSegundos = cantidad,
                                origen = "exercise_interruption",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                        finish()
                    }
                }
            )
        }
    }
}

@Composable
fun ExerciseInterruptionScreen(
    instruccion: String,
    segundosTotales: Int,
    onCompletado: () -> Unit
) {
    var restante by remember { mutableStateOf(segundosTotales) }

    LaunchedEffect(Unit) {
        while (restante > 0) {
            delay(1000)
            restante--
        }
        onCompletado()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF060606)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                instruccion,
                color = Color(0xFFFFAB13),
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )
            Text(
                "00:%02d".format(restante),
                color = Color.White,
                fontSize = 56.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                "Completa el ejercicio\nantes de continuar.",
                color = Color(0xFF888888),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}
