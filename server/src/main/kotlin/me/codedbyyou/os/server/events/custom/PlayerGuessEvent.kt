package me.codedbyyou.os.server.events.custom

import me.codedbyyou.os.core.interfaces.player.Player
import me.codedbyyou.os.server.events.impl.CancellableEvent
import me.codedbyyou.os.server.events.interfaces.Cancellable

class PlayerGuessEvent(player: Player, var guess: Int) : PlayerEvent(player), Cancellable {

    var _isCancelled: Boolean = false
    override fun isCancelled(): Boolean {
        return this._isCancelled
    }

    override fun setCancelled(cancelled: Boolean) {
        this._isCancelled = cancelled
    }
    override fun getName() : String {
        return "PlayerGuessEvent"
    }
}