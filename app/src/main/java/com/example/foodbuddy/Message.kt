package com.example.foodbuddy

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable

@Entity (tableName = "Message")
class Message: Serializable {
    companion object {
        const val MESSAGE_TEXT = 1
        const val MESSAGE_PHOTO = 2
        const val MESSAGE_VOICE = 3
        const val MESSAGE_DUMMY = 4
    }

    @PrimaryKey(autoGenerate = true)
    var id: Long? = null

    var messageText: String = ""
    var senderName: String = ""
    var senderId: String = ""
    var timestamp: String = ""
    var imageData: String = ""
    var voiceData: String = ""
    var imagePath: String = ""
    var voicePath: String = ""
    var type: Int = 1
    var seen: Boolean = false
    var seenAt: String = ""
    var read: Boolean = false
    var readAt: String = ""
    var conversationId: String = ""
    var ownerId: String = ""
}