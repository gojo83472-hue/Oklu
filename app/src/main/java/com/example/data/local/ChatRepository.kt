package com.example.data.local

import kotlinx.coroutines.flow.Flow

class ChatRepository(private val messageDao: MessageDao) {
    val messages: Flow<List<MessageEntity>> = messageDao.getAllMessages()

    suspend fun insertMessage(message: MessageEntity): Long {
        return messageDao.insertMessage(message)
    }

    suspend fun updateMessage(message: MessageEntity) {
        messageDao.updateMessage(message)
    }

    suspend fun clearHistory() {
        messageDao.clearAllMessages()
    }

    suspend fun deleteMessage(id: Long) {
        messageDao.deleteMessageById(id)
    }

    suspend fun getRecentMessages(limit: Int = 10): List<MessageEntity> {
        return messageDao.getRecentMessages(limit)
    }
}
