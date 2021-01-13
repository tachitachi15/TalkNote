package com.yuki.talknote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.yuki.talkmemo.Talk
import com.yuki.talkmemo.TalkRoomDatabase

class TalkViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TalkRepository
    val allTalks: LiveData<List<Talk>>

    init {
        val talkDao = TalkRoomDatabase.getTalkDatabase(application).talkDao()
        repository = TalkRepository(talkDao)
        allTalks = repository.allTalks
    }
}