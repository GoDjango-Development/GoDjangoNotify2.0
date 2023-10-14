package com.godjango.godjangonotify20.core

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.godjango.godjangonotify20.MainActivity
import com.godjango.godjangonotify20.R
import com.godjango.godjangonotify20.data.Repository
import com.godjango.godjangonotify20.ui.viewmodel.onlyTry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.system.exitProcess

@AndroidEntryPoint
class ProtocolsService:Service() {

    @Inject lateinit var repository:Repository
    @Inject lateinit var runner:Runner

    private var amount = 0
    override fun onBind(p0: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            Actions.START.toString() -> start()
            Actions.STOP.toString() -> {
                stopSelf()
                exitProcess(0)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
    private fun start(){
        CoroutineScope(Dispatchers.IO).launch {
            run()
        }
        val stopIntent = Intent(this,this::class.java).also {
            it.action = Actions.STOP.toString()
        }
        val pendingStopIntent = PendingIntent.getService(this,1,stopIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        val stopAction = NotificationCompat.Action.Builder(android.R.drawable.ic_lock_power_off,getString(android.R.string.cancel),pendingStopIntent).build()
        val notification = NotificationCompat.Builder(this,"protocols_channel")
            .setSmallIcon(R.drawable.without_bg)
            .setContentTitle(getString(R.string.running))
            .setContentIntent(contentIntent)
            .addAction(stopAction)
            .build()
        startForeground(1,notification)

        CoroutineScope(Dispatchers.IO).launch {
            repository.newMessages.collect{
                if(it!=amount){
                    val updateNotification = NotificationCompat.Builder(this@ProtocolsService,"protocols_channel")
                        .setSmallIcon(R.drawable.without_bg)
                        .addAction(stopAction)
                        .setContentIntent(contentIntent)
                        .setContentText(if(it == 0) getString(R.string.up_to_date) else "$it "+ getString(R.string.unread_notifications))
                        .build()
                    startForeground(2,updateNotification)
                    amount=it
                }
            }
        }
    }

    private suspend fun run() {
        combine(repository.configuration, repository.interval)
        { config, inter ->
            Pair(config, inter)
        }
            .collectLatest {
                runBlocking {
                    it.first.forEach {config->
                        configDownloads[config.id] = config.alreadyDownloads.toHashSet()
                    }
                    onlyTry {
                        runner.future?.cancel(true)
                    }
                    onlyTry {
                        runner.myExecutor.shutdownNow()
                        runner.myExecutor = Executors.newSingleThreadScheduledExecutor()
                    }
                    runner.future = runner.myExecutor.scheduleWithFixedDelay({
                        runBlocking { runner.autoRun(it.first) }
                    }, 0, it.second?.toLong() ?: 1L, TimeUnit.SECONDS)
                }
            }
    }
}

enum class Actions {
    START,
    STOP
}