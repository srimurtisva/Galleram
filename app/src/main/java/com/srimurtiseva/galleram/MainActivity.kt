package com.srimurtiseva.galleram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.srimurtiseva.galleram.data.local.GalleramDatabase
import com.srimurtiseva.galleram.data.local.MediaScanner
import com.srimurtiseva.galleram.ui.main.GalleramViewModel
import com.srimurtiseva.galleram.ui.main.MediaGrid
import com.srimurtiseva.galleram.ui.theme.GalleramTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Manual DI for the skeleton
        val db = GalleramDatabase.getDatabase(this)
        val mediaDao = db.mediaDao()
        val scanner = MediaScanner(this, mediaDao)
        val viewModel = GalleramViewModel(mediaDao, scanner)

        setContent {
            GalleramTheme {
                val state by viewModel.state.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        } else {
                            MediaGrid(
                                items = state.mediaItems,
                                columnCount = state.gridColumnCount,
                                onItemClick = { /* Handle click */ }
                            )
                        }
                    }
                }
            }
        }
    }
}
