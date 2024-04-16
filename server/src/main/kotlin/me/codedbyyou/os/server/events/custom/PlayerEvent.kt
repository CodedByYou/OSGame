package me.codedbyyou.os.server.events.custom

import me.codedbyyou.os.core.interfaces.player.Player
import me.codedbyyou.os.server.events.impl.Event

/**
 * The father of all player events in the event system, the PlayerEvent class
 * @param player the player that the event is about
 * @author abdollah kandrani
 * @version 1.0
 * @since 1.0
 * Date: 4/11/2024
 */
abstract class PlayerEvent(val player: Player, name:String = "PlayerEvent") : Event(name)