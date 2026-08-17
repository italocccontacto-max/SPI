package com.centinela.app

import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.sistemapersonal.data.repo.SistemaPersonalRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class WeeklyMirrorActivity : ComponentActivity() {

    private val httpClient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("centinela", MODE_PRIVATE)
        val apiKey = prefs.getString("api_key", "") ?: ""

        lifecycleScope.launch {
            val repo = SistemaPersonalRepository.get(this@WeeklyMirrorActivity)
            val blockedApps = repo.ajustes.appsProhibidas.first()
            val usageStats = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val now = System.currentTimeMillis()
            val weekStart = now - (7 * 24 * 60 * 60 * 1000L)
            val stats = usageStats.queryUsageStats(UsageStatsManager.INTERVAL_WEEKLY, weekStart, now)
            val pm = packageManager

            val usageSummary = stats
                ?.filter { it.packageName in blockedApps && it.totalTimeInForeground > 0 }
                ?.sortedByDescending { it.totalTimeInForeground }
                ?.take(3)
                ?.joinToString("\n") { stat ->
                    val name = try {
                        pm.getApplicationLabel(pm.getApplicationInfo(stat.packageName, 0)).toString()
                    } catch (e: Exception) { stat.packageName }
                    val hours = stat.totalTimeInForeground / 3_600_000
                    val mins = (stat.totalTimeInForeground % 3_600_000) / 60_000
                    "$name: ${hours}h ${mins}m"
                } ?: "Sin datos de uso esta semana"

            setContent {
                WeeklyMirrorScreen(
                    usageSummary = usageSummary,
                    apiKey = apiKey,
                    httpClient = httpClient,
                    onDone = { finish() }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        httpClient.dispatcher.executorService.shutdown()
    }
}

@Composable
fun WeeklyMirrorScreen(
    usageSummary: String,
    apiKey: String,
    httpClient: OkHttpClient,
    onDone: () -> Unit
) {
    var question by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var answer by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                question = if (apiKey.isNotBlank()) {
                    val body = JSONObject().apply {
                        put("model", "claude-haiku-20240307")
                        put("max_tokens", 200)
                        put("messages", JSONArray().put(JSONObject().apply {
                            put("role", "user")
                            put("content", """Eres CENTINELA. El usuario usó estas apps esta semana:
$usageSummary
Genera UNA SOLA pregunta brutal, específica, personalizada basada en estos datos reales.
No genérica. Usa los números exactos. Sin suavizar. Máximo 2 oraciones. Solo la pregunta, nada más.""")
                        }))
                    }.toString().toRequestBody("application/json".toMediaType())
                    val req = Request.Builder()
                        .url("https://api.anthropic.com/v1/messages")
                        .addHeader("x-api-key", apiKey)
                        .addHeader("anthropic-version", "2023-06-01")
                        .post(body).build()
                    val resp = httpClient.newCall(req).execute()
                    val json = JSONObject(resp.body?.string() ?: "")
                    json.getJSONArray("content").getJSONObject(0).getString("text")
                } else {
                    val firstLine = usageSummary.split("\n").firstOrNull() ?: "apps de distracción"
                    "Esta semana invertiste tiempo en $firstLine. ¿Qué construiste con las horas que no regalaste a una pantalla?"
                }
            } catch (e: Exception) {
                question = "Esta semana el espejo no miente. ¿Qué harás diferente la próxima?"
            }
            isLoading = false
        }
    }

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

            Text("ESPEJO SEMANAL", color = Color(0xFFCC0000), fontSize = 11.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 6.sp)

            Text("ESTA SEMANA", color = Color(0xFF444444), fontSize = 11.sp,
                letterSpacing = 2.sp)

            Box(modifier = Modifier.fillMaxWidth()
                .border(1.dp, Color(0xFF1A1A1A))
                .background(Color(0xFF0D0D0D))
                .padding(20.dp)
            ) {
                Text(usageSummary, color = Color(0xFF666666), fontSize = 13.sp,
                    lineHeight = 22.sp)
            }

            if (isLoading) {
                CircularProgressIndicator(color = Color(0xFFCC0000))
            } else {
                question?.let { q ->
                    Text(q, color = Color.White, fontSize = 20.sp,
                        fontWeight = FontWeight.Black, textAlign = TextAlign.Center,
                        lineHeight = 30.sp)
                }

                BasicTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    modifier = Modifier.fillMaxWidth()
                        .border(1.dp, if (answer.length >= 20) Color(0xFF00CC44) else Color(0xFF333333))
                        .background(Color(0xFF0D0D0D))
                        .padding(16.dp)
                        .heightIn(min = 100.dp),
                    textStyle = TextStyle(color = Color.White, fontSize = 15.sp,
                        lineHeight = 24.sp),
                    cursorBrush = SolidColor(Color(0xFFFFFF00)),
                    decorationBox = { inner ->
                        if (answer.isEmpty()) Text("Responde antes de continuar...",
                            color = Color(0xFF333333), fontSize = 15.sp)
                        inner()
                    }
                )

                Text(
                    if (answer.length < 20) "${20 - answer.length} caracteres mínimo"
                    else "✓ listo para continuar",
                    color = if (answer.length >= 20) Color(0xFF00CC44) else Color(0xFF444444),
                    fontSize = 11.sp
                )

                Box(
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                        .background(if (answer.length >= 20) Color(0xFFCC0000) else Color(0xFF1A1A1A))
                        .clickable(enabled = answer.length >= 20) { onDone() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("CONTINUAR",
                        color = if (answer.length >= 20) Color.White else Color(0xFF333333),
                        fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
