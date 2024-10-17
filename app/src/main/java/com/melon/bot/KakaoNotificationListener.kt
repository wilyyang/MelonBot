package com.melon.bot

import android.app.Notification
import android.app.PendingIntent
import android.app.Person
import android.app.RemoteInput
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi

class KakaoNotificationListener : NotificationListenerService() {

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if(sbn?.packageName == "com.kakao.talk"){

            val is_group_conversation = sbn.notification.extras.get(Notification.EXTRA_IS_GROUP_CONVERSATION).toString()
            val messaging_person : Person? = sbn.notification.extras.get(Notification.EXTRA_MESSAGING_PERSON) as Person?
            val self_display_name = sbn.notification.extras.get(Notification.EXTRA_SELF_DISPLAY_NAME).toString()
            val sub_text = sbn.notification.extras.get(Notification.EXTRA_SUB_TEXT).toString()
            val text = sbn.notification.extras.get(Notification.EXTRA_TEXT).toString()
            val title = sbn.notification.extras.get(Notification.EXTRA_TITLE).toString()

            val messaging_person_name : String = (messaging_person?.name?.toString())?:"null"
            val messaging_person_key : String = (messaging_person?.key.toString())?:"null"
            val messaging_person_uri : String = (messaging_person?.uri.toString())?:"null"

            val map = hashMapOf<String, String>(
                "is_group_conversation" to is_group_conversation,
                "messaging_person.name" to messaging_person_name,
                "messaging_person.key" to messaging_person_key,
                "messaging_person.uri" to messaging_person_uri,
                "self_display_name" to self_display_name,
                "sub_text" to sub_text,
                "text" to text,
                "title" to title,
            )


            val mapToString = map.pairToString()
            val actions : Array<Notification.Action>? = sbn.notification.actions
            if(!actions.isNullOrEmpty()){

                val replyAction = actions[1] // 인덱스를 바꾸어 원하는 액션을 선택

                // RemoteInput이 있는지 확인
                for (input in replyAction.remoteInputs ?: emptyArray()) {
                    // 자동으로 응답할 텍스트를 설정합니다
                    val replyText : String = when(text){
                        "가위" -> "바위"
                        "바위" -> "보"
                        "보" ->"가위"
                        else -> ""
                    }

                    if(replyText.isNotBlank()){
                        // RemoteInput에 텍스트를 넣어줍니다
                        val intent = Intent()
                        val remoteInputs = mutableMapOf<String, Any>()

                        remoteInputs[input.resultKey] = replyText
                        val bundle = Bundle()
                        for ((key, value) in remoteInputs) {
                            bundle.putCharSequence(key, value.toString())
                        }
                        RemoteInput.addResultsToIntent(replyAction.remoteInputs, intent, bundle)

                        // PendingIntent를 실행하여 응답을 트리거합니다
                        try {
                            replyAction.actionIntent.send(this, 0, intent)
                        } catch (e: PendingIntent.CanceledException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            Log.e("MelonBot", mapToString)
            Toast.makeText(this, "테스트1 ${mapToString}",
                Toast.LENGTH_SHORT).show()

        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        Toast.makeText(this, "테스트2", Toast.LENGTH_SHORT).show()
    }
}

fun Map<String, String>.pairToString() = filterValues { it != "null" }.map { (key, value) -> "$key : $value" }.joinToString(", ")