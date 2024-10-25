package com.melon.bot

import android.app.Notification
import android.app.Notification.Action
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import com.melon.bot.domain.contents.QuestionGame
import com.melon.bot.domain.intent.IntentMap
import com.melon.bot.domain.intent.UserIntentKey
import java.util.logging.Logger

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

    private var firstAction : Action? = null

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val key = sbn?.notification?.getKey()
        val action = sbn?.notification?.actions?.get(replyActionIndex)
        val text = sbn?.notification?.extras?.getString(Notification.EXTRA_TEXT, "")

        if (sbn?.packageName == targetPackageName && key != null && action != null && !text.isNullOrBlank()) {
            intentMap.put(key, action)

            firstAction = action

            Log.e("WILLY", "${sbn.key} ${key.roomName} ${key.userName} ${key.isGroupConversation} $text")


        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)

        val key = sbn?.notification?.getKey()
        val action = sbn?.notification?.actions?.get(replyActionIndex)
        val text = sbn?.notification?.extras?.getString(Notification.EXTRA_TEXT, "")

        if (sbn?.packageName == targetPackageName && key != null && action != null && !text.isNullOrBlank()) {
            firstAction?.let { firstAction ->


                Log.e("WILLY", "Removed ${sbn.key} ${key.roomName} ${key.userName} ${key.isGroupConversation} $text")

                for (input in firstAction.remoteInputs ?: emptyArray()) {
                    val intent = Intent()
                    val remoteInputs = mutableMapOf<String, Any>()

                    remoteInputs[input.resultKey] = "너무 피곤하다.."
                    val bundle = Bundle()
                    for ((inputKey, value) in remoteInputs) {
                        bundle.putCharSequence(inputKey, value.toString())
                    }
                    RemoteInput.addResultsToIntent(firstAction.remoteInputs, intent, bundle)

                    try {
                        firstAction.actionIntent.send(this, 0, intent)
                    } catch (e: PendingIntent.CanceledException) {
                        e.printStackTrace()
                    }
                }
            }
        }


    }
}