package com.example.foodbuddy

import java.io.Serializable

class EventRequest: Serializable {
    companion object {
        const val EVENT_REQUEST_PERMISSION = 0
        const val EVENT_CANCEL_PERMISSION_REQUEST = 1
        const val EVENT_PERMISSION_GRANTED = 2
        const val EVENT_PERMISSION_DENIED = 3
    }

    var reqUserId: String = ""
    var permUserId: String = ""
    var reqType: Int = -1

    var reqUser: User = User()
}