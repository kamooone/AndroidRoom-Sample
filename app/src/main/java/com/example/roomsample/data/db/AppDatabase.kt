package com.example.roomsample.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.roomsample.data.user.User
import com.example.roomsample.data.user.UserDao

@Database(entities = [User::class], version = 2)
abstract class TestDB : RoomDatabase() {
    abstract fun userDao(): UserDao
}