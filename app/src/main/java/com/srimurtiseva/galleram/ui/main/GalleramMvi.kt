package com.srimurtiseva.galleram.ui.main

import androidx.paging.PagingData
import com.srimurtiseva.galleram.data.local.entities.MediaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * The single source of truth for the Galleram UI.
 */
data class GalleramState(
    val mediaItems: Flow<PagingData<MediaEntity>> = emptyFlow(),
    val gridColumnCount: Int = 4,        // Dynamic for pinch-to-zoom
    val isSyncing: Boolean = false,
    val isLoading: Boolean = true,
    val authState: AuthState = AuthState.LoggedOut,
    val selectedMedia: MediaEntity? = null,
    val errorMessage: String? = null
)

sealed class AuthState {
    object LoggedOut : AuthState()
    object LoggingIn : AuthState()
    data class WaitingForOtp(val phoneNumber: String) : AuthState()
    data class WaitingForPassword(val hasRecovery: Boolean) : AuthState()
    object LoggedIn : AuthState()
}

/**
 * Intent represents the user's will to change the state.
 */
sealed class GalleramIntent {
    object LoadMedia : GalleramIntent()
    data class UpdateZoom(val delta: Int) : GalleramIntent() // -1 for zoom in, +1 for zoom out
    object SyncNow : GalleramIntent()

    // Auth Intents
    data class StartLogin(val phoneNumber: String) : GalleramIntent()
    data class SubmitOtp(val otp: String) : GalleramIntent()
    data class SubmitPassword(val password: String) : GalleramIntent()
    
    // UI Intents
    data class SelectMedia(val media: MediaEntity?) : GalleramIntent()
}
