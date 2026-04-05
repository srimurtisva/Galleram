package com.srimurtiseva.galleram.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.srimurtiseva.galleram.data.local.entities.MediaEntity

@Composable
fun MediaDetailScreen(
    media: MediaEntity,
    onBack: () -> Unit
) {
    AsyncImage(
        model = media.pathUri,
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}
