package com.godjango.godjangonotify20

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.godjango.godjangonotify20.core.Actions
import com.godjango.godjangonotify20.core.ProtocolsService
import com.godjango.godjangonotify20.core.getUUID
import com.godjango.godjangonotify20.databinding.ActivityMainBinding
import com.godjango.godjangonotify20.ui.viewmodel.MainViewModel
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionX.init(this).permissions(listOf(Manifest.permission.POST_NOTIFICATIONS))
                .request { isGranted, _, _ ->
                    if (!isGranted)
                        Toast.makeText(
                            this,
                            resources.getString(R.string.grant_permissions),
                            Toast.LENGTH_SHORT
                        ).show()
                }
        }
        launchNotify()
        lifecycleScope.launch {
            mainViewModel.unreviewed.collect{ unviewed->
                if(unviewed == 0){
                    binding.navView.removeBadge(R.id.navigation_message)
                }else{
                    binding.navView.getOrCreateBadge(R.id.navigation_message).apply {
                        backgroundColor = Color.parseColor("#ff6584")
                        number = unviewed
                    }
                }
            }
        }
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main)
        NavigationUI.setupWithNavController(binding.navView, navController)
    }

    private fun launchNotify() {
        Intent(applicationContext, ProtocolsService::class.java).also {
            it.action = Actions.START.toString()
            startService(it)
        }
    }
}