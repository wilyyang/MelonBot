package com.melon.bot.processor

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.melon.bot.core.common.openChatRoomName
import com.melon.bot.core.common.tag
import com.melon.bot.domain.contents.Command
import com.melon.bot.domain.contents.CommonContents
import com.melon.bot.domain.contents.GroupTextResponse
import com.melon.bot.domain.contents.QuestionGame
import com.melon.bot.domain.contents.UserTextResponse
import com.melon.bot.domain.intent.ChatRoomKey

class CmdProcessor(private val context: Context) {
    private val mainOpenChatRoomKey : ChatRoomKey = ChatRoomKey(isGroupConversation = true, roomName = openChatRoomName)
    private var mainOpenChatRoomAction : Notification.Action? = null
    private val userChatRoomMap = mutableMapOf<ChatRoomKey, Notification.Action>()
    private val commonContents = CommonContents()
    private val questionGame = QuestionGame()

    fun deliverNotification(chatRoomKey: ChatRoomKey, action : Notification.Action, userName: String, text : String){
        Log.i(tag, "[deliver] key : $chatRoomKey")
        Log.i(tag, "[deliver] userName : $userName / text : $text")
        if(!chatRoomKey.isGroupConversation){
            userChatRoomMap[chatRoomKey] = action
        }else if(chatRoomKey == mainOpenChatRoomKey){
            mainOpenChatRoomAction = action
        }
        val commonCommand = commonContents.request(chatRoomKey = chatRoomKey, userName = userName, text = text)
        handleCommand(commonCommand)
        val questionGameCommand = questionGame.request(chatRoomKey = chatRoomKey, userName = userName, text = text)
        handleCommand(questionGameCommand)
    }

    private fun handleCommand(command: Command){
        Log.i(tag, "[command] ${command.javaClass.simpleName} / mainOpenChatRoomAction : $mainOpenChatRoomAction")
        when(command){
            is GroupTextResponse -> {
                mainOpenChatRoomAction?.let { action ->
                    sendActionText(context, action, command.text)
                }
            }
            is UserTextResponse -> {
                userChatRoomMap[ChatRoomKey(isGroupConversation = false, roomName = command.userName)]?.let { action ->
                    sendActionText(context, action, command.text)
                }
            }
            else -> {}
        }
    }


}

fun sendActionText(context : Context, action: Notification.Action, text : String){
    for (input in action.remoteInputs ?: emptyArray()) {
        val intent = Intent()
        val remoteInputs = mutableMapOf<String, Any>()

        remoteInputs[input.resultKey] = text
        val bundle = Bundle()
        for ((inputKey, value) in remoteInputs) {
            bundle.putCharSequence(inputKey, value.toString())
        }
        RemoteInput.addResultsToIntent(action.remoteInputs, intent, bundle)

        try {
            action.actionIntent.send(context, 0, intent)
        } catch (e: PendingIntent.CanceledException) {
            e.printStackTrace()
        }
    }
}