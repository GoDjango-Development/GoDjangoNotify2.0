package com.godjango.godjangonotify20.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.godjango.godjangonotify20.core.Runner
import com.godjango.godjangonotify20.data.Repository
import com.godjango.godjangonotify20.data.models.Configuration
import com.godjango.godjangonotify20.data.models.toMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.TimerTask
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltViewModel
class MainViewModel
@Inject constructor(private val repository: Repository, private val runner: Runner):ViewModel()
{
    val unreviewed = MutableStateFlow(0)
    val mutex = Mutex()
    init {
        viewModelScope.launch {
            repository.newMessages.collect{
                unreviewed.value = it
            }
        }
    }

//    private fun autoRun(){
//        protocols.forEach { config ->
//                try {
//                    config.safeFolders.forEach { safeFolder ->
//                                    Log.i("RUN", "$config $safeFolder")
//                                    val tfp = config.toProtocol()
//                                    tfp.tcpTimeOut.connectTimeout = 3000
//                                    tfp.tcpTimeOut.connectRetry = 1
//                                    tfp.tcpTimeOut.dnsResolutionTimeout = 3000
//                                    tfp.connect()
//
//                                    val lsvFileName =
//                                        "lsv2Response-${safeFolder.second}-${config.name}-${Calendar.getInstance().timeInMillis}.txt"
//
//                                    tfp.lsv2Command(
//                                        safeFolder.second,
//                                        "${safeFolder.second}/$lsvFileName"
//                                    )
//
//                                    val lsv2Content = getContent(
//                                        "${safeFolder.second}/$lsvFileName",
//                                        tfp
//                                    )
//                                    val filesToSend =
//                                        lsv2Content.split("${safeFolder.second}/").filter {
//                                            !it.startsWith("lsv2Response") && it.isNotEmpty() && it != " " && !config.alreadyDownloads.contains(
//                                                it
//                                            )
//                                        }
//                                    Log.i("RUN", filesToSend.joinToString { "$it, " })
//                                    val contents =
//                                        mutableListOf<Pair<String, Pair<String, String>>>()
//                                    filesToSend.forEach { fileName ->
//                                        val fileContent = getContent(
//                                            "${safeFolder.second}/$fileName",
//                                            tfp
//                                        )
//                                        if (fileContent.isNotEmpty()) {
//                                            contents.add(
//                                                Pair(
//                                                    fileContent,
//                                                    Pair(safeFolder.first, fileName)
//                                                )
//                                            )
//                                        }
//                                    }
//                                    tfp.delCommand("${safeFolder.second}/$lsvFileName")
//                                    tfp.disconnect()
//                                    updateValues(contents, config){}
//                                 }
//                } catch (e: Exception) {
//                                Log.e(
//                                    "ServerListenerError",
//                                    e.message.toString() + '\n' + e.stackTraceToString()
//                                )
//                            }
//            }
//    }

}

fun onlyTry(function:()->Unit){
    try {
        function()
    }catch (_:Exception){}
}