package com.godjango.godjangonotify20.data

import android.annotation.SuppressLint
import com.godjango.godjangonotify20.core.configDownloads
import com.godjango.godjangonotify20.data.db.database.ConfigurationDao
import com.godjango.godjangonotify20.data.models.Configuration
import com.godjango.godjangonotify20.data.models.Interval
import com.godjango.godjangonotify20.data.models.Message
import com.nerox.client.Tfprotocol
import java.io.IOException
import java.io.OutputStream
import javax.inject.Inject

class Repository @Inject constructor(
    private val dao: ConfigurationDao
) {

    val interval = dao.getInterval()
    val configuration = dao.getConfigs()
    suspend fun insertConfig(configuration: Configuration) = dao.insertConfig(configuration)
    suspend fun insertConfigs(configurations: List<Configuration>) = dao.insertConfigs(configurations)
    suspend fun updateConfig(configuration: Configuration) = dao.updateConfig(configuration)
    suspend fun deleteConfig(id: Int) = dao.deleteConfig(id)

    val newMessages = dao.getNewMessages()
    val unsavedMessages = dao.getMessages()
    val savedMessages = dao.getArchiveMessages()
    suspend fun insertMessage(message: Message) = dao.insertMsg(message)
    suspend fun deleteMessage(id:Int) = dao.deleteMessage(id)
    suspend fun archiveMessage(id:Int) = dao.archiveMessage(id)
    suspend fun viewed(id: Int) = dao.viewMessage(id)
    suspend fun archiveAllMessages() = dao.archiveAllMessages()
    suspend fun cleanHistory() = dao.deleteHistory()
    suspend fun cleanDownloads() {
        dao.deleteAllMessages()
        dao.cleanDownloads(mutableListOf())
    }

    suspend fun addFolder(downloads: MutableList<String>, id: Int) = dao.addDownloadFolder(downloads,id)

    suspend fun setInterval(value:Int) = dao.setInterval(value)
    suspend fun addInterval(value:Int) = dao.insertInterval(Interval(value=value))
}

fun Tfprotocol.getStringContent(filePath: String):String{
    if(!(isConnect)) connect()

    var content = ""
    sdownCommand(filePath, object : OutputStream() {
        override fun write(p0: Int) {}
        @SuppressLint("NewApi")
        @Throws(IOException::class)
        override fun write(b: ByteArray) {
            content =  b.toString(Charsets.UTF_8)
        }
    }, 3)

    return content
}