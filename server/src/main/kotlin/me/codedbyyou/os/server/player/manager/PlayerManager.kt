package me.codedbyyou.os.server.player.manager

import com.sun.security.ntlm.Client
import kotlinx.coroutines.*
import me.codedbyyou.os.core.interfaces.player.Player
import me.codedbyyou.os.core.interfaces.server.Packet
import me.codedbyyou.os.core.interfaces.server.PacketType
import me.codedbyyou.os.server.Server
import me.codedbyyou.os.server.exceptions.TicketOutOfBoundsException
import me.codedbyyou.os.server.managers.TicketManager
import me.codedbyyou.os.server.player.GamePlayer
import java.io.OutputStream
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.logging.Logger

/**
 * PlayerManager.kt
 * Manages all players in the server, including their data and packets
 * @author Abdollah Kandrani
 * @since 1.0.0
 * @see TicketManager
 */
object PlayerManager {

    private val logger = Logger.getLogger(PlayerManager::class.java.name)
    private val players = Collections.synchronizedMap(mutableMapOf<String, Player>())
    private val playerPackets = ConcurrentLinkedQueue<Pair<Player, Packet>>()
    private val playerDataProcessors = mutableMapOf<String, OutputStream>()
    private val playerProcessRoutine = CoroutineScope(GlobalScope.coroutineContext)

    /**
     * Initializes the player manager
     */
    init {
        logger.info("PlayerManager initialized")

        /**
         * Launches a coroutine to process player packets
         */
        playerProcessRoutine.launch {
            withContext(newSingleThreadContext("PlayerPacketProcessor")) {
                while (true) {
                    while (playerPackets.isNotEmpty()) {
                        val (player, packet) = playerPackets.poll()
                        playerDataProcessors[player.uniqueName].let {
                            packet.sendPacket(it!!)
                            if (packet.packetType != PacketType.LEADERBOARD)
                                logger.info("Packet [${packet.packetType}] sent to ${player.uniqueName}")
                        }
                    }
                    delay(100)
                }
            }
        }.start()
    }

    /**
     * Gets all players
     * @return A list of all players
     * @see GamePlayer
     */
    fun getPlayers(): List<Player> {
        return players.values.toList()
    }

    /**
     * Gets a player by their ticket
     * @param nameTicket The player's unique name & ticket
     * @return The player with the given ticket
     * @see GamePlayer
     */
    fun getPlayer(nameTicket: String): Player? {
        return players[nameTicket]
    }

    /**
     * Registers a player to the server
     * @param nickname The player's nickname
     * @param macAddress The player's MAC address
     * @param ip The player's IP address
     * @param outputStream The player's output stream
     * @return The player's ticket
     * @throws TicketOutOfBoundsException If all tickets are used
     * @see GamePlayer
     * @see TicketManager
     */
    @Throws(TicketOutOfBoundsException::class)
    fun registerPlayer(nickname: String, macAddress: String, ip: String, outputStream: OutputStream) : String {
        val ticket = TicketManager.generateTicket(nickname)
        players["$nickname#$ticket"] =
            GamePlayer(
                pseudoName = nickname,
                ticket = ticket,
                isOnline = true,
                isPlayerBanned = false,
                isPlayerMuted = false,
                isPlayerOp = false,
                isPlayerWhitelisted = false,
                ip = ip,
                macAddress = macAddress
            )
        playerDataProcessors["$nickname#$ticket"] = outputStream
        logger.info("[$ip] Player $nickname registered with ticket $ticket")
        Server.config.set("players.${macAddress.replace(":","")}", "$nickname#$ticket")
        CoroutineScope(GlobalScope.coroutineContext)
            .launch{
                Server.config.save()
            }
        return ticket
    }

    /**
     * Checks if a player is valid by their ticket
     * @param nickTicket The player's ticket
     * @return Whether the player is valid
     * @see GamePlayer
     */
    fun isValidPlayer(nickTicket: String): Boolean {
        return players.containsKey(nickTicket)
    }

    /**
     * Checks if a player is registered by their MAC address
     * @param macAddress The player's MAC address
     * @return Whether the player is registered
     * @see GamePlayer
     * @see Server
     */
    fun isMacAddressRegistered(macAddress: String): Boolean {
        return Server.config.contains("players.${macAddress.replace(":","")}")
    }

    /**
     * Authenticates a player
     * @param nickTicket The player's ticket
     * @param macAddress The player's MAC address
     * @return Whether the player is authenticated
     * @see GamePlayer
     */
    fun doAuth(nickTicket: String, macAddress: String): Boolean {
        if (!isValidPlayer(nickTicket) && !isMacAddressRegistered(macAddress)) return false
        val player = this.players[nickTicket]
        return player != null && player.macAddress == macAddress
    }

    /**
     * Connects a player to the server
     * @param nickTicket The player's ticket
     * @param ip The player's IP address
     * @param macAddress The player's MAC address
     * @param outputStream The player's output stream
     * @return The player that was connected
     * @see GamePlayer
     * @see Packet
     */
    fun connect(nickTicket: String, ip: String, macAddress: String, outputStream: OutputStream): Player {
        val player = players[nickTicket]
                players[nickTicket] = GamePlayer(
            pseudoName = player!!.pseudoName,
            ticket = player.ticket,
            isOnline = true,
            isPlayerBanned = player.isPlayerBanned,
            isPlayerMuted = player.isPlayerMuted,
            isPlayerOp = player.isPlayerOp,
            isPlayerWhitelisted = player.isPlayerWhitelisted,
            ip = ip,
            macAddress = macAddress
        )
        Server.lastPinged[nickTicket] = System.currentTimeMillis()
        playerDataProcessors[nickTicket] = outputStream
        return player
    }

    /**
     * Disconnects a player from the server
     * @param nickTicket The player's ticket
     * @see GamePlayer
     * @see Packet
     * @see Player
     */
    fun disconnect(nickTicket: String) {
        if (playerDataProcessors.containsKey(nickTicket)) {
            val outputStream = playerDataProcessors[nickTicket]!!
            outputStream.close()
            playerDataProcessors.remove(nickTicket)
        }
        val player = players[nickTicket]
        players[nickTicket] = GamePlayer(
            pseudoName =  player!!.pseudoName,
            ticket = player.ticket,
            isOnline = false,
            isPlayerBanned = player.isPlayerBanned,
            isPlayerMuted = player.isPlayerMuted,
            isPlayerOp = player.isPlayerOp,
            isPlayerWhitelisted = player.isPlayerWhitelisted,
            ip = player.ip,
            macAddress = player.macAddress
        )

    }

    /**
     * Gets a player by their MAC address
     * @param macAddress The player's MAC address
     * @return The player with the given MAC address
     * @see GamePlayer
     */
    fun getPlayerByMacAddress(macAddress: String): Player? {
        return players.values.find { it.macAddress == macAddress.replace(":", "") }
    }

    /**
     * Get Online Players
     * @return A list of all online players
     */
    fun getOnlinePlayers(): List<Player> = players.values.filter { it.isOnline }

    /**
     * Get Offline Players
     * @return A list of all offline players
     */
    fun getOfflinePlayers(): List<Player> = players.values.filter { !it.isOnline }


    /**
     * Loads a player into the player manager from the server's configuration file
     * @param nickname The player's nickname
     * @param ticket The player's ticket
     * @param macAddress The player's MAC address
     * @see GamePlayer
     */
    fun loadPlayer(nickname: String, ticket: String, macAddress: String) {
        players["$nickname#$ticket"] = GamePlayer(nickname,
            ticket = ticket,
            isOnline = false,
            isPlayerBanned = false,
            isPlayerMuted = false,
            isPlayerOp = false,
            isPlayerWhitelisted = false,
            ip = "NaN",
            macAddress = macAddress
        )
    }

    /**
     * Adds a packet to a player's packet queue
     * @param nickTicket The player's ticket
     * @param packet The packet to add
     * @see Packet
     * @see GamePlayer
     */
    fun addPacket(nickTicket: String, packet: Packet) {
        players[nickTicket]?.let {
            playerPackets.add(Pair(it, packet))
        }
    }

    /**
     * Broadcasts a packet to all players
     * @param packet The packet to broadcast
     * @param exceptions A list of players to exclude from the broadcast
     */
    fun broadcast(packet: Packet, exceptions: List<String>) {
        players.values.forEach {
            if (it.isOnline && !exceptions.contains(it.uniqueName)) {
                addPacket(it.uniqueName, packet)
            }
        }
    }


    /**
     * Broadcasts a message to all players, including the console (can be excluded)
     * @param message The message to broadcast
     * @param exceptions A list of players to exclude from the broadcast
     * @param permission The permission required to receive the message
     * @param includeConsole Whether to include the console in the broadcast
     */
    fun broadcastMessage(message: String, exceptions: List<String> = listOf(), permission: String = "", includeConsole : Boolean = true) {

        players.values.forEach { player ->
            player as GamePlayer
            if (player.isOnline && !exceptions.contains(player.uniqueName) &&
                (permission == "" || player.hasPermission(permission))) {
                player.sendMessage(message)
            }
        }

        if (includeConsole)
            Server.consoleCommandSender.sendMessage(message)
    }
}