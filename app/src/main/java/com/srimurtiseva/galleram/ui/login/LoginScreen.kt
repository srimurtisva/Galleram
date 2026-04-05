package com.srimurtiseva.galleram.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.srimurtiseva.galleram.ui.main.AuthState

@Composable
fun LoginScreen(
    authState: AuthState,
    onLogin: (String) -> Unit,
    onOtpSubmit: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (authState) {
            is AuthState.WaitingForOtp -> {
                Text("Enter OTP")
                TextField(value = text, onValueChange = { text = it })
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { onOtpSubmit(text) }) {
                    Text("Submit OTP")
                }
            }
            else -> {
                Text("Enter Phone Number")
                TextField(value = text, onValueChange = { text = it })
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { onLogin(text) }) {
                    Text("Login with Telegram")
                }
            }
        }
    }
}
