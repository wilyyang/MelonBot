package com.melon.bot.domain.intent

import android.util.Log
import androidx.core.text.isDigitsOnly
import com.melon.bot.core.common.tag
import com.melon.bot.domain.contents.Command
import com.melon.bot.domain.contents.Contents
import com.melon.bot.domain.contents.GroupTextResponse
import com.melon.bot.domain.contents.None
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerContents(override val commandChannel : Channel<Command>) : Contents {
    override val contentsName : String = "타이머"
    private var isStart = false
    private var targetMinute = 3L
    private var nickName = ""
    private val timer = Timer()

    override suspend fun request(chatRoomKey: ChatRoomKey, userName : String, text : String) {
        val textList = text.split(" ")

        if(textList[0] == "t"){
            val (tempMinute, tempName, isEndCommand) = extractInfo(textList)
            Log.i(tag, "minute : $tempMinute, name : $tempName, isEndCommand : $isEndCommand")

            val command = if(isStart){
                val minute = timer.getRemainingTime()/ 60
                val second = timer.getRemainingTime() % 60
                val info = "$nickName ${if(minute > 0) "$minute 분" else ""} $second 초 / $targetMinute 분"
                if(isEndCommand){
                    clearTimer()
                }
                GroupTextResponse(text = "${if(isEndCommand) "종료 : " else ""}$info ")
            }else if(!isEndCommand){
                startTimer(minute = tempMinute, name = tempName)
                timer.start(
                    onLastMinute = {
                        commandChannel.send(GroupTextResponse(text = "$nickName 1분 남음"))
                    }
                ) {
                    clearTimer()
                    commandChannel.send(GroupTextResponse(text = "타이머 종료"))
                }
                GroupTextResponse(text = "타이머 시작 : $targetMinute 분")
            }else{
                None
            }
            commandChannel.send(command)
        }
    }

    private fun extractInfo(textList : List<String>) : Triple<Long, String, Boolean>{
        var minute = 3L
        var name = ""

        val secondText = textList.getOrElse(1) { "" }
        val thirdText = textList.getOrElse(2) { "" }

        return if(secondText == "e"){
            Triple(minute, name, true)
        }else{
            if(secondText.isNotBlank()){
                if(secondText.isDigitsOnly()){
                    minute = secondText.toInt().toLong()
                }else{
                    name = secondText
                }
            }

            if(thirdText.isNotBlank()){
                if(thirdText.isDigitsOnly()){
                    minute = thirdText.toInt().toLong()
                }else{
                    name = thirdText
                }
            }
            Triple(minute, name, false)
        }
    }

    private fun startTimer(minute : Long, name : String){
        isStart = true
        targetMinute = minute
        nickName = name
        timer.setTimeMinute(minute)

    }

    private fun clearTimer(){
        isStart = false
        targetMinute = 3L
        nickName = ""
        timer.stop()
    }
}

class Timer {
    private var totalDuration: Long = 180L
    private val _remainingTime = MutableStateFlow(totalDuration)
    val remainingTime = _remainingTime.asStateFlow()

    private var timerJob: Job? = null

    fun start(onLastMinute: suspend () -> Unit, onFinish: suspend () -> Unit) {
        timerJob = CoroutineScope(Dispatchers.Default).launch {
            for (second in totalDuration downTo 1) {
                _remainingTime.value = second
                if(totalDuration > 60L && _remainingTime.value == 60L){
                    onLastMinute()
                }
                delay(1000)
            }
            _remainingTime.value = 0
            onFinish()
        }
    }

    fun setTimeMinute(minute : Long){
        totalDuration = minute * 60
    }
    fun getRemainingTime(): Long = _remainingTime.value

    fun stop() {
        timerJob?.cancel()
        _remainingTime.value = totalDuration
    }
}