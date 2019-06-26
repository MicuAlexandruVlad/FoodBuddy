package com.example.foodbuddy

import java.io.Serializable

class Conversation: Serializable {
    var conversationId: String = ""
    var profilePhotoId: String = ""
    var lastMessage: Message = Message()
}