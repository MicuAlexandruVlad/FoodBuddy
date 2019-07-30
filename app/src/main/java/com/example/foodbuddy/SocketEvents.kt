package com.example.foodbuddy

class SocketEvents {
    companion object {
        const val STATUS_CHANGE = "status-change"
        const val EVENT_REQ_PERMISSION = "event-creation-request-permission"
        const val EVENT_CANCEL_REQ_PERMISSION = "event-request-canceled"
        const val EVENT_PERMISSION_HANDLE = "event-permission-handle"


        fun listenForUserStatusChange(userId: String): String {
            return "user-$userId-status-changed"
        }

        fun permissionRequired(userId: String): String {
            return "user-$userId-permission-required"
        }

        fun requestCancel(userId: String): String {
            return "user-$userId-permission-cancel"
        }

        fun permissionHandled(userId: String): String {
            return "user-$userId-handled-permission"
        }
    }
}