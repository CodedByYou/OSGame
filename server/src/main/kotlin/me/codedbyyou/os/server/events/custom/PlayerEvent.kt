package me.codedbyyou.os.server.events.custom

import me.codedbyyou.os.core.interfaces.player.Player
import me.codedbyyou.os.server.events.impl.Event

open class PlayerEvent(val player: Player) : Event() {

    override fun getName() : String {
        return "PlayerEvent"
    }

}