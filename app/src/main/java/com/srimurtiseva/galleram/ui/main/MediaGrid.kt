package com.srimurtiseva.galleram.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.srimurtiseva.galleram.data.local.entities.MediaEntity

@Composable
fun MediaGrid(
    items: List<MediaEntity>,
    columnCount: Int,
    onItemClick: (MediaEntity) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columnCount),
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp)
    ) {
        items(items, key = { it.localId }) { item ->
            MediaItemCard(item = item, onClick = { onItemClick(item) })
        }
    }
}

@Composable
fun MediaItemCard(
    item: MediaEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(2.dp)
            .aspectRatio(1f)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = item.pathUri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
