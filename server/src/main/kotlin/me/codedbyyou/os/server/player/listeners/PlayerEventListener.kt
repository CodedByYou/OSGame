package me.codedbyyou.os.server.player.listeners

import me.codedbyyou.os.server.Server
import me.codedbyyou.os.server.events.annotations.EventHandler
import me.codedbyyou.os.server.events.custom.PlayerChatEvent
import me.codedbyyou.os.server.events.custom.PlayerGuessEvent
import me.codedbyyou.os.server.events.interfaces.EventListener

class PlayerEventListener : EventListener {

    private val censoredWords = listOf("ketchup", "no bonus", "hard exam")
    @EventHandler
    fun onPlayerGuess(event : PlayerGuessEvent) {
        Server.broadcast("Player ${event.player.uniqueName} guessed ${event.guess}")
    }
    @EventHandler
    fun onPlayerChat(event : PlayerChatEvent) {
        event.message = censoredWords.fold(event.message) { acc, word ->
            acc.replace(word, "*".repeat(word.length))
        }
    }

}

