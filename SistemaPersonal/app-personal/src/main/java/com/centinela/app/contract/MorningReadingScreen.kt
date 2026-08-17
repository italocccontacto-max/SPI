package com.centinela.app.contract

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MorningReadingScreen(onDone: () -> Unit) {
    val vm: MorningReadingViewModel = viewModel()
    val contract by vm.contract.collectAsState()
    val readingDone by vm.readingDone.collectAsState()
    val context = LocalContext.current
    var ttsReady by remember { mutableStateOf(false) }
    var ttsFinished by remember { mutableStateOf(false) }
    var displayedText by remember { mutableStateOf("") }
    val ttsManager = remember { ContractTtsManager(context) }

    LaunchedEffect(readingDone) { if (readingDone) onDone() }

    LaunchedEffect(contract) {
        contract?.let {
            ttsManager.init {
                ttsReady = true
                ttsManager.speak(it.contractText) { ttsFinished = true }
            }
        }
    }

    LaunchedEffect(ttsReady, contract) {
        contract?.let { c ->
            if (ttsReady) {
                c.contractText.forEachIndexed { i, char ->
                    kotlinx.coroutines.delay(60L)
                    displayedText = c.contractText.take(i + 1)
                }
            }
        }
    }

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
                "HOY DIJISTE",
                color = Color(0xFF444444),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp
            )
            androidx.compose.material3.Text(
                displayedText,
                color = Color.White,
                fontSize = 18.sp,
                lineHeight = 28.sp,
                textAlign = TextAlign.Center
            )
            androidx.compose.material3.Button(
                onClick = { contract?.let { vm.markAsRead(it.id) } },
                enabled = ttsFinished,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF0000),
                    disabledContainerColor = Color(0xFF1A1A1A)
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                androidx.compose.material3.Text(
                    "LO ESCUCHO",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = if (ttsFinished) Color.White else Color(0xFF333333)
                )
            }
        }
    }
}
