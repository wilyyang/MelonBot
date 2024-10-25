package com.melon.bot.domain.contents

enum class ContentsCategory {
    Game
}

sealed class Command{
    sealed class QuestionGame {

    }
}

interface Contents{
    val hostKey : String
    val category : ContentsCategory
    val commandMap : Map<String, Command>
}

class QuestionGame : Contents{
    override val hostKey : String = "?"
    override val category: ContentsCategory = ContentsCategory.Game
    override val commandMap: Map<String, Command> = mapOf()
}