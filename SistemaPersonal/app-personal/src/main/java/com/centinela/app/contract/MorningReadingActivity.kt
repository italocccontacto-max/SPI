package com.centinela.app.contract

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MorningReadingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MorningReadingScreen(onDone = { finish() })
        }
    }
}
