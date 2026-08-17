package com.centinela.app.contract

import com.sistemapersonal.data.repo.SistemaPersonalRepository
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.sistemapersonal.data.entity.IntentionContractEntity
import kotlinx.coroutines.launch

class MorningReadingViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = IntentionContractRepository(
        SistemaPersonalRepository.get(app).intentionContractDao()
    )

    private val _contract = MutableStateFlow<IntentionContractEntity?>(null)
    val contract: StateFlow<IntentionContractEntity?> = _contract

    private val _readingDone = MutableStateFlow(false)
    val readingDone: StateFlow<Boolean> = _readingDone

    init {
        viewModelScope.launch {
            _contract.value = repo.getLatest()
        }
    }

    fun markAsRead(id: Long) {
        viewModelScope.launch {
            repo.markAsRead(id)
            _readingDone.value = true
        }
    }
}
