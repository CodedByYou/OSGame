package me.codedbyyou.os.server.events.custom

import me.codedbyyou.os.core.interfaces.player.Player
import me.codedbyyou.os.server.events.interfaces.Cancellable

class PlayerChatEvent(player: Player, var message: String, name: String = "PlayerChatEvent",
                      override var isCancelled: Boolean = false
) : PlayerEvent(player, name),
    Cancellable {

}

//TODO player guess event + cancelable
//TODO player server join event + cancelable
//TODO player server leave event + cancelable
//TODO player game join event + cancelable
//TODO player game leave event + cancelable
//TODO player win event (player, winType.round || game )+ cancelable
//TODO player lose event (player, winType.round || game ) + cancelable
//TODO do the enums (add them to the enum folder)
//