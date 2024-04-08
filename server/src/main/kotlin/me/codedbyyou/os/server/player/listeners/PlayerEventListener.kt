package me.codedbyyou.os.server.player.listeners

import me.codedbyyou.os.server.Server
import me.codedbyyou.os.server.events.annotations.EventHandler
import me.codedbyyou.os.server.events.custom.PlayerGuessEvent
import me.codedbyyou.os.server.events.interfaces.EventListener

class PlayerEventListener : EventListener {
    @EventHandler
    fun onPlayerGuess(event : PlayerGuessEvent) {
        Server.broadcast("Player ${event.player.uniqueName} guessed ${event.guess}")
    }

}