package com.yuki.talkmemo

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "talk_table")
data class Talk (
    @PrimaryKey(autoGenerate = true) val id:Int,
    @ColumnInfo(name = "date") val date: String, //データベースに日時を保存する時Stringに変換して保存する
    @ColumnInfo(name = "keywords") val keywords: String
):Parcelable