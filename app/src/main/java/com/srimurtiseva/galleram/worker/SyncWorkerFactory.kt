package com.srimurtiseva.galleram.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.srimurtiseva.galleram.data.local.dao.MediaDao
import com.srimurtiseva.galleram.data.remote.TelegramClient

class SyncWorkerFactory(
    private val mediaDao: MediaDao,
    private val telegramClient: TelegramClient
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            SyncWorker::class.java.name ->
                SyncWorker(appContext, workerParameters, mediaDao, telegramClient)
            else -> null
        }
    }
}
