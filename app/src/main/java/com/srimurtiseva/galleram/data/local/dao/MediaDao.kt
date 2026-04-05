package com.srimurtiseva.galleram.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.srimurtiseva.galleram.data.local.entities.MediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media ORDER BY dateModified DESC")
    fun getAllMediaFlow(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media ORDER BY dateModified DESC")
    fun getAllMediaPagingSource(): PagingSource<Int, MediaEntity>

    @Query("SELECT * FROM media WHERE hashsum = :hash LIMIT 1")
    suspend fun getByHash(hash: String): MediaEntity?

    @Query("SELECT * FROM media WHERE localId = :localId LIMIT 1")
    suspend fun getByLocalId(localId: String): MediaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(media: MediaEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(media: List<MediaEntity>)

    @Query("UPDATE media SET pathUri = :newPath WHERE hashsum = :hash")
    suspend fun updatePathByHash(hash: String, newPath: String)

    @Query("SELECT localId FROM media")
    suspend fun getAllLocalIds(): List<String>
}
