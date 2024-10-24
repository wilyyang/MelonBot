package com.melon.bot.domain.intent

import android.app.Notification

data class UserIntentKey(
    val isGroupConversation : Boolean,
    val roomName : String,
    val userName : String
)

class IntentMap {
    private val userIntentMap : MutableMap<UserIntentKey, Notification.Action> = mutableMapOf()

    fun put(key : UserIntentKey, action : Notification.Action){
        userIntentMap[key] = action
    }
}