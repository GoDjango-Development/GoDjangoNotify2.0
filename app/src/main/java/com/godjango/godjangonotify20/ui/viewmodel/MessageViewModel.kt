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
class MessageViewModel @Inject constructor(
    private val repository: Repository
) :ViewModel() {
    val messages = MutableStateFlow<List<Message>>(emptyList())
    init {
        viewModelScope.launch {
            repository.unsavedMessages.collect{
                messages.value = it
            }
        }
    }

    fun saveInHistory(id: Int){
        viewModelScope.launch {
            repository.archiveMessage(id)
        }
    }
    fun viewed(id: Int){
        viewModelScope.launch {
            repository.viewed(id)
        }
    }
    fun archiveAll(){
        viewModelScope.launch {
            repository.archiveAllMessages()
        }
    }
}