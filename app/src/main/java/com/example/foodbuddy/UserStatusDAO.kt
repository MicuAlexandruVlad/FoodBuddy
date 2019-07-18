package com.example.foodbuddy

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface UserStatusDAO {
    @Insert
    fun insertUserStatus(userStatus: UserStatus): Long

    @Query("SELECT * FROM UserStatus WHERE userId = :userId")
    fun getUserStatusForId(userId: String): UserStatus

    @Query("SELECT * FROM UserStatus")
    fun getAllUserStatus(): List<UserStatus>

    @Query("""UPDATE UserStatus 
        SET status = :status, statusChangedAt = :changedAt 
        WHERE userId = :userId""")
    fun updateUserStatus(userId: String, status: Int, changedAt: String)
}