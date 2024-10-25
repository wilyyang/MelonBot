package com.melon.bot.domain.contents

interface Command

object None : Command
data class GroupTextResponse(val text: String) : Command
data class UserTextResponse(val userName: String, val text: String) : Command
