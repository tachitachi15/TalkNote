package com.yuki.talkmemo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Talk::class), version = 1, exportSchema = false)
public abstract class TalkRoomDatabase : RoomDatabase() {
    abstract fun talkDao(): TalkDao

    companion object {
        @Volatile
        private var INSTANCE: TalkRoomDatabase? = null

        fun getTalkDatabase(context: Context): TalkRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TalkRoomDatabase::class.java,
                    "Talk_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}