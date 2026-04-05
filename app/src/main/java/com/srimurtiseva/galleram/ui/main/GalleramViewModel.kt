package com.srimurtiseva.galleram.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.srimurtiseva.galleram.data.local.MediaScanner
import com.srimurtiseva.galleram.data.local.dao.MediaDao
import com.srimurtiseva.galleram.data.remote.TelegramClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GalleramViewModel(
    private val mediaDao: MediaDao,
    private val mediaScanner: MediaScanner,
    private val telegramClient: TelegramClient
) : ViewModel() {

    private val _state = MutableStateFlow(GalleramState())
    val state: StateFlow<GalleramState> = _state.asStateFlow()

    init {
        loadMedia()
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            telegramClient.authState.collectLatest { authState ->
                _state.update { it.copy(authState = authState) }
            }
        }
    }

    fun handleIntent(intent: GalleramIntent) {
        when (intent) {
            is GalleramIntent.LoadMedia -> {} // Handled by init or refresh logic
            is GalleramIntent.UpdateZoom -> updateZoom(intent.delta)
            is GalleramIntent.SyncNow -> syncNow()
            is GalleramIntent.StartLogin -> startTelegramLogin(intent.phoneNumber)
            is GalleramIntent.SubmitOtp -> submitOtp(intent.otp)
            is GalleramIntent.SubmitPassword -> submitPassword(intent.password)
        }
    }

    private fun loadMedia() {
        val pagingFlow = Pager(
            config = PagingConfig(pageSize = 60, enablePlaceholders = true),
            pagingSourceFactory = { mediaDao.getAllMediaPagingSource() }
        ).flow.cachedIn(viewModelScope)

        _state.update { it.copy(mediaItems = pagingFlow, isLoading = false) }
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
        telegramClient.startLogin(phoneNumber)
    }

    private fun submitOtp(otp: String) {
        telegramClient.submitOtp(otp)
    }

    private fun submitPassword(password: String) {
        // TDLib implementation will go here
    }
}
