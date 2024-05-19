package com.example.roomsample.data.user
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entityはおまじないです（テーブルですよって目印）
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // 主キー、オートインクリメント
    @ColumnInfo(name = "name") val memo: String //ColumnInfoでカラム名を設定
)