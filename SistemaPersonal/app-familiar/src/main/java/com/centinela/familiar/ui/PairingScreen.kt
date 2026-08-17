package com.centinela.familiar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sistemapersonal.ui.components.AngularPanel
import com.sistemapersonal.ui.components.GlowButton
import com.sistemapersonal.ui.theme.*

@Composable
fun PairingScreen(
    cargando: Boolean,
    error: String?,
    onCodigoIngresado: (String) -> Unit
) {
    var codigo by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("VINCULAR DISPOSITIVO", color = Amber, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Ingresá el código temporal de 12 caracteres que genera Sistema Personal. " +
                "El código es de un solo uso y no es la contraseña de la familia.",
            color = Ink2, style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(20.dp))

        AngularPanel(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BasicTextField(
                    value = codigo,
                    onValueChange = { codigo = it.uppercase().filter(Char::isLetterOrDigit).take(12) },
                    textStyle = TextStyle(color = Ink0, letterSpacing = 4.sp, fontSize = 20.sp),
                    cursorBrush = SolidColor(Amber),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (codigo.isEmpty()) Text("CÓDIGO DE 12 CARACTERES", color = Ink3)
                        inner()
                    }
                )
                error?.let { Text(it, color = Warn, style = MaterialTheme.typography.bodySmall) }
                GlowButton(
                    if (cargando) "VINCULANDO…" else "VINCULAR",
                    accent = Amber, glow = AmberGlow,
                    enabled = !cargando && codigo.length == 12,
                    onClick = { onCodigoIngresado(codigo) }
                )
            }
        }
    }
}
