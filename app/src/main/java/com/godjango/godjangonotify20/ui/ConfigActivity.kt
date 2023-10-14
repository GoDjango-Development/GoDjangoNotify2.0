package com.godjango.godjangonotify20.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.godjango.godjangonotify20.R
import com.godjango.godjangonotify20.core.copyFileToExternalStorage
import com.godjango.godjangonotify20.core.exportConfigs
import com.godjango.godjangonotify20.core.importConfig
import com.godjango.godjangonotify20.data.models.Configuration
import com.godjango.godjangonotify20.data.models.toConfiguration
import com.godjango.godjangonotify20.data.models.toJSON
import com.godjango.godjangonotify20.databinding.ActivityConfigBinding
import com.godjango.godjangonotify20.ui.adapters.FoldersAdapter
import com.godjango.godjangonotify20.ui.viewmodel.ConfigViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.io.FileWriter
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class ConfigActivity : AppCompatActivity(),CoroutineScope {
    private lateinit var binding: ActivityConfigBinding
    private val configViewModel: ConfigViewModel by viewModels()
    private lateinit var Adapter:FoldersAdapter
    private lateinit var config:Configuration
    private lateinit var job: Job
    private var configurations = emptyList<Configuration>()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.fabPasteKey.setOnClickListener {
            chooseKeyFromClipboard()
        }
        config = intent.getSerializableExtra("config") as Configuration
        binding.btnExport.setOnClickListener {
            //exportConfigs(listOf(config),this,this)
            selectExternalStorageFolder(
                (config.name ?: (getString(R.string.new_configuration) + " " + config.id)).replace(" ","")+".goconf"
            )
        }
        binding.btnImport.setOnClickListener {
            importConfig(importLauncher, this, this)
        }
        updateFieldsValues(config)
        binding.rvFolder.layoutManager = LinearLayoutManager(this@ConfigActivity)
        Adapter = FoldersAdapter(config.safeFolders,{pos->
            config.safeFolders.removeAt(pos)
        },{pos,folderName->
            config.safeFolders[pos] = Pair(folderName,config.safeFolders[pos].second)
        }, onUpdateSecond = {pos,realFolderName->
            config.safeFolders[pos] = Pair(config.safeFolders[pos].first,realFolderName)
        })
        binding.rvFolder.adapter = Adapter

        binding.btnAddFolder.setOnClickListener {
            config.safeFolders.add(0,Pair("",""))
            Adapter.notifyItemInserted(0)
        }
        /*lifecycleScope.launchWhenCreated {
            delay(10)
            binding.ivQrCode.setImageBitmap(getQr(config))
            binding.ivQrCode.invalidate()
        }*/
        binding.btnSave.setOnClickListener {
            if (!configurations.any { (it.name?:"${getString(R.string.new_configuration)} ${it.id}") == binding.etName.text.toString() && it.id != config.id }) {
                config = config.copy(
                    ipServer = binding.etIpServe.text.toString(),
                    portServe = if (!binding.etPortServe.text.isNullOrEmpty()) binding.etPortServe.text.toString()
                        .toInt() else 0,
                    publicKey = binding.tvKey.text.toString().trim(),
                    hash = binding.etHash.text.toString(),
                    protocol = binding.etProtocol.text.toString(),
                    interval = 1,
                    name = binding.etName.text.toString(),
                    safeFolders = config.safeFolders,
                    alreadyDownloads = config.alreadyDownloads
                )
                updateFieldsValues(config)
                configViewModel.uploadConfig(config)
                    Toast.makeText(
                        this@ConfigActivity,
                        resources.getString(R.string.saved),
                        Toast.LENGTH_SHORT).show()
            } else {
                Snackbar.make(
                        binding.coordinator,
                        getString(R.string.there_is_already_a_configuration_with_that_name),
                        Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(resources.getColor(R.color.error))
                        .setActionTextColor(resources.getColor(R.color.onError))
                        .setTextColor(resources.getColor(R.color.onError))
                        .show()
            }
        }
    }

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument(
            "*/*"
        )
    ) { destinationUri ->
        destinationUri?.let {
            copyFileToExternalStorage(destinationUri, listOf(config),this)
        }
    }
    private fun selectExternalStorageFolder(fileName:String) {
        PermissionX.init(this).permissions(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE ).request{ areGranted, _, _->
            if (areGranted) {
                filePickerLauncher.launch(fileName)
            }else{
                Toast.makeText(
                    this,
                    getString(R.string.grant_permissions),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private val importLauncher= registerForActivityResult(ActivityResultContracts.GetContent()){ uri->
        if (uri != null) {
            val bytes = uri.path?.let {
                contentResolver?.openInputStream(uri)!!.readBytes()
            }
            if(bytes!=null){
                val result = bytes.toString(Charsets.UTF_8)
                if(result.isNotEmpty()){
                    val configsJSON = JSONArray(result)
                    if(configsJSON.length()>=1) {
                        val aux = configsJSON.getJSONObject(0).toConfiguration()
                        config = config.copy(
                            ipServer = aux.ipServer,
                            portServe = aux.portServe,
                            publicKey = aux.publicKey.trim(),
                            hash = aux.hash,
                            protocol = aux.protocol,
                            interval = 1,
                            safeFolders = aux.safeFolders
                        )
                        updateFieldsValues(config)
                        configViewModel.uploadConfig(config)
                        Toast.makeText(
                            this@ConfigActivity,
                            resources.getString(R.string.saved),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
    private fun updateFieldsValues(config: Configuration?){
        config?.apply {
            binding.etIpServe.setText(ipServer)
            binding.etHash.setText(hash)
            binding.etPortServe.setText(portServe.toString())
            binding.etProtocol.setText(protocol)
            binding.tvKey.text = publicKey
            binding.etName.setText(name?:"${getString(R.string.new_configuration)} $id")
            Adapter = FoldersAdapter(config.safeFolders,{pos->
                config.safeFolders.removeAt(pos)
            },{pos,folderName->
                config.safeFolders[pos] = Pair(folderName,config.safeFolders[pos].second)
            }, onUpdateSecond = {pos,realFolderName->
                config.safeFolders[pos] = Pair(config.safeFolders[pos].first,realFolderName)
            })
            binding.rvFolder.adapter = Adapter
            /*binding.ivQrCode.setImageBitmap(getQr(this))
            binding.ivQrCode.invalidate()*/
        }
    }
    private fun chooseKeyFromClipboard() {
        val clipboard: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip()) {
            val clipData: ClipData? = clipboard.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val key = clipData.getItemAt(0).text
                binding.tvKey.text = key
            }
        }
    }
    /*private fun getQr(config: Configuration): Bitmap? {
        val barcodeEncoder = BarcodeEncoder()
        return barcodeEncoder.encodeBitmap(
            config.toJSON().toString(),
            BarcodeFormat.QR_CODE,
            binding.ivQrCode.width,
            binding.ivQrCode.width
        )
    }*/
    override fun onStart() {
        super.onStart()
        job = SupervisorJob()
        launch {
            configViewModel.configuration.collect { configs ->
                configurations = configs
            }
        }
    }
    override fun onStop() {
        job.cancel()
        super.onStop()
    }
}