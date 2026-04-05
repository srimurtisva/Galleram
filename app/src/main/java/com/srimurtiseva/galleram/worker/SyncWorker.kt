package com.srimurtiseva.galleram.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.srimurtiseva.galleram.data.local.dao.MediaDao
import com.srimurtiseva.galleram.data.local.entities.MediaEntity
import com.srimurtiseva.galleram.data.remote.TelegramClient
import org.drinkless.tdlib.TdApi
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SyncWorker(
    context: Context,
    params: WorkerParameters,
    private val mediaDao: MediaDao,
    private val telegramClient: TelegramClient
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val unsyncedMedia = mediaDao.getUnsyncedMedia()
        if (unsyncedMedia.isEmpty()) return Result.success()

        val chatId = getCloudChatId() ?: return Result.retry()

        for (media in unsyncedMedia) {
            mediaDao.updateSyncStatus(media.localId, "syncing")
            val caption = generateCaption(media)
            val success = uploadMedia(chatId, media.pathUri, media.mediaType, caption)
            if (success) {
                // Ideally, we'd extract actual remote IDs from TDLib response
                mediaDao.markAsSynced(media.localId, "remote_${media.localId}", 0L)
            } else {
                mediaDao.updateSyncStatus(media.localId, "local")
            }
        }

        return Result.success()
    }

    private fun generateCaption(media: MediaEntity): TdApi.FormattedText {
        val hashtags = mutableListOf<String>()
        hashtags.add("#galleram")
        hashtags.add("#hash_${media.hashsum}")
        hashtags.add("#type_${media.mediaType}")
        
        val captionText = hashtags.joinToString(" ")
        return TdApi.FormattedText(captionText, emptyArray<TdApi.TextEntity>())
    }

    private suspend fun getCloudChatId(): Long? = suspendCoroutine { continuation ->
        telegramClient.send(TdApi.GetMe()) { user ->
            if (user is TdApi.User) {
                continuation.resume(user.id)
            } else {
                continuation.resume(null)
            }
        }
    }

    private suspend fun uploadMedia(
        chatId: Long, 
        path: String, 
        type: String, 
        caption: TdApi.FormattedText
    ): Boolean = suspendCoroutine { continuation ->
        val content = if (type == "photo") {
            TdApi.InputMessagePhoto(TdApi.InputFileLocal(path), null, null, 0, 0, caption, 0)
        } else {
            TdApi.InputMessageVideo(TdApi.InputFileLocal(path), null, null, 0, 0, 0, 0, caption, 0)
        }

        telegramClient.send(TdApi.SendMessage(chatId, 0, 0, null, null, content)) { result ->
            continuation.resume(result is TdApi.Message)
        }
    }
}
