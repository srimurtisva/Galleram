package com.srimurtiseva.galleram.data.remote

import android.content.Context
import com.srimurtiseva.galleram.ui.main.AuthState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi

class TelegramClient(private val context: Context) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val authState = _authState.asStateFlow()

    private var client: Client? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        setupClient()
    }

    private fun setupClient() {
        client = Client.create({ update ->
            when (update) {
                is TdApi.UpdateAuthorizationState -> {
                    handleAuthState(update.authorizationState)
                }
            }
        }, null, null)
    }

    private fun handleAuthState(state: TdApi.AuthorizationState) {
        when (state) {
            is TdApi.AuthorizationStateWaitTdlibParameters -> {
                val params = TdApi.TdlibParameters().apply {
                    databaseDirectory = context.filesDir.absolutePath + "/tdlib"
                    useMessageDatabase = true
                    useSecretChats = false
                    apiId = TelegramConfig.API_ID
                    apiHash = TelegramConfig.API_HASH
                    systemLanguageCode = "en"
                    deviceModel = TelegramConfig.DEVICE_MODEL
                    systemVersion = TelegramConfig.SYSTEM_VERSION
                    applicationVersion = TelegramConfig.APPLICATION_VERSION
                }
                send(TdApi.SetTdlibParameters(params))
            }
            is TdApi.AuthorizationStateWaitEncryptionKey -> {
                send(TdApi.CheckDatabaseEncryptionKey())
            }
            is TdApi.AuthorizationStateWaitPhoneNumber -> {
                _authState.value = AuthState.LoggedOut
            }
            is TdApi.AuthorizationStateWaitCode -> {
                // We'll need to pass the phone number here if we want to show it in the UI
                _authState.value = AuthState.WaitingForOtp("") 
            }
            is TdApi.AuthorizationStateReady -> {
                _authState.value = AuthState.LoggedIn
            }
            is TdApi.AuthorizationStateLoggingOut -> {
                _authState.value = AuthState.LoggedOut
            }
            is TdApi.AuthorizationStateClosed -> {
                setupClient() // Re-init if closed
            }
        }
    }

    fun startLogin(phoneNumber: String) {
        send(TdApi.SetAuthenticationPhoneNumber(phoneNumber, null))
        _authState.value = AuthState.LoggingIn
    }

    fun submitOtp(otp: String) {
        send(TdApi.CheckAuthenticationCode(otp))
    }

    fun send(query: TdApi.Function<*>, callback: (TdApi.Object) -> Unit = {}) {
        client?.send(query) { result ->
            scope.launch { callback(result) }
        }
    }
}
