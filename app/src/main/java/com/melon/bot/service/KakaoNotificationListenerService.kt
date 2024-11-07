package com.melon.bot.service

import android.app.Notification
import android.app.Person
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.SpannableString
import com.melon.bot.core.common.replyActionIndex
import com.melon.bot.core.common.targetPackageName
import com.melon.bot.domain.intent.ChatRoomKey
import com.melon.bot.processor.CmdProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KakaoNotificationListenerService : NotificationListenerService() {

    private val cmdProcessor = CmdProcessor(this)
    private val serviceScope = CoroutineScope(Dispatchers.Default)
    override fun onNotificationPosted(sbn : StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        val key = sbn?.notification?.getKey()
        val action = sbn?.notification?.actions?.get(replyActionIndex)

        val user = sbn?.notification?.getPerson()
        val text = when(val textObject = sbn?.notification?.extras?.get(Notification.EXTRA_TEXT)){
            is SpannableString -> textObject.toString()
            is String -> textObject
            else -> null
        }

        if (sbn?.packageName == targetPackageName && key != null && action != null && user != null && !text.isNullOrBlank()) {
            serviceScope.launch {
                cmdProcessor.deliverNotification(chatRoomKey = key, action = action, user = user, text = text)
            }
        }
    }

    private fun Notification.getKey() : ChatRoomKey? {
        val isGroupConversation = extras.getBoolean(Notification.EXTRA_IS_GROUP_CONVERSATION, false)
        val roomName = extras.getString(Notification.EXTRA_SUB_TEXT, "")
        val userName = extras.getString(Notification.EXTRA_TITLE, "")

        return if (isGroupConversation && roomName.isNotBlank()) {
            ChatRoomKey(isGroupConversation = true, roomName = roomName)
        } else if(!isGroupConversation && userName.isNotBlank()){
            ChatRoomKey(isGroupConversation = false, roomName = userName)
        } else{
            null
        }
    }

    private fun Notification.getPerson() : Person? {
        val messages = extras.getParcelableArray("android.messages")
        if(!messages.isNullOrEmpty()){
            messages[0]?.let { parcelable ->
                return (parcelable as? Bundle)?.getParcelable<Person>("sender_person")
            }
        }
        return null
    }
}