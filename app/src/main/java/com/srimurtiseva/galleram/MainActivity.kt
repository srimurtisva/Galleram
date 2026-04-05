package com.srimurtiseva.galleram

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.srimurtiseva.galleram.data.local.MediaScanner
import com.srimurtiseva.galleram.ui.detail.MediaDetailScreen
import com.srimurtiseva.galleram.ui.login.LoginScreen
import com.srimurtiseva.galleram.ui.main.AuthState
import com.srimurtiseva.galleram.ui.main.GalleramIntent
import com.srimurtiseva.galleram.ui.main.GalleramViewModel
import com.srimurtiseva.galleram.ui.main.MediaGrid
import com.srimurtiseva.galleram.ui.theme.GalleramTheme

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as GalleramApplication
        val db = app.db
        val mediaDao = db.mediaDao()
        val scanner = MediaScanner(this, mediaDao)
        val telegramClient = app.telegramClient
        val workManager = androidx.work.WorkManager.getInstance(applicationContext)
        val viewModel = GalleramViewModel(workManager, mediaDao, scanner, telegramClient)

        setContent {
            GalleramTheme {
                val state by viewModel.state.collectAsState()

                val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
                } else {
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                }

                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { result ->
                    if (result.values.all { it }) {
                        viewModel.handleIntent(GalleramIntent.SyncNow)
                    }
                }

                LaunchedEffect(Unit) {
                    val allGranted = permissions.all {
                        ContextCompat.checkSelfPermission(this@MainActivity, it) == PackageManager.PERMISSION_GRANTED
                    }
                    if (allGranted) {
                        viewModel.handleIntent(GalleramIntent.SyncNow)
                    } else {
                        launcher.launch(permissions)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (state.authState is AuthState.LoggedIn && state.selectedMedia == null) {
                            CenterAlignedTopAppBar(
                                title = { Text("Galleram") },
                                actions = {
                                    IconButton(onClick = { viewModel.handleIntent(GalleramIntent.UpdateZoom(-1)) }) {
                                        Icon(Icons.Default.Add, contentDescription = "Zoom In")
                                    }
                                    IconButton(
                                        onClick = { viewModel.handleIntent(GalleramIntent.SyncNow) },
                                        enabled = !state.isSyncing
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Sync Now")
                                    }
                                }
                            )
                        }
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        val selectedMedia = state.selectedMedia
                        if (selectedMedia != null) {
                            BackHandler {
                                viewModel.handleIntent(GalleramIntent.SelectMedia(null))
                            }
                            MediaDetailScreen(
                                media = selectedMedia,
                                onBack = { viewModel.handleIntent(GalleramIntent.SelectMedia(null)) }
                            )
                        } else {
                            when (val authState = state.authState) {
                                is AuthState.LoggedIn -> {
                                    if (state.isLoading) {
                                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                    } else {
                                        MediaGrid(
                                            items = state.mediaItems,
                                            columnCount = state.gridColumnCount,
                                            onItemClick = { viewModel.handleIntent(GalleramIntent.SelectMedia(it)) },
                                            onZoom = { delta: Int -> viewModel.handleIntent(GalleramIntent.UpdateZoom(delta)) }
                                        )
                                    }
                                    if (state.isSyncing) {
                                        LinearProgressIndicator(
                                            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)
                                        )
                                    }
                                }
                                else -> {
                                    LoginScreen(
                                        authState = authState,
                                        onLogin = { viewModel.handleIntent(GalleramIntent.StartLogin(it)) },
                                        onOtpSubmit = { viewModel.handleIntent(GalleramIntent.SubmitOtp(it)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
