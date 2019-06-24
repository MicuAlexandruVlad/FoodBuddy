package com.example.foodbuddy

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.util.Log

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

    fun getMessagesForConversation(conversationId: String): List<Message> {
        return database.messageDAO().getMessagesForConversation(conversationId)
    }

    fun getAllMessages(): List<Message> {
        return database.messageDAO().getAllMessages()
    }
}