package com.melon.bot.domain.intent

import com.melon.bot.domain.contents.Command
import com.melon.bot.domain.contents.Contents
import com.melon.bot.domain.contents.None
import kotlinx.coroutines.channels.Channel

class TimerContents(override val commandChannel : Channel<Command>) : Contents {
    override val contentsName : String = "타이머"

    override suspend fun request(chatRoomKey: ChatRoomKey, userName : String, text : String) {
        var command : Command = None
        commandChannel.send(command)
    }
}