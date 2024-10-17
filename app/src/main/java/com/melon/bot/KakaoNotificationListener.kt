package com.melon.bot

import android.app.Notification
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi

class KakaoNotificationListener : NotificationListenerService() {

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if(sbn?.packageName == "com.kakao.talk"){
            val title = sbn.notification.extras.get(Notification.EXTRA_TITLE).toString()
            val text = sbn.notification.extras.get(Notification.EXTRA_TEXT).toString()
            val person = sbn.notification.extras.get(Notification.EXTRA_CALL_PERSON).toString()
            val message = sbn.notification.extras.get(Notification.EXTRA_MESSAGES).toString()
            val message_person = sbn.notification.extras.get(Notification.EXTRA_MESSAGING_PERSON).toString()
            Log.e("WILLY", "$title $text $person $message $message_person")
            Toast.makeText(this, "테스트1 ${sbn.notification.extras}", Toast.LENGTH_SHORT).show()

        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        Toast.makeText(this, "테스트2", Toast.LENGTH_SHORT).show()
    }
}