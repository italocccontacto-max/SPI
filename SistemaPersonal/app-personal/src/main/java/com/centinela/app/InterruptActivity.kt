package com.centinela.app

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import com.sistemapersonal.data.repo.SistemaPersonalRepository
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class InterruptActivity : ComponentActivity() {

    companion object {
        val DEFAULT_QUESTIONS = listOf(
            "¿Esto te acerca a quien quieres ser?",
            "¿Tu yo de mañana te agradecerá esto?",
            "¿Qué deberías estar haciendo AHORA MISMO?",
            "¿Cuántas horas más vas a regalar hoy?",
            "¿Esto es lo que elegiste para tu vida?",
            "¿Si te viera tu yo de hace 5 años, qué pensaría?",
            "¿Estás construyendo o destruyendo?",
            "¿Qué excusa te estás contando ahora mismo?",
            "El tiempo que pierdes hoy, ¿quién lo paga mañana?",
            "¿Esto es urgente o solo cómodo?"
        )
        val DEFAULT_PHRASES = listOf(
            "La disciplina es elegir entre lo que quieres ahora y lo que quieres más.",
            "No hay versión exitosa de ti que haga lo que estás haciendo ahora.",
            "Cada vez que cedes, le enseñas a tu cerebro que puede cederse.",
            "El dolor de la disciplina pesa menos que el peso del arrepentimiento.",
            "Nadie va a venir a salvarte. O lo haces tú o no lo hace nadie.",
            "Lo que haces cuando nadie te ve define quién eres en realidad."
        )
    }

    private val httpClient = OkHttpClient()
    private var videoUri by mutableStateOf<Uri?>(null)

    private val mediaPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val mimeType = contentResolver.getType(it) ?: ""
            val isVideo = mimeType.startsWith("video/")
            videoUri = it
            getSharedPreferences("centinela", MODE_PRIVATE).edit()
                .putString("video_uri", it.toString())
                .putBoolean("media_is_video", isVideo)
                .apply()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("centinela", MODE_PRIVATE)
        val savedUri = prefs.getString("video_uri", null)
        if (savedUri != null) videoUri = Uri.parse(savedUri)
        val mediaIsVideo = prefs.getBoolean("media_is_video", false)
        val videoHasSound = prefs.getBoolean("video_has_sound", true)
        val timeMs = intent.getLongExtra("time_ms", 0L)
        val minutes = timeMs / 60000
        val packageName = intent.getStringExtra("package_name") ?: ""

        val allQuestions = loadAllQuestions(this)
        val allPhrases = loadAllPhrases(this)

        val sessionStartMs = intent.getLongExtra("session_start_ms", System.currentTimeMillis())

        lifecycleScope.launch {
            val hourlyValue = SistemaPersonalRepository.get(this@InterruptActivity).ajustes.hourlyValue.first()
            setContent {
                InterruptScreen(
                    questions = allQuestions,
                    phrases = allPhrases,
                    minutes = minutes,
                    videoUri = videoUri,
                    mediaIsVideo = mediaIsVideo,
                    videoHasSound = videoHasSound,
                    apiKey = prefs.getString("api_key", "") ?: "",
                    httpClient = httpClient,
                    hourlyValue = hourlyValue,
                    sessionStartMs = sessionStartMs,
                    onPickVideo = { mediaPicker.launch(arrayOf("image/*", "video/*")) },
                    onContinue = { finish() },
                    onReturn = { finish() }
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
fun InterruptScreen(
    questions: List<String>,
    phrases: List<String>,
    minutes: Long,
    videoUri: Uri?,
    mediaIsVideo: Boolean,
    videoHasSound: Boolean,
    apiKey: String,
    httpClient: OkHttpClient,
    hourlyValue: Float,
    sessionStartMs: Long,
    onPickVideo: () -> Unit,
    onContinue: () -> Unit,
    onReturn: () -> Unit
) {
    val context = LocalContext.current
    val question = remember { if (questions.isNotEmpty()) questions.random() else "¿Esto te acerca a quien quieres ser?" }
    val phrase = remember { if (phrases.isNotEmpty()) phrases.random() else "La disciplina es elegir entre lo que quieres ahora y lo que quieres más." }
    var aiResponse by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var elapsedMs by remember { mutableStateOf(System.currentTimeMillis() - sessionStartMs) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            elapsedMs = System.currentTimeMillis() - sessionStartMs
        }
    }
    val costSoFar = if (hourlyValue > 0f) {
        val hours = elapsedMs / 3_600_000f
        hours * hourlyValue
    } else 0f

    LaunchedEffect(Unit) {
        if (apiKey.isNotBlank()) {
            isLoading = true
            withContext(Dispatchers.IO) {
                try {
                    val body = JSONObject().apply {
                        put("model", "claude-haiku-20240307")
                        put("max_tokens", 120)
                        put("messages", JSONArray().put(JSONObject().apply {
                            put("role", "user")
                            put("content", "Eres CENTINELA. En 2 oraciones máximo, responde esto con brutalidad directa sin condescendencia: $question")
                        }))
                    }.toString().toRequestBody("application/json".toMediaType())
                    val req = Request.Builder()
                        .url("https://api.anthropic.com/v1/messages")
                        .addHeader("x-api-key", apiKey)
                        .addHeader("anthropic-version", "2023-06-01")
                        .post(body).build()
                    val resp = httpClient.newCall(req).execute()
                    val json = JSONObject(resp.body?.string() ?: "")
                    aiResponse = json.getJSONArray("content").getJSONObject(0).getString("text")
                } catch (e: Exception) { aiResponse = null }
                isLoading = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF080808))) {
        videoUri?.let { uri ->
            if (mediaIsVideo) {
                val player = remember {
                    ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(uri))
                        repeatMode = ExoPlayer.REPEAT_MODE_ALL
                        volume = if (videoHasSound) 1f else 0f
                        prepare(); play()
                    }
                }
                DisposableEffect(Unit) { onDispose { player.release() } }
                AndroidView(
                    factory = { PlayerView(it).apply { this.player = player; useController = false } },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize().background(Color(0xCC000000)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Text("⚠ PAUSA OBLIGATORIA", color = Color(0xFFCC0000),
                fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)

            if (minutes > 0) Text("$minutes MIN", color = Color(0xFF444444),
                fontSize = 11.sp, letterSpacing = 2.sp)

            if (costSoFar > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D0000))
                        .border(1.dp, Color(0xFF440000))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TIEMPO GASTADO HASTA AHORA",
                            color = Color(0xFF660000), fontSize = 9.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "$${"%.2f".format(costSoFar)}",
                            color = Color(0xFFCC0000),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Text(question, color = Color.White, fontSize = 22.sp,
                fontWeight = FontWeight.Black, textAlign = TextAlign.Center, lineHeight = 32.sp)

            Box(
                modifier = Modifier.fillMaxWidth()
                    .border(1.dp, Color(0xFF222222))
                    .background(Color(0xFF0D0D0D))
                    .padding(20.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFFCC0000),
                        modifier = Modifier.align(Alignment.Center).size(24.dp))
                } else {
                    Text(aiResponse ?: phrase, color = Color(0xFFAAAAAA),
                        fontSize = 15.sp, lineHeight = 24.sp, textAlign = TextAlign.Center)
                }
            }

            Spacer(Modifier.height(8.dp))

            val interactionContinue = remember { MutableInteractionSource() }
            val pressedContinue by interactionContinue.collectIsPressedAsState()
            Box(
                modifier = Modifier.fillMaxWidth().height(56.dp)
                    .background(if (pressedContinue) Color(0xFF990000) else Color(0xFFCC0000))
                    .clickable(interactionSource = interactionContinue, indication = null) { onContinue() },
                contentAlignment = Alignment.Center
            ) {
                Text("CONTINUAR DE TODAS FORMAS", color = Color.White,
                    fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
            }

            val interactionReturn = remember { MutableInteractionSource() }
            val pressedReturn by interactionReturn.collectIsPressedAsState()
            Box(
                modifier = Modifier.fillMaxWidth().height(56.dp)
                    .border(1.dp, Color(0xFF00CC44))
                    .background(if (pressedReturn) Color(0xFF004422) else Color.Transparent)
                    .clickable(interactionSource = interactionReturn, indication = null) { onReturn() },
                contentAlignment = Alignment.Center
            ) {
                Text("← VOLVER", color = Color(0xFF00CC44),
                    fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
            }

            Text("cambiar fondo", color = Color(0xFF333333), fontSize = 11.sp,
                modifier = Modifier.clickable { onPickVideo() })

            Spacer(Modifier.height(16.dp))
        }
    }
}
