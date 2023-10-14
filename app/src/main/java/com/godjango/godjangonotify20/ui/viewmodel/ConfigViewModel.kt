package com.godjango.godjangonotify20.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.godjango.godjangonotify20.data.Repository
import com.godjango.godjangonotify20.data.models.Configuration
import com.godjango.godjangonotify20.data.models.Message
import com.godjango.godjangonotify20.data.models.toMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfigViewModel @Inject constructor(
    private val repository: Repository
) :ViewModel() {
    val configuration = MutableStateFlow<List<Configuration>>(emptyList())
    val interval = MutableStateFlow<Int?>(null)
    init {
        viewModelScope.launch {
            repository.configuration.collect{
                configuration.value = it
            }
        }
        viewModelScope.launch {
            repository.interval.collect{
                interval.value = it
            }
        }
    }
    fun uploadConfig(configuration: Configuration) {
        viewModelScope.launch {
            repository.updateConfig(configuration)
        }
    }
    fun deleteConfig(id: Int) {
        viewModelScope.launch {
            repository.deleteConfig(id)
        }
    }
    fun insertConfig(configuration: Configuration) {
        viewModelScope.launch {
            repository.insertConfig(configuration)
        }
    }
    fun setInterval(value:Int, new:Boolean){
        viewModelScope.launch {
            if(!new){
                repository.setInterval(value)
            }else{
                repository.addInterval(value)
            }
        }
    }

    fun cleanDB(){
        viewModelScope.launch {
            repository.cleanDownloads()
        }
    }
    fun insertConfigs(configs:List<Configuration>){
        viewModelScope.launch {
            repository.insertConfigs(configs)
        }
    }

}