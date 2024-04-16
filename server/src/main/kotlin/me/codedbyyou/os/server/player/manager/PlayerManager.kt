package me.codedbyyou.os.server.player.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.codedbyyou.os.core.interfaces.player.Player
import me.codedbyyou.os.core.interfaces.server.Packet
import me.codedbyyou.os.server.Server
import me.codedbyyou.os.server.exceptions.TicketOutOfBoundsException
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

    @Throws(TicketOutOfBoundsException::class)
    fun registerPlayer(nickname: String, macAddress: String, ip: String, dataProcessor : (Packet) -> Unit) : String {
        val ticket = TicketManager.generateTicket(nickname)
        players["$nickname#$ticket"] =
            GamePlayer(
                nickname, ticket, true, false, false, false, false, ip,
                macAddress,
                dataProcessor
            )
        logger.info("all player names: ${players.keys}")
        logger.info("[$ip] Player $nickname registered with ticket $ticket")
        Server.config.set("players.${macAddress.replace(":","")}", "$nickname#$ticket")
        CoroutineScope(GlobalScope.coroutineContext).launch{
            Server.config.save()
        }
        return ticket
    }

    fun isValidPlayer(nickTicket: String): Boolean {
        return players.containsKey(nickTicket)
    }

    fun isMacAddressRegistered(macAddress: String): Boolean {
        return Server.config.contains("players.${macAddress.replace(":","")}")
    }

    fun doAuth(nickTicket: String, macAddress: String): Boolean {
        if (!isValidPlayer(nickTicket) && !isMacAddressRegistered(macAddress)) return false
        val player = this.players[nickTicket]
        return player != null && player.macAddress == macAddress
    }

    fun connect(nickTicket: String, ip: String, macAddress: String, dataProcessor: (Packet) -> Unit): Boolean {
        if (!doAuth(nickTicket, macAddress))
            return false
        val player = players[nickTicket]
        players[nickTicket] = GamePlayer(
            player!!.pseudoName,
            player.ticket,
            true,
            player.isPlayerBanned,
            player.isPlayerMuted,
            player.isPlayerOp,
            player.isPlayerWhitelisted,
            ip,
            macAddress,
            dataProcessor
        )
        return true
    }

    fun disconnect(nickTicket: String) {
        val player = players[nickTicket]
        players[nickTicket] = GamePlayer(
            player!!.pseudoName,
            player.ticket,
            false,
            player.isPlayerBanned,
            player.isPlayerMuted,
            player.isPlayerOp,
            player.isPlayerWhitelisted,
            player.ip,
            player.macAddress,
            { _ ->}
        )
    }

    fun getPlayerByMacAddress(macAddress: String): Player? {
        return players.values.find { it.macAddress == macAddress.replace(":", "") }
    }

    fun loadPlayer(nickname: String, ticket: String, macAddress: String) {
        players["$nickname#$ticket"] = GamePlayer(nickname,
            ticket,
            false,
            false,
            false,
            false,
            false,
            "NaN",
            macAddress
        ) { _ -> }
    }
}