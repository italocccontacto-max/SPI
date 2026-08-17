package com.centinela.familiar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.sistemapersonal.network.AuthFamiliar
import com.sistemapersonal.ui.components.AngularPanel
import com.sistemapersonal.ui.components.GlowButton
import com.sistemapersonal.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(auth: AuthFamiliar, onLoginOk: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("SISTEMA FAMILIAR", color = Amber, style = MaterialTheme.typography.displayMedium)
        Text("Acceso para familiares invitados", color = Ink2, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        AngularPanel(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Correo", color = Ink2, style = MaterialTheme.typography.labelMedium)
                BasicTextField(
                    value = email, onValueChange = { email = it },
                    textStyle = TextStyle(color = Ink0), cursorBrush = SolidColor(Amber),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Contraseña", color = Ink2, style = MaterialTheme.typography.labelMedium)
                BasicTextField(
                    value = password, onValueChange = { password = it },
                    textStyle = TextStyle(color = Ink0), cursorBrush = SolidColor(Amber),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let { Text(it, color = Red, style = MaterialTheme.typography.bodySmall) }
                GlowButton(
                    text = if (cargando) "INGRESANDO…" else "INGRESAR",
                    accent = Amber, glow = AmberGlow, enabled = !cargando,
                    onClick = {
                        cargando = true
                        error = null
                        scope.launch {
                            val result = auth.iniciarSesion(email.trim(), password)
                            cargando = false
                            result.onSuccess { onLoginOk() }
                                .onFailure { error = "No se pudo iniciar sesión. Verificá tus datos." }
                        }
                    }
                )
            }
        }
    }
}
