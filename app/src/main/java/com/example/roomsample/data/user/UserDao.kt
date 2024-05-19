package com.example.roomsample.data.user
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

// データベースにアクセスする時に使う関数たちを書く
@Dao
interface UserDao {
    /** 全データ取得 */
    @Query("SELECT * FROM users")
    fun getAll(): List<User>

    /** データ更新 */
    @Update
    fun update(testDBEntity: User)

    /** データ追加 */
    @Insert
    fun insert(testDBEntity: User)

    /** データ削除 */
    @Delete
    fun delete(testDBEntity: User)
}