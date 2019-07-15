package com.example.foodbuddy

class SocketEvents {
    companion object {
        const val STATUS_CHANGE = "status-change"

        fun userStatusChange(userId: String): String {
            return "user-$userId-status-changed"
        }
    }
}