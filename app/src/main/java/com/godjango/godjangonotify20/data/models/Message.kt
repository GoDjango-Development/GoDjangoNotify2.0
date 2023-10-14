package com.godjango.godjangonotify20.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(tableName = "messages")
data class Message (
    @PrimaryKey(autoGenerate = true) var id: Int=0,
    var configAndFolder: String?=null,
    var name: String? = null,
    var tel: String? = null,
    var addr: String? = null,
    var msg: String? = null,
    var date: String? = null,
    var total: String? = null,
    var archive: Boolean = false,
    var viewed: Boolean = false
)

fun JSONObject.toMessage(config:String,folder:String):Message {
    val msg = Message(configAndFolder = "$config::$folder")
    try {
        msg.name = getString("name")
    }catch (e:Exception){
        e.printStackTrace()
    }
    try {
        msg.addr = getString("addr")
    }catch (e:Exception){
        e.printStackTrace()
    }
    try {
        msg.msg = getString("msg")
    }catch (e:Exception){
        e.printStackTrace()
    }
    try {
        msg.total = getString("Total")
    }catch (e:Exception){
        e.printStackTrace()
    }
    try {
        msg.date = getString("date")
    }catch (e:Exception){
        e.printStackTrace()
    }
    try {
        msg.tel = getString("tel")
    }catch (e:Exception){
        e.printStackTrace()
    }
    return msg
}