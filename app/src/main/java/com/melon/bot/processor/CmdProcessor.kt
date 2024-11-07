package com.melon.bot.processor

import android.app.Notification
import android.app.PendingIntent
import android.app.Person
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
import com.melon.bot.domain.intent.TimerContents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class CmdProcessor(private val serviceContext: Context) {
    private val mainOpenChatRoomKey : ChatRoomKey = ChatRoomKey(isGroupConversation = true, roomName = openChatRoomName)
    private var mainOpenChatRoomAction : Notification.Action? = null
    private val userChatRoomMap = mutableMapOf<ChatRoomKey, Notification.Action>()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val commandChannel : Channel<Command> = Channel()
    private val channelFlow = commandChannel.receiveAsFlow().shareIn(scope = scope, started = WhileSubscribed())

    private val commonContents = CommonContents(commandChannel)
    private val questionGame = QuestionGame(commandChannel)
    private val timerContents = TimerContents(commandChannel)

    init{
        scope.launch {
            channelFlow.collect { command ->
                handleCommand(command)
            }
        }

    }

    suspend fun deliverNotification(chatRoomKey: ChatRoomKey, action : Notification.Action, user: Person, text : String){
        Log.i(tag, "[deliver] key : $chatRoomKey")
        Log.i(tag, "[deliver] userName : ${user.name} / text : $text")
        if(!chatRoomKey.isGroupConversation){
            userChatRoomMap[chatRoomKey] = action
        }else if(chatRoomKey == mainOpenChatRoomKey){
            mainOpenChatRoomAction = action
        }
        commonContents.request(chatRoomKey = chatRoomKey, user = user, text = text)
        questionGame.request(chatRoomKey = chatRoomKey, user = user, text = text)
        timerContents.request(chatRoomKey = chatRoomKey, user = user, text = text)
    }

    private fun handleCommand(command: Command){
        Log.i(tag, "[command] ${command.javaClass.simpleName} / mainOpenChatRoomAction : $mainOpenChatRoomAction")
        when(command){
            is GroupTextResponse -> {
                mainOpenChatRoomAction?.let { action ->
                    sendActionText(serviceContext, action, command.text)
                }
            }
            is UserTextResponse -> {
                userChatRoomMap[ChatRoomKey(isGroupConversation = false, roomName = command.userName)]?.let { action ->
                    sendActionText(serviceContext, action, command.text)
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