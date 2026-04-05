package com.srimurtiseva.galleram.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.srimurtiseva.galleram.data.local.dao.MediaDao
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

        // Get the "Galleram Cloud" chat ID or create it
        val chatId = getCloudChatId() ?: return Result.retry()

        for (media in unsyncedMedia) {
            mediaDao.updateSyncStatus(media.localId, "syncing")
            val success = uploadMedia(chatId, media.pathUri, media.mediaType)
            if (success) {
                // In a real scenario, we'd get the remoteUniqueId and messageId from TDLib response
                // For now, mark as synced with placeholder
                mediaDao.markAsSynced(media.localId, "remote_${media.localId}", 0L)
            } else {
                mediaDao.updateSyncStatus(media.localId, "local")
            }
        }

        return Result.success()
    }

    private suspend fun getCloudChatId(): Long? = suspendCoroutine { continuation ->
        telegramClient.send(TdApi.GetMe()) { user ->
            if (user is TdApi.User) {
                // Using "Saved Messages" as the default cloud storage
                continuation.resume(user.id)
            } else {
                continuation.resume(null)
            }
        }
    }

    private suspend fun uploadMedia(chatId: Long, path: String, type: String): Boolean = suspendCoroutine { continuation ->
        val content = if (type == "photo") {
            TdApi.InputMessagePhoto(TdApi.InputFileLocal(path), null, null, 0, 0, null, 0)
        } else {
            TdApi.InputMessageVideo(TdApi.InputFileLocal(path), null, null, 0, 0, 0, 0, null, 0)
        }

        telegramClient.send(TdApi.SendMessage(chatId, 0, 0, null, null, content)) { result ->
            continuation.resume(result is TdApi.Message)
        }
    }
}
