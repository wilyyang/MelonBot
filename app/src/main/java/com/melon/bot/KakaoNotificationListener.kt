package com.melon.bot

import android.app.Notification
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresApi
import com.melon.bot.domain.contents.QuestionGame
import com.melon.bot.domain.intent.IntentMap
import com.melon.bot.domain.intent.UserIntentKey

const val targetPackageName = "com.kakao.talk"
const val replyActionIndex = 1
const val hostKeyword = "?"

fun Notification.getKey() : UserIntentKey? {
    val isGroupConversation = extras.getBoolean(Notification.EXTRA_IS_GROUP_CONVERSATION, false)
    val roomName = extras.getString(Notification.EXTRA_SUB_TEXT, "")
    val userName = extras.getString(Notification.EXTRA_TITLE, "")

    return if (roomName.isNotBlank() || userName.isNotBlank()) {
        UserIntentKey(isGroupConversation, roomName, userName)
    } else null
}

class KakaoNotificationListener : NotificationListenerService() {
    private val intentMap : IntentMap = IntentMap()
    private val questionGame : QuestionGame = QuestionGame()

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val key = sbn?.notification?.getKey()
        val action = sbn?.notification?.actions?.get(replyActionIndex)
        val text = sbn?.notification?.extras?.getString(Notification.EXTRA_TEXT, "")

        if (sbn?.packageName == targetPackageName && key != null && action != null && !text.isNullOrBlank()) {
            intentMap.put(key, action)
            if(text.startsWith(hostKeyword)){
                questionGame.handleCommand(text.split(" "))

            }
        }
    }
}