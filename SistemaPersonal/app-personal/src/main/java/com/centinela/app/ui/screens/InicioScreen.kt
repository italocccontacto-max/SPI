package com.centinela.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.centinela.app.gamificacion.SoundManager
import com.centinela.app.R
import com.centinela.app.ui.hoyYyyyMMdd
import com.centinela.app.ui.rememberGamificacionEngine
import com.centinela.app.ui.rememberRepo
import com.sistemapersonal.data.entity.StreakEntity
import com.sistemapersonal.model.Modulo
import com.sistemapersonal.ui.components.AngularPanel
import com.sistemapersonal.ui.components.CelebrationPopup
import com.sistemapersonal.ui.components.DonutChart
import com.sistemapersonal.ui.components.LeaderboardCard
import com.sistemapersonal.ui.components.LeaderboardEntry
import com.sistemapersonal.ui.components.SystemCore
import com.sistemapersonal.ui.theme.*

@Composable
fun InicioScreen(onIrAModulo: (Modulo) -> Unit) {
    val repo = rememberRepo()
    val streak by repo.streakDao().observar().collectAsState(initial = StreakEntity())
    val streakData = streak ?: StreakEntity()
    val puntosTotal by repo.pointsDao().totalObservable().collectAsState(initial = 0)
    var minutosHoy by remember { mutableStateOf(0L) }

    val actividadHoy by repo.activityDao().porDia(hoyYyyyMMdd()).collectAsState(initial = emptyList())
    LaunchedEffect(actividadHoy) {
        minutosHoy = actividadHoy.sumOf { it.durationMs } / 60000
    }

    val rankingSemanal by repo.pointsDao().rankingSemanalObservable().collectAsState(initial = emptyList())
    val entradasLeaderboard = remember(rankingSemanal) {
        rankingSemanal.mapIndexed { index, fila ->
            LeaderboardEntry(etiqueta = fila.semana, puntos = fila.puntos, esActual = index == 0)
        }
    }

    val gamificacionEngine = rememberGamificacionEngine()
    val scope = rememberCoroutineScope()
    var eventoCelebracion by remember { mutableStateOf<com.sistemapersonal.domain.EventoCelebracion?>(null) }
    LaunchedEffect(Unit) {
        gamificacionEngine.celebraciones.collect { evento ->
            eventoCelebracion = evento
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SystemCore(
            accent = Amber,
            intensity = 1.3f,
            density = 1.4f,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(360.dp)
                .graphicsLayer { alpha = 0.42f }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo_sistema_personal),
                        contentDescription = "Logo de Sistema Personal",
                        modifier = Modifier.size(74.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "SISTEMA PERSONAL",
                            style = MaterialTheme.typography.displayMedium,
                            color = Amber,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "CENTRO DE CONTROL • VISIÓN GENERAL",
                            style = MaterialTheme.typography.labelMedium,
                            color = Ink3
                        )
                    }
                }
                AngularPanel(
                    fill = Ok.copy(alpha = 0.05f),
                    borderColor = Ok.copy(alpha = 0.28f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("●", color = Ok, style = MaterialTheme.typography.labelSmall)
                        Text("SISTEMA ACTIVO", color = Ok, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AngularPanel(
                    modifier = Modifier.weight(1f),
                    borderColor = Amber.copy(alpha = 0.28f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DonutChart(
                            value = (streakData.diasConsecutivos.coerceAtMost(30)) / 30f,
                            color = Amber,
                            label = "${streakData.diasConsecutivos}"
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("RACHA ACTUAL", style = MaterialTheme.typography.labelMedium, color = Ink2)
                            Text("${streakData.diasConsecutivos} días", style = MaterialTheme.typography.titleLarge, color = Ink0)
                            Text("mejor: ${streakData.mejorRacha}", style = MaterialTheme.typography.bodySmall, color = Ink2)
                        }
                    }
                }

                AngularPanel(
                    modifier = Modifier.weight(1f),
                    borderColor = Mint.copy(alpha = 0.28f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("PUNTOS ACUMULADOS", style = MaterialTheme.typography.labelMedium, color = Ink2)
                        Text("$puntosTotal", style = MaterialTheme.typography.displayMedium, color = Mint)
                        Text("progreso total registrado", style = MaterialTheme.typography.bodySmall, color = Ink2)
                    }
                }

                AngularPanel(
                    modifier = Modifier.weight(1f),
                    borderColor = Cyan.copy(alpha = 0.28f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("USO DE HOY", style = MaterialTheme.typography.labelMedium, color = Ink2)
                        Text("$minutosHoy min", style = MaterialTheme.typography.displayMedium, color = Cyan)
                        Text("actividad registrada", style = MaterialTheme.typography.bodySmall, color = Ink2)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(0.7f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("MÓDULOS", style = MaterialTheme.typography.titleLarge, color = Ink0)
                    Text(
                        "Selecciona un módulo para entrar en modo operativo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink2
                    )
                    LazyVerticalGrid(
                        modifier = Modifier.fillMaxWidth(),
                        columns = GridCells.Adaptive(170.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(Modulo.entries.filter { it != Modulo.INICIO }) { modulo ->
                            val colores = accentFor(modulo.theme)
                            AngularPanel(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onIrAModulo(modulo) },
                                borderColor = colores.accent.copy(alpha = 0.38f),
                                glowColor = if (modulo == Modulo.GUARDIAN) colores.glow else null
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(82.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        modulo.roman ?: "•",
                                        color = colores.accent,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            modulo.label,
                                            color = Ink0,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text("→", color = colores.accent, style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(0.3f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("RANKING", style = MaterialTheme.typography.titleLarge, color = Ink0)
                    Text(
                        "Rendimiento semanal",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink2
                    )
                    LeaderboardCard(entradas = entradasLeaderboard)
                }
            }
        }

        val evento = eventoCelebracion
        CelebrationPopup(
            titulo = evento?.titulo ?: "",
            subtitulo = evento?.subtitulo,
            accent = Mint,
            visible = evento != null,
            onSonido = { SoundManager.reproducirCelebracion(scope) },
            onDismiss = { eventoCelebracion = null }
        )
    }
}
