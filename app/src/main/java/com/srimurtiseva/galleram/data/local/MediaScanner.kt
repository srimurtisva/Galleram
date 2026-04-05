package com.srimurtiseva.galleram.data.local

import android.content.Context
import android.provider.MediaStore
import com.srimurtiseva.galleram.data.local.dao.MediaDao
import com.srimurtiseva.galleram.data.local.entities.MediaEntity
import com.srimurtiseva.galleram.utils.HashUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaScanner(private val context: Context, private val mediaDao: MediaDao) {

    suspend fun scanLocalMedia() = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.Video.Media.DURATION
        )

        val cursor = context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            "${MediaStore.MediaColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE} OR " +
            "${MediaStore.MediaColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO}",
            null,
            "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            val dateColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val mimeColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

            while (it.moveToNext()) {
                val localId = it.getString(idColumn)
                val path = it.getString(dataColumn)
                val dateModified = it.getLong(dateColumn)
                val size = it.getLong(sizeColumn)
                val mimeType = it.getString(mimeColumn)
                val duration = it.getLong(durationColumn)
                val type = if (mimeType.startsWith("video")) "video" else "photo"

                // Check if we already know this file
                val existing = mediaDao.getByLocalId(localId)
                if (existing == null) {
                    // New file found! Calculate hash for the "Trinity of Identity"
                    val file = File(path)
                    if (file.exists()) {
                        val hash = HashUtils.calculateSHA256(file.inputStream())
                        
                        val newMedia = MediaEntity(
                            localId = localId,
                            pathUri = path,
                            hashsum = hash,
                            remoteUniqueId = null,
                            messageId = null,
                            mediaType = type,
                            dateModified = dateModified,
                            fileSize = size,
                            duration = duration,
                            syncStatus = "local"
                        )
                        mediaDao.upsert(newMedia)
                    }
                } else if (existing.dateModified != dateModified) {
                    // File was modified - we might need to re-hash or mark for re-sync
                    // For now, just update the local metadata
                }
            }
        }
    }
}
