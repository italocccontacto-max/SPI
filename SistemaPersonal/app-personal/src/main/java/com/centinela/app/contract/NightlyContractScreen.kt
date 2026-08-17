package com.centinela.app.contract

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NightlyContractScreen(
    vm: NightlyContractViewModel,
    onContractSaved: () -> Unit
) {
    val saved by vm.saved.collectAsState()
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(saved) { if (saved) onContractSaved() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF080808)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            androidx.compose.material3.Text(
                "MAÑANA",
                color = Color(0xFF444444),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp
            )
            androidx.compose.material3.Text(
                "¿Qué vas a hacer?",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .padding(vertical = 8.dp),
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 26.sp
                ),
                cursorBrush = SolidColor(Color(0xFFFFFF00)),
                decorationBox = { inner ->
                    if (text.isEmpty()) androidx.compose.material3.Text(
                        "Escribe al menos 30 caracteres...",
                        color = Color(0xFF333333),
                        fontSize = 16.sp
                    )
                    inner()
                }
            )
            androidx.compose.material3.Button(
                onClick = { vm.saveContract(text) },
                enabled = text.length >= 30,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFFF00),
                    disabledContainerColor = Color(0xFF1A1A1A)
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                androidx.compose.material3.Text(
                    "FIRMADO",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = if (text.length >= 30) Color.Black else Color(0xFF333333)
                )
            }
        }
    }
}
