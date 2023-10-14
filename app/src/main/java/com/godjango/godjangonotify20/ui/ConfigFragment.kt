package com.godjango.godjangonotify20.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.core.content.FileProvider
import androidx.core.text.isDigitsOnly
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.godjango.godjangonotify20.R
import com.godjango.godjangonotify20.core.copyFileToExternalStorage
import com.godjango.godjangonotify20.core.exportConfigs
import com.godjango.godjangonotify20.core.getUUID
import com.godjango.godjangonotify20.core.importConfig
import com.godjango.godjangonotify20.data.models.Configuration
import com.godjango.godjangonotify20.data.models.toConfiguration
import com.godjango.godjangonotify20.data.models.toJSON
import com.godjango.godjangonotify20.databinding.FragmentConfigBinding
import com.godjango.godjangonotify20.ui.adapters.ConfigAdapter
import com.godjango.godjangonotify20.ui.components.ConfirmDialog
import com.godjango.godjangonotify20.ui.viewmodel.ConfigViewModel
import com.google.android.material.snackbar.Snackbar
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.IOException
import kotlin.coroutines.CoroutineContext


@AndroidEntryPoint
class ConfigFragment : Fragment(),CoroutineScope {
    private var _binding: FragmentConfigBinding? = null
    private val binding get() = _binding!!
    private val configViewModel:ConfigViewModel by viewModels()
    private var configurations = emptyList<Configuration>()

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfigBinding.inflate(inflater, container, false)

        binding.fabAdd.setOnClickListener {
            configViewModel.insertConfig(
                Configuration(uuid= getUUID())
            )
        }
        binding.fabCleanDB.setOnClickListener {
            context?.let { it1 ->
                ConfirmDialog().invoke(it1, R.string.all_message_record_will_be_deleted){
                    configViewModel.cleanDB()
                }
            }
        }
        binding.btnPickImg.setOnClickListener {
            activity?.let { it1 -> context?.let { it2 -> importConfig(importLauncher, it2, it1) } }
        }
        binding.btnScanner.setOnClickListener {
            val options = ScanOptions()
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setPrompt(resources.getString(R.string.powered_by_tfprotocol))
                .setOrientationLocked(false)
            barcodeLauncher.launch(options)
        }
        binding.rvConfig.layoutManager = LinearLayoutManager(context)
        return binding.root
    }
    private val filePickerLauncher = registerForActivityResult(CreateDocument("*/*")) { destinationUri ->
        destinationUri?.let {
            copyFileToExternalStorage(destinationUri,configurations,requireContext())
        }
    }
    private fun selectExternalStorageFolder(fileName:String) {
        PermissionX.init(requireActivity()).permissions(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE ).request{ areGranted, _, _->
            if (areGranted) {
                filePickerLauncher.launch(fileName)
            }else{
                Toast.makeText(
                    requireContext(),
                    getString(R.string.grant_permissions),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private val importLauncher= registerForActivityResult(ActivityResultContracts.GetContent()){ uri->
        if (uri != null) {
            val bytes = uri.path?.let {
                context?.contentResolver?.openInputStream(uri)!!.readBytes()
            }
            if(bytes!=null){
                val result = bytes.toString(Charsets.UTF_8)
                if(result.isNotEmpty()){
                    val configs = mutableListOf<Configuration>()
                    val configsJSON = JSONArray(result)
                    for(i in 0 until configsJSON.length())
                        configs.add(configsJSON.getJSONObject(i).toConfiguration())

                    configViewModel.insertConfigs(configs)
                }
            }
        }
    }

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if(result.contents!=null) {
            println(result.contents)
            val config = JSONObject(result.contents).toConfiguration()
            configViewModel.insertConfig(config)
        }
    }

    override fun onStart() {
        super.onStart()
        job = SupervisorJob()
        launch {

            configViewModel.configuration.collect{configs->
                configurations = configs
                binding.btnScanner.setOnClickListener {
                    /*activity?.let { it1 -> context?.let { it2 -> exportConfigs(configs, it2, it1) } }*/
                    configurations = configs
                    selectExternalStorageFolder(
                        "all.goconf"
                    )
                }
                binding.rvConfig.adapter = ConfigAdapter(configs, {id->
                    context?.let {ctx->
                        ConfirmDialog().invoke(ctx,R.string.do_you_want_to_delete_this_configuration){
                            val config = configs.find { it.id == id }!!.copy(id=0)
                            configViewModel.deleteConfig(id)
                            val sb = Snackbar.make(binding.coordinator,getString(R.string.removed),Snackbar.LENGTH_SHORT)
                            sb.setAction(R.string.undo){
                                configViewModel.insertConfig(config)
                            }
                            sb.setBackgroundTint(resources.getColor(R.color.error))
                            sb.setActionTextColor(resources.getColor(R.color.onError))
                            sb.setTextColor(resources.getColor(R.color.onError))
                            sb.show()
                        }
                    }
                }){config->
                    Intent(parentFragment?.context,ConfigActivity::class.java).apply {
                        putExtra("config", config)
                        startActivity(this@apply)
                    }
                }
            }
        }
        launch {
            configViewModel.interval.collect{
                println("INT: $it")
                binding.etInterval.setText("${it?:1}")
                binding.etInterval.addTextChangedListener {it2->
                    if(it2.isNullOrEmpty() || !it2.isDigitsOnly() || it2.toString().toInt() <=0)
                        binding.etInterval.error = getString(R.string.interval_error)
                    else{
                        configViewModel.setInterval(it2.toString().toInt(), it==null )
                    }
                }
            }
        }
    }

    override fun onStop() {
        job.cancel()
        super.onStop()
    }

}

