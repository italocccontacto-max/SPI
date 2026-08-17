package com.centinela.familiar.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.*
import com.sistemapersonal.ui.components.AngularPanel
import com.sistemapersonal.ui.theme.*
import kotlinx.coroutines.tasks.await

data class EventoUi(val tipo: String, val resumen: String, val timestamp: Long, val storagePath: String?)

@Composable
fun DashboardScreen(familyId: String, onCerrarSesion: () -> Unit, onCambiarVinculacion: () -> Unit) {
    var estadoActual by remember { mutableStateOf<Map<String, Any?>>(emptyMap()) }
    var eventos by remember { mutableStateOf<List<EventoUi>>(emptyList()) }

    LaunchedEffect(familyId) {
        try {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
            val token = com.google.firebase.messaging.FirebaseMessaging.getInstance().token.await()
            FirebaseDatabase.getInstance().getReference("familias/$familyId/tokens/$uid").setValue(token)
        } catch (e: Exception) {

        }
    }

    DisposableEffect(familyId) {
        val db = FirebaseDatabase.getInstance()
        val refEstado = db.getReference("familias/$familyId/estado_actual")
        val refEventos = db.getReference("familias/$familyId/eventos").limitToLast(50)

        val estadoListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                @Suppress("UNCHECKED_CAST")
                estadoActual = (snapshot.value as? Map<String, Any?>) ?: emptyMap()
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        val eventosListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                eventos = snapshot.children.mapNotNull { child ->
                    val tipo = child.child("tipo").getValue(String::class.java) ?: return@mapNotNull null
                    val resumen = child.child("resumen").getValue(String::class.java) ?: ""
                    val ts = child.child("timestamp").getValue(Long::class.java) ?: 0L
                    val storagePath = child.child("extra").child("storagePath").getValue(String::class.java)
                    EventoUi(tipo, resumen, ts, storagePath)
                }.sortedByDescending { it.timestamp }
            }
            override fun onCancelled(error: DatabaseError) {}
        }

        refEstado.addValueEventListener(estadoListener)
        refEventos.addValueEventListener(eventosListener)

        onDispose {
            refEstado.removeEventListener(estadoListener)
            refEventos.removeEventListener(eventosListener)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("SISTEMA FAMILIAR", color = Amber, style = MaterialTheme.typography.headlineLarge)
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text("cambiar vinculación", color = Ink2, style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.clickable(onClick = onCambiarVinculacion))
                Text("cerrar sesión", color = Ink2, style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.clickable(onClick = onCerrarSesion))
            }
        }
        Spacer(Modifier.height(16.dp))

        AngularPanel(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("Estado actual", color = Cyan, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                if (estadoActual.isEmpty()) {
                    Text("Sin datos todavía. Se actualiza cuando el dispositivo sincroniza.", color = Ink2)
                } else {
                    estadoActual.forEach { (k, v) ->
                        Text("$k: $v", color = Ink1, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Actividad reciente", color = Ink0, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(eventos) { e ->
                AngularPanel(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(e.resumen, color = Ink0, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                java.text.SimpleDateFormat("dd/MM HH:mm").format(java.util.Date(e.timestamp)),
                                color = Ink2, style = MaterialTheme.typography.labelSmall
                            )
                        }
                        if (e.tipo == "screenshot" && e.storagePath != null) {
                            Spacer(Modifier.height(8.dp))
                            SecureScreenshotImage(e.storagePath)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SecureScreenshotImage(storagePath: String) {
    var bitmap by remember(storagePath) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(storagePath) {
        runCatching {
            val bytes = com.google.firebase.storage.FirebaseStorage.getInstance()
                .reference.child(storagePath)
                .getBytes(10L * 1024L * 1024L)
                .await()
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }
    bitmap?.let {
        androidx.compose.foundation.Image(
            bitmap = it.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(160.dp)
        )
    }
}

@androidx.compose.runtime.Composable
fun SinConfigurarScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Firebase no está configurado todavía", color = Warn, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "Completá los valores en core-network/FirebaseConfig.kt (o inyectalos vía " +
                "BuildConfig desde propiedades de Gradle o variables de entorno) para habilitar Sistema Familiar.",
            color = Ink2, style = MaterialTheme.typography.bodyMedium
        )
    }
}
