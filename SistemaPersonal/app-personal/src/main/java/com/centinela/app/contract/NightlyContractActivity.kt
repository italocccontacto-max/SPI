package com.centinela.app.contract

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider

class NightlyContractActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vm = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[NightlyContractViewModel::class.java]
        setContent {
            NightlyContractScreen(vm = vm, onContractSaved = { finish() })
        }
    }
}
