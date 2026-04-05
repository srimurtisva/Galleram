package com.srimurtiseva.galleram

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.srimurtiseva.galleram.data.local.GalleramDatabase
import com.srimurtiseva.galleram.data.remote.TelegramClient
import com.srimurtiseva.galleram.worker.SyncWorkerFactory

class GalleramApplication : Application(), Configuration.Provider {
    
    lateinit var db: GalleramDatabase
    lateinit var telegramClient: TelegramClient

    override fun onCreate() {
        super.onCreate()
        db = GalleramDatabase.getDatabase(this)
        telegramClient = TelegramClient(this)
        
        WorkManager.initialize(
            this,
            workManagerConfiguration
        )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(SyncWorkerFactory(db.mediaDao(), telegramClient))
            .build()
}
