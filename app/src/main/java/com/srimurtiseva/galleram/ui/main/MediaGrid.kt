package com.srimurtiseva.galleram.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.srimurtiseva.galleram.data.local.entities.MediaEntity
import kotlinx.coroutines.flow.Flow

@Composable
fun MediaGrid(
    items: Flow<androidx.paging.PagingData<MediaEntity>>,
    columnCount: Int,
    onItemClick: (MediaEntity) -> Unit,
    onZoom: (Int) -> Unit // -1 for zoom in (fewer columns), +1 for zoom out (more columns)
) {
    val pagingItems = items.collectAsLazyPagingItems()
    val state = rememberTransformableState { zoomChange, _, _ ->
        if (zoomChange > 1.05f) {
            onZoom(-1)
        } else if (zoomChange < 0.95f) {
            onZoom(1)
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columnCount),
        modifier = Modifier
            .fillMaxSize()
            .transformable(state = state),
        contentPadding = PaddingValues(2.dp)
    ) {
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey { it.localId }
        ) { index ->
            pagingItems[index]?.let { item ->
                MediaItemCard(item = item, onClick = { onItemClick(item) })
            }
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
