package com.yuki.talknote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yuki.talkmemo.Talk
import com.yuki.talkmemo.TalkRoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddTalkViewModel(application: Application): AndroidViewModel(application){
    private val repository: TalkRepository
    init {
        val talkDao = TalkRoomDatabase.getTalkDatabase(application).talkDao()
        repository = TalkRepository(talkDao)
    }

    fun insert(talk: Talk) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(talk)
    }

    fun delete(talk: Talk) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(talk)
    }
}