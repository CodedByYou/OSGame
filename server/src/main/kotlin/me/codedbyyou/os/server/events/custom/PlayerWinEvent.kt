package me.codedbyyou.os.server.events.custom

import me.codedbyyou.os.core.interfaces.player.Player
import me.codedbyyou.os.server.events.interfaces.Cancellable

class PlayerWinEvent(player: Player,  name: String = "PlayerWinEvent",
                     override var isCancelled: Boolean = false
) : PlayerEvent(player, name),
Cancellable {


}