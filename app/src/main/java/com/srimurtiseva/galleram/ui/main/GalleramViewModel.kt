package com.srimurtiseva.galleram.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.srimurtiseva.galleram.data.local.MediaScanner
import com.srimurtiseva.galleram.data.local.dao.MediaDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GalleramViewModel(
    private val mediaDao: MediaDao,
    private val mediaScanner: MediaScanner
) : ViewModel() {

    private val _state = MutableStateFlow(GalleramState())
    val state: StateFlow<GalleramState> = _state.asStateFlow()

    init {
        handleIntent(GalleramIntent.LoadMedia)
    }

    fun handleIntent(intent: GalleramIntent) {
        when (intent) {
            is GalleramIntent.LoadMedia -> loadMedia()
            is GalleramIntent.UpdateZoom -> updateZoom(intent.delta)
            is GalleramIntent.SyncNow -> syncNow()
            is GalleramIntent.StartLogin -> startTelegramLogin(intent.phoneNumber)
            is GalleramIntent.SubmitOtp -> submitOtp(intent.otp)
            is GalleramIntent.SubmitPassword -> submitPassword(intent.password)
        }
    }

    private fun loadMedia() {
        viewModelScope.launch {
            mediaDao.getAllMediaFlow().collectLatest { items ->
                _state.update { it.copy(mediaItems = items, isLoading = false) }
            }
        }
    }

    private fun updateZoom(delta: Int) {
        _state.update { 
            val newCount = (it.gridColumnCount + delta).coerceIn(2, 6)
            it.copy(gridColumnCount = newCount)
        }
    }

    private fun syncNow() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true) }
            mediaScanner.scanLocalMedia()
            _state.update { it.copy(isSyncing = false) }
        }
    }

    private fun startTelegramLogin(phoneNumber: String) {
        // TDLib implementation will go here
        _state.update { it.copy(authState = AuthState.LoggingIn) }
    }

    private fun submitOtp(otp: String) {
        // TDLib implementation will go here
    }

    private fun submitPassword(password: String) {
        // TDLib implementation will go here
    }
}
