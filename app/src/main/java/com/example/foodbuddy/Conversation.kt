package com.example.foodbuddy

import java.io.Serializable

class Conversation: Serializable {
    var conversationId: String = ""
    var profilePhotoId: String = ""
    var conversationUser: User = User()
    var lastMessage: Message = Message()
    var unreadMessages: Int = 0
}