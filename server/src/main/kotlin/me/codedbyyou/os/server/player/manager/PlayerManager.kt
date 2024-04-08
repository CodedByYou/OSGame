package me.codedbyyou.os.server.player.manager

import me.codedbyyou.os.core.interfaces.player.Player
import me.codedbyyou.os.server.managers.TicketManager
import me.codedbyyou.os.server.player.GamePlayer
import java.util.*
import java.util.logging.Logger

object PlayerManager {

    private val logger = Logger.getLogger(PlayerManager::class.java.name)
    private val players = Collections.synchronizedMap(mutableMapOf<String, Player>())

    init {
        logger.info("PlayerManager initialized")
    }

    fun getPlayers(): List<Player> {
        return players.values.toList()
    }

    fun getPlayer(nameTicket: String): Player? {
        return players[nameTicket]
    }

    fun registerPlayer(nickname: String, macAddress: String, ip: String, dataProcessor : (String) -> Unit) : String {
        val ticket = TicketManager.generateTicket(nickname)
        players["$nickname#$ticket"] =
            GamePlayer(
                nickname, ticket, true, false, false, false, false, ip,
                dataProcessor
            )
        logger.info("[$ip] Player $nickname registered with ticket $ticket")
        return ticket
    }

    fun isValidPlayer(nickTicket: String): Boolean {
        return players.containsKey(nickTicket)
    }
}