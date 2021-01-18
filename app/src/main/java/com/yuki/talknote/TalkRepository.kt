package com.yuki.talknote

import androidx.lifecycle.LiveData
import com.yuki.talkmemo.Talk
import com.yuki.talkmemo.TalkDao

class TalkRepository(private val talkDao: TalkDao) {
    val allTalks: LiveData<List<Talk>> = talkDao.getTalks()

    suspend fun insert(talk: Talk) {
        talkDao.insert(talk)
    }

    suspend fun delete(talk: Talk) {
        talkDao.delete(talk)
    }
}
