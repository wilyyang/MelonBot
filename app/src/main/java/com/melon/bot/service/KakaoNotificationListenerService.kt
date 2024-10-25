package com.melon.bot.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.melon.bot.core.common.replyActionIndex
import com.melon.bot.core.common.targetPackageName
import com.melon.bot.domain.intent.ChatRoomKey
import com.melon.bot.processor.CmdProcessor

class KakaoNotificationListenerService : NotificationListenerService() {

    private val cmdProcessor = CmdProcessor(this)

    override fun onNotificationPosted(sbn : StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        val key = sbn?.notification?.getKey()
        val action = sbn?.notification?.actions?.get(replyActionIndex)

        val userName = sbn?.notification?.extras?.getString(Notification.EXTRA_TITLE, "")
        val text = sbn?.notification?.extras?.getString(Notification.EXTRA_TEXT, "")

        if (sbn?.packageName == targetPackageName && key != null && action != null && !userName.isNullOrBlank() && !text.isNullOrBlank()) {
            cmdProcessor.deliverNotification(chatRoomKey = key, action = action, userName = userName, text = text)
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
}