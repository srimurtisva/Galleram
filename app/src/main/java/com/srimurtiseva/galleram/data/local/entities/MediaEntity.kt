package com.srimurtiseva.galleram.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media")
data class MediaEntity(
    @PrimaryKey val localId: String,          // MediaStore ID
    @ColumnInfo val pathUri: String,          // Local file path
    @ColumnInfo val hashsum: String?,         // SHA-256 for de-duplication
    @ColumnInfo val remoteUniqueId: String?,  // TDLib remote.unique_id
    @ColumnInfo val messageId: Long?,         // Telegram message ID
    @ColumnInfo val mediaType: String,        // "photo" or "video"
    @ColumnInfo val dateModified: Long,       // Timestamp for sorting
    @ColumnInfo val fileSize: Long,           // Size in bytes
    @ColumnInfo val duration: Long = 0,       // Duration if video
    @ColumnInfo val hashtags: String? = null, // Hidden metadata from caption
    @ColumnInfo val syncStatus: String        // "local", "syncing", "synced"
)
