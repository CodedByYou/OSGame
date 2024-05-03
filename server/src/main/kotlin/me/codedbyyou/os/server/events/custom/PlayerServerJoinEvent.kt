package me.codedbyyou.os.server.events.custom

import me.codedbyyou.os.core.interfaces.player.Player
import me.codedbyyou.os.server.enums.impl.GameRoom
import me.codedbyyou.os.server.events.interfaces.Cancellable

class PlayerServerJoinEvent(player: Player, room: GameRoom , name: String = "PlayerServerJoinEvent",
                            override var isCancelled: Boolean = false
) : PlayerEvent(player, name),
    Cancellable {


}