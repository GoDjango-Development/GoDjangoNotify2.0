package com.godjango.godjangonotify20.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.godjango.godjangonotify20.core.Callback
import com.godjango.godjangonotify20.core.getUUID
import com.godjango.godjangonotify20.data.db.database.ConverterListFiles
import com.nerox.client.Tfprotocol
import org.json.JSONObject
import java.io.Serializable

@Entity(tableName = "configuration")
data class Configuration(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var uuid:String,
    var name:String?=null,
    var ipServer:String = "",
    var portServe:Int = 0,
    var publicKey:String = "",
    var hash:String = "",
    var protocol:String = "",
    var interval:Int = 1,
    val safeFolders:MutableList<Pair<String,String>> = mutableListOf(),
    val alreadyDownloads:MutableList<String> = mutableListOf()
) : Serializable

fun Configuration.toJSON(): JSONObject = JSONObject()
        .put("ipServer",ipServer)
        .put("publicKey",publicKey)
        .put("portServe",portServe)
        .put("hash",hash)
        .put("protocol",protocol)
        .put("safeFolders",ConverterListFiles().fromPairList(safeFolders))

fun JSONObject.toConfiguration() = Configuration(
    0,
    getUUID(),
    null,
    getString("ipServer"),
    getInt("portServe"),
    getString("publicKey").trim(),
    getString("hash"),
    getString("protocol"),
    1,
    ConverterListFiles().toPairList(getString("safeFolders"))
)

fun Configuration.toProtocol() = Tfprotocol(ipServer,portServe,(publicKey).trim(),hash,64,protocol,
    Callback()
)