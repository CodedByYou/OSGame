package me.codedbyyou.os.server.events.custom

import me.codedbyyou.os.core.interfaces.player.Player
import me.codedbyyou.os.server.events.enums.WinLoseType
import me.codedbyyou.os.server.events.interfaces.Cancellable

class PlayerLoseEvent(player: Player, val loseType: WinLoseType, name: String = "PlayerLoseEvent",
                      override var isCancelled: Boolean = false
) : PlayerEvent(player, name),
    Cancellable {

}
