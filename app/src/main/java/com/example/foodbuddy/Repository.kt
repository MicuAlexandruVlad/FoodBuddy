package com.example.foodbuddy

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.provider.ContactsContract.CommonDataKinds.Note



class Repository(context: Context) {
    companion object {
        const val TAG = "Repository"
    }
    private val database: Database = Database.invoke(context)

    @SuppressLint("StaticFieldLeak")
    fun insertMessage(message: Message): Long {
        object : AsyncTask<Void, Void, Long>() {
            override fun doInBackground(vararg voids: Void): Long? {
                if (message.type == 1)
                    Log.d(TAG, "doInBackground: inserting text message")
                Log.d(TAG, "doInBackground: message id -> " + message.id)
                return database.messageDAO().insertMessage(message)
            }

            override fun onPostExecute(id: Long?) {
                super.onPostExecute(id)
                message.id = id
                Log.d(TAG, "onPostExecute: id -> " + message.id)
            }
        }.execute()
        return -1
    }

    @SuppressLint("StaticFieldLeak")
    fun insertUserStatus(userStatus: UserStatus): Long {
        object : AsyncTask<Void, Void, Long>() {
            override fun doInBackground(vararg p0: Void?): Long {
                Log.d(TAG, "doInBackground: Inserting userStatus")
                return database.userStatusDAO().insertUserStatus(userStatus)
            }

            override fun onPostExecute(id: Long?) {
                super.onPostExecute(id)
                userStatus.id = id
                Log.d(TAG, "onPostExecute: Finished inserting userStatus -> $id")
            }

        }.execute()
        return -1
    }

    @SuppressLint("StaticFieldLeak")
    fun insertUser(user: User): Long {
        object : AsyncTask<Void, Void, Long>() {
            override fun doInBackground(vararg p0: Void?): Long {
                Log.d(TAG, "doInBackground: Inserting user")
                return database.userDAO().insertUser(user)
            }

            override fun onPostExecute(id: Long?) {
                super.onPostExecute(id)
                user.localDbId = id
                Log.d(TAG, "onPostExecute: Finished inserting user -> $id")
            }

        }.execute()
        return -1
    }

    fun getMessagesForConversation(conversationId: String): List<Message> {
        return database.messageDAO().getMessagesForConversation(conversationId)
    }

    fun getLastMessageForConversation(conversationId: String): Message {
        return database.messageDAO().getLastMessageFromConversation(conversationId)
    }

    fun getConversationIds(): List<String> {
        return database.messageDAO().getConversationIds()
    }

    fun getAllMessages(): List<Message> {
        return database.messageDAO().getAllMessages()
    }

    fun getUnreadMessagesAll(read: Boolean): List<Message> {
        return database.messageDAO().getUnreadMessagesAll(read)
    }

    fun getUnreadMessagesForConversation(read: Boolean, conversationId: String): List<Message> {
        return database.messageDAO().getUnreadMessagesForConversation(read, conversationId)
    }

    fun getAllUserStatus(): List<UserStatus> {
        return database.userStatusDAO().getAllUserStatus()
    }

    fun getUserForServerId(id: String): User {
        return database.userDAO().getUserForServerId(id)
    }

    fun getUserStatusForId(userId: String): UserStatus {
        return database.userStatusDAO().getUserStatusForId(userId)
    }

    fun updateUserStatus(userId: String, status: Int, changedAt: String, inConversationWith: String) {
        database.userStatusDAO().updateUserStatus(userId, status, changedAt, inConversationWith)
    }

    fun updateUnreadMessagesInConversation(read: Boolean, readAt: String, conversationId: String) {
        database.messageDAO().updateUnreadMessagesInConversation(read, readAt, conversationId)
    }

    @SuppressLint("StaticFieldLeak")
    fun updateUser(user: User) {
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                database.userDAO().updateUser(user)
                return null
            }
        }.execute()
    }

    fun deleteMessageById(messageId: Long) {
        return database.messageDAO().deleteMessageById(messageId)
    }

    fun deleteUserById(userId: String) {
        database.userDAO().deleteUser(userId)
    }
}