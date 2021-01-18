package com.yuki.talkmemo

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TalkDao {
    @Query("SELECT * from talk_table")
    fun getTalks(): LiveData<List<Talk>>
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(talk: Talk)
    @Delete
    suspend fun delete(talk: Talk)
}