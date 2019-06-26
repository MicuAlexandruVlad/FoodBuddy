package com.example.foodbuddy

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface MessageDAO {
    @Insert
    fun insertMessage(message: Message): Long

    @Query("SELECT * FROM Message")
    fun getAllMessages(): List<Message>

    @Query("SELECT * FROM Message WHERE senderId = :userId ORDER BY id DESC LIMIT 1")
    fun getLastMessageFromConversation(userId: String): Message

    @Query("SELECT * FROM Message WHERE conversationId = :conversationId ORDER BY id ASC")
    fun getMessagesForConversation(conversationId: String): List<Message>

    @Query("SELECT DISTINCT conversationId FROM Message GROUP BY conversationId")
    fun getConversationIds(): List<String>

    @Query("UPDATE Message SET imagePath= :path WHERE id= :id")
    fun updateImagePath(path: String, id: Int)

    @Query("DELETE from Message WHERE id = :messageId")
    fun deleteMessageById(messageId: Long)
}