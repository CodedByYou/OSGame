package me.codedbyyou.os.server.events.custom

import me.codedbyyou.os.core.interfaces.player.Player
import me.codedbyyou.os.server.events.interfaces.Cancellable

class PlayerGuessEvent(player: Player, var guess: Int) : PlayerEvent(player, "PlayerGuessEvent"), Cancellable {

    override var isCancelled: Boolean = false
        set(value) {
            field = value
        }

}