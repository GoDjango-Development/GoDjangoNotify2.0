package com.godjango.godjangonotify20.data.db.database

import androidx.room.*
import com.godjango.godjangonotify20.data.models.Configuration
import com.godjango.godjangonotify20.data.models.Interval
import com.godjango.godjangonotify20.data.models.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigurationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMsg(message: Message)

    @Query("SELECT * FROM messages WHERE archive = 0")
    fun getMessages(): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE archive = 1")
    fun getArchiveMessages(): Flow<List<Message>>

    @Query("SELECT COUNT(id) FROM messages WHERE viewed = 0")
    fun getNewMessages(): Flow<Int>

    @Query("UPDATE messages SET viewed=1 WHERE id=:id")
    suspend fun viewMessage(id:Int)

    @Query("SELECT * FROM messages")
    fun getAllMessages(): Flow<List<Message>>

    @Query("DELETE FROM messages WHERE id=:id")
    suspend fun deleteMessage(id:Int)

    @Query("DELETE FROM messages WHERE viewed = 1")
    suspend fun deleteHistory()

    @Query("UPDATE messages SET archive = 1 WHERE id=:id")
    suspend fun archiveMessage(id:Int)

    @Query("UPDATE messages SET archive = 1, viewed = 1 ")
    suspend fun archiveAllMessages()

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: Configuration)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfigs(configs: List<Configuration>)

    @Update
    suspend fun updateConfig(config: Configuration)

    @Query("UPDATE configuration SET alreadyDownloads =:list")
    suspend fun cleanDownloads(list:MutableList<String>)

    @Query("SELECT * FROM configuration")
    fun getConfigs(): Flow<List<Configuration>>

    @Query("SELECT * FROM configuration WHERE id = :id")
    fun getConfig(id: Int): Flow<Configuration>

    @Query("DELETE FROM configuration WHERE id = :id")
    suspend fun deleteConfig(id: Int)

    @Query("UPDATE configuration SET alreadyDownloads = :downloads WHERE id = :id")
    suspend fun addDownloadFolder(downloads: MutableList<String>,id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterval(interval: Interval)

    @Query("UPDATE interval SET value = :value WHERE id=1")
    suspend fun setInterval(value:Int)

    @Query("SELECT value FROM interval WHERE id=1")
    fun getInterval(): Flow<Int?>
}