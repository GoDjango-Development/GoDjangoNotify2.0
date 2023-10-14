package com.godjango.godjangonotify20.core

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import com.godjango.godjangonotify20.R
import com.godjango.godjangonotify20.data.models.Configuration
import com.godjango.godjangonotify20.data.models.toConfiguration
import com.godjango.godjangonotify20.data.models.toJSON
import com.permissionx.guolindev.PermissionX
import org.json.JSONArray
import java.io.File
import java.io.FileWriter
import java.io.IOException

fun exportConfigs(configs:List<Configuration>, ctx: Context, act:FragmentActivity){
    PermissionX.init(act).permissions(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE ).request{ areGranted, _, _->
        if (areGranted) {
            try {
                /*val docFolder = ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                val jsonArray = JSONArray()
                val file = if(configs.size == 1) File("$docFolder/${configs[0].name}-${configs[0].uuid}.goconf") else File("$docFolder/all.goconf")
                configs.forEach { config->
                    jsonArray.put(config.toJSON())
                }
                file.createNewFile()
                val writer = FileWriter(file)
                writer.write(jsonArray.toString())
                writer.close()
                Toast.makeText(
                    ctx,
                    ctx.getString(R.string.saved_in) + ": " + file.path.toString(),
                    Toast.LENGTH_LONG
                ).show()
                val uri = FileProvider.getUriForFile(ctx,"com.godjango.godjangonotify20.provider", file)
                val intent = Intent(Intent.ACTION_SEND)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.type = "application/octet-stream"
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                ctx.startActivity(intent)*/
            } catch (e: Exception) {
                Log.e("FILE_ERROR", e.message.toString())
            }
        }else {
            Toast.makeText(
                ctx,
                ctx.getString(R.string.grant_permissions),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

fun copyFileToExternalStorage(destination: Uri, configurations:List<Configuration>, ctx: Context) {
    try {
        val jsonArray = JSONArray()
        configurations.forEach { config->
            jsonArray.put(config.toJSON())
        }
        val outputStream = ctx.contentResolver.openOutputStream(destination)
        outputStream?.write(jsonArray.toString().toByteArray())
        outputStream?.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun getLauncher(ctx:Context, act: FragmentActivity, onSave:(Configuration)->Unit) = act.registerForActivityResult(ActivityResultContracts.GetContent()){ uri->
    if (uri != null) {
        val bytes = uri.path?.let {
            ctx.contentResolver.openInputStream(uri)!!.readBytes()
        }
        if(bytes!=null){
            val result = bytes.toString(Charsets.UTF_8)
            if(result.isNotEmpty()){
                val configs = JSONArray(result)
                for(i in 0 until configs.length())
                    onSave(configs.getJSONObject(i).toConfiguration())
            }
        }
    }
}

fun importConfig(pickLauncher:ActivityResultLauncher<String>, ctx:Context, act: FragmentActivity){
    val permissions = mutableListOf<String>()
    permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)

    PermissionX.init(act).permissions(permissions).request{isGranted,_,_->
        if(isGranted){
            pickLauncher.launch("*/*")
        }else{
            Toast.makeText(ctx, ctx.resources.getString(R.string.grant_permissions), Toast.LENGTH_SHORT).show()
        }
    }
}