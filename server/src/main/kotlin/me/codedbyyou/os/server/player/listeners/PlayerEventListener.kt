package me.codedbyyou.os.server.player.listeners

import me.codedbyyou.os.server.Server
import me.codedbyyou.os.server.events.annotations.EventHandler
import me.codedbyyou.os.server.events.custom.PlayerChatEvent
import me.codedbyyou.os.server.events.custom.PlayerGuessEvent
import me.codedbyyou.os.server.events.interfaces.EventListener

class PlayerEventListener : EventListener {

    private val warningMap = mutableMapOf<String, Int>()
    private val censoredWords = listOf("ketchup", "no bonus", "hard exam")
    @EventHandler
    fun onPlayerGuess(event : PlayerGuessEvent) {
        Server.broadcast("Player ${event.player.uniqueName} guessed ${event.guess}")
    }

    @EventHandler
    fun onPlayerChat(event : PlayerChatEvent) {
        println("Player ${event.player.uniqueName} said: ${event.message}")
        val messageBefore = event.message
        event.message = censoredWords.fold(event.message) { acc, word ->
            acc.replace(word, "*".repeat(word.length))
        }
        if(messageBefore == event.message)
            return
        if (warningMap.getOrDefault(event.player.uniqueName, 0) >= 3) {
            event.player.sendMessage("[Server] You have been muted for spamming")
            event.isCancelled = true
        } else {
            event.player.sendMessage("[Server] Your message was censored because it contained a censored word")
            event.player.sendMessage("[Server] You have ${3 - warningMap.getOrDefault(event.player.uniqueName, 0)} warnings left")
            warningMap[event.player.uniqueName] = warningMap.getOrDefault(event.player.uniqueName, 0) + 1
        }

    }

}

