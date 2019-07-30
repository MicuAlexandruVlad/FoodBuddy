package com.example.foodbuddy

import android.arch.persistence.room.*

@Dao
interface UserDAO {
    @Insert
    fun insertUser(user: User): Long

    @Query("SELECT * FROM User Where _id = :id ")
    fun getUserForServerId(id: String): User

    @Query("DELETE from User WHERE _id = :id")
    fun deleteUser(id: String)

    @Update
    fun updateUser(user: User)
}