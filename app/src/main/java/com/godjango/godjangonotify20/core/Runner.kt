package com.godjango.godjangonotify20.core

import android.content.res.Resources
import android.util.Log
import com.godjango.godjangonotify20.R
import com.godjango.godjangonotify20.data.Repository
import com.godjango.godjangonotify20.data.getStringContent
import com.godjango.godjangonotify20.data.models.Configuration
import com.godjango.godjangonotify20.data.models.toMessage
import com.godjango.godjangonotify20.data.models.toProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Calendar
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import javax.inject.Inject
import javax.inject.Singleton

var configDownloads = mutableMapOf<Int,HashSet<String>>()

@Singleton
class Runner @Inject constructor(
    private val repository: Repository
)  {
    var myExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    var future: ScheduledFuture<*>?=null
    suspend fun autoRun(protocols:List<Configuration>) {
        protocols.forEach{config->
            try {
                config.safeFolders.forEach { safeFolder ->
                    Log.i("RUN", "$config $safeFolder")
                    val tfp = config.toProtocol()
                    tfp.tcpTimeOut.connectTimeout = 3000
                    tfp.tcpTimeOut.connectRetry = 1
                    tfp.tcpTimeOut.dnsResolutionTimeout = 3000
                    tfp.connect()

                    val lsvFileName =
                        "lsv2Response-${safeFolder.second}-${config.uuid}.txt".replace("/","")

                    tfp.lsv2Command(
                        safeFolder.second,
                        "${safeFolder.second}/$lsvFileName"
                    )

                    val lsv2Content = tfp.getStringContent("${safeFolder.second}/$lsvFileName")

                    val cacheDownloads = configDownloads[config.id]?: hashSetOf()
                    println("cache $cacheDownloads")
                    val filesToSend =
                        lsv2Content.split("\n").filter {
                            !it.contains("lsv2Response") &&
                                    it.isNotEmpty() &&
                                    it != " " &&
                                    !config.alreadyDownloads.contains(it) &&
                                    !cacheDownloads.contains(it)
                        }
                    Log.i("RUN", filesToSend.joinToString { "$it, " })
                    var isDownload = false
                    filesToSend.forEach { fileName ->
                        val fileContent = tfp.getStringContent(fileName)
                        config.alreadyDownloads.add(fileName)
                        isDownload= true
                        if (fileContent.isNotEmpty()) {
                            val json = JSONObject(fileContent)
                            withContext(Dispatchers.IO){
                                repository.insertMessage(
                                    json.toMessage(
                                        config.name?:"${Resources.getSystem().getString(R.string.new_configuration)} ${config.id}",
                                        safeFolder.first
                                    )
                                )
                            }
                        }
                    }
                    tfp.delCommand("${safeFolder.second}/$lsvFileName")
                    tfp.disconnect()
                    withContext(Dispatchers.IO) {
                        if (isDownload) repository.addFolder(config.alreadyDownloads, config.id)
                    }
                }
            } catch (e: Exception) {
                Log.e(
                    "ServerListenerError",
                    e.message.toString() + '\n' + e.stackTraceToString()
                )
            }
        }
    }
}