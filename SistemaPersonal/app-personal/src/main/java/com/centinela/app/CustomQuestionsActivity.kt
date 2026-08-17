package com.centinela.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun loadAllQuestions(context: Context): List<String> {
    val prefs = context.getSharedPreferences("centinela", Context.MODE_PRIVATE)
    if (!prefs.contains("all_questions_initialized")) {
        prefs.edit()
            .putString("all_questions_v2", InterruptActivity.DEFAULT_QUESTIONS.joinToString("||"))
            .putBoolean("all_questions_initialized", true)
            .apply()
    }
    val raw = prefs.getString("all_questions_v2", "") ?: ""
    return if (raw.isEmpty()) InterruptActivity.DEFAULT_QUESTIONS
    else raw.split("||").filter { it.isNotBlank() }
}

fun saveAllQuestions(context: Context, questions: List<String>) {
    context.getSharedPreferences("centinela", Context.MODE_PRIVATE)
        .edit().putString("all_questions_v2", questions.joinToString("||")).apply()
}

fun loadAllPhrases(context: Context): List<String> {
    val prefs = context.getSharedPreferences("centinela", Context.MODE_PRIVATE)
    if (!prefs.contains("all_phrases_initialized")) {
        prefs.edit()
            .putString("all_phrases_v2", InterruptActivity.DEFAULT_PHRASES.joinToString("||"))
            .putBoolean("all_phrases_initialized", true)
            .apply()
    }
    val raw = prefs.getString("all_phrases_v2", "") ?: ""
    return if (raw.isEmpty()) InterruptActivity.DEFAULT_PHRASES
    else raw.split("||").filter { it.isNotBlank() }
}

fun saveAllPhrases(context: Context, phrases: List<String>) {
    context.getSharedPreferences("centinela", Context.MODE_PRIVATE)
        .edit().putString("all_phrases_v2", phrases.joinToString("||")).apply()
}

fun loadCustomQuestions(context: Context): List<String> = emptyList()
fun saveCustomQuestions(context: Context, questions: List<String>) {}
fun loadCustomPhrases(context: Context): List<String> = emptyList()
fun saveCustomPhrases(context: Context, phrases: List<String>) {}

class CustomQuestionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CustomQuestionsScreen(onDone = { finish() }) }
    }
}

@Composable
fun CustomQuestionsScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    var questions by remember { mutableStateOf(loadAllQuestions(context)) }
    var phrases by remember { mutableStateOf(loadAllPhrases(context)) }
    var newQuestion by remember { mutableStateOf("") }
    var newPhrase by remember { mutableStateOf("") }
    var editingQ by remember { mutableStateOf(-1) }
    var editingP by remember { mutableStateOf(-1) }
    var editQText by remember { mutableStateOf("") }
    var editPText by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF080808))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text("TUS PREGUNTAS", color = Color(0xFF444444), fontSize = 11.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 6.sp)
                Spacer(Modifier.height(12.dp))
            }

            items(questions.size) { i ->
                val q = questions[i]
                if (editingQ == i) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        BasicTextField(
                            value = editQText, onValueChange = { editQText = it },
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                            cursorBrush = SolidColor(Color(0xFFFFFF00))
                        )
                        Text("✓", color = Color(0xFF00CC44), fontSize = 18.sp,
                            modifier = Modifier.clickable {
                                if (editQText.isNotBlank()) {
                                    val updated = questions.toMutableList()
                                    updated[i] = editQText
                                    questions = updated
                                    saveAllQuestions(context, questions)
                                }
                                editingQ = -1
                            })
                        Spacer(Modifier.width(12.dp))
                        Text("✕", color = Color(0xFFCC0000), fontSize = 18.sp,
                            modifier = Modifier.clickable { editingQ = -1 })
                    }
                } else {
                    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(q, color = Color(0xFFCCCCCC), fontSize = 14.sp,
                            modifier = Modifier.weight(1f))
                        Row {
                            Text("✎", color = Color(0xFF444444), fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 8.dp).clickable {
                                    editingQ = i; editQText = q
                                })
                            Text("✕", color = Color(0xFF444444), fontSize = 16.sp,
                                modifier = Modifier.clickable {
                                    val updated = questions.toMutableList()
                                    updated.removeAt(i)
                                    questions = updated
                                    saveAllQuestions(context, questions)
                                })
                        }
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF1A1A1A)))
            }

            item {
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = newQuestion, onValueChange = { newQuestion = it },
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                        cursorBrush = SolidColor(Color(0xFFFFFF00)),
                        decorationBox = { inner ->
                            if (newQuestion.isEmpty()) Text("Nueva pregunta...",
                                color = Color(0xFF333333), fontSize = 14.sp)
                            inner()
                        }
                    )
                    Text("+ AGREGAR",
                        color = if (newQuestion.length > 5) Color(0xFFFFFF00) else Color(0xFF333333),
                        fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            if (newQuestion.length > 5) {
                                val updated = questions + newQuestion
                                questions = updated
                                saveAllQuestions(context, questions)
                                newQuestion = ""
                            }
                        })
                }
                Spacer(Modifier.height(24.dp))
            }

            item {
                Text("TUS FRASES", color = Color(0xFF444444), fontSize = 11.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 6.sp)
                Spacer(Modifier.height(12.dp))
            }

            items(phrases.size) { i ->
                val p = phrases[i]
                if (editingP == i) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        BasicTextField(
                            value = editPText, onValueChange = { editPText = it },
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                            cursorBrush = SolidColor(Color(0xFFFFFF00))
                        )
                        Text("✓", color = Color(0xFF00CC44), fontSize = 18.sp,
                            modifier = Modifier.clickable {
                                if (editPText.isNotBlank()) {
                                    val updated = phrases.toMutableList()
                                    updated[i] = editPText
                                    phrases = updated
                                    saveAllPhrases(context, phrases)
                                }
                                editingP = -1
                            })
                        Spacer(Modifier.width(12.dp))
                        Text("✕", color = Color(0xFFCC0000), fontSize = 18.sp,
                            modifier = Modifier.clickable { editingP = -1 })
                    }
                } else {
                    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(p, color = Color(0xFFCCCCCC), fontSize = 14.sp,
                            modifier = Modifier.weight(1f))
                        Row {
                            Text("✎", color = Color(0xFF444444), fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 8.dp).clickable {
                                    editingP = i; editPText = p
                                })
                            Text("✕", color = Color(0xFF444444), fontSize = 16.sp,
                                modifier = Modifier.clickable {
                                    val updated = phrases.toMutableList()
                                    updated.removeAt(i)
                                    phrases = updated
                                    saveAllPhrases(context, phrases)
                                })
                        }
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF1A1A1A)))
            }

            item {
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = newPhrase, onValueChange = { newPhrase = it },
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                        cursorBrush = SolidColor(Color(0xFFFFFF00)),
                        decorationBox = { inner ->
                            if (newPhrase.isEmpty()) Text("Nueva frase...",
                                color = Color(0xFF333333), fontSize = 14.sp)
                            inner()
                        }
                    )
                    Text("+ AGREGAR",
                        color = if (newPhrase.length > 5) Color(0xFFFFFF00) else Color(0xFF333333),
                        fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            if (newPhrase.length > 5) {
                                val updated = phrases + newPhrase
                                phrases = updated
                                saveAllPhrases(context, phrases)
                                newPhrase = ""
                            }
                        })
                }
                Spacer(Modifier.height(32.dp))
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                        .background(Color(0xFFCC0000))
                        .clickable { onDone() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("GUARDAR Y SALIR", color = Color.White, fontSize = 13.sp,
                        fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
