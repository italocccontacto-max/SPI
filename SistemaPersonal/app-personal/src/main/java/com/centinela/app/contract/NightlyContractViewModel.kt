package com.centinela.app.contract

import com.sistemapersonal.data.repo.SistemaPersonalRepository
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NightlyContractViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = IntentionContractRepository(
        SistemaPersonalRepository.get(app).intentionContractDao()
    )

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved

    fun saveContract(text: String) {
        viewModelScope.launch {
            repo.saveContract(text)
            _saved.value = true
        }
    }
}
