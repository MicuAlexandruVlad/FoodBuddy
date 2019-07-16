package com.example.foodbuddy

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "UserStatus")
class UserStatus: Serializable {
    companion object {
        const val OFFLINE = 0
        const val ONLINE = 1
        const val NOT_INITIALIZED = -1
        const val IN_CONVERSATION = 2
    }

    @PrimaryKey(autoGenerate = true)
    var id: Long? = null

    var userId: String = ""
    // status => 0 - offline, 1 - online, -1 - not initialized
    // 2 - in a conversation
    var status: Int = -1
    var statusChangedAt: String = ""

    @Ignore
    var inConversationWith: String = ""
}