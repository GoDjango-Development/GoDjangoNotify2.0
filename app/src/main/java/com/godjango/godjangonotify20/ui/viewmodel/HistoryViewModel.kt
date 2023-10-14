package com.godjango.godjangonotify20.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.godjango.godjangonotify20.data.Repository
import com.godjango.godjangonotify20.data.models.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: Repository
) :ViewModel() {
    val savedMessages = MutableStateFlow<List<Message>>(emptyList())
    init {
        viewModelScope.launch {
            repository.savedMessages.collect{
                println(it)
                savedMessages.value = it
            }
        }
    }
    fun deleteFromHistory(id:Int){
        viewModelScope.launch {
            repository.deleteMessage(id)
        }
    }

    fun cleanHistory(){
        viewModelScope.launch {
            repository.cleanHistory()
        }
    }
}