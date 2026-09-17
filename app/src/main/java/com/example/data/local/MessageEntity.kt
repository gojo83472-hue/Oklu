package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String? = null, // e.g. "OPEN_CAMERA", "CALL_PHONE", etc.
    val actionPayload: String? = null, // e.g. phone number, search query, app name
    val actionLabel: String? = null, // human readable label
    val actionStatus: String = "pending" // "pending", "executed", "cancelled"
)
