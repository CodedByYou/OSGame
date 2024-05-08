package me.codedbyyou.os.server.player

import me.codedbyyou.os.core.interfaces.player.Player
import me.codedbyyou.os.core.interfaces.server.Packet
import me.codedbyyou.os.core.interfaces.server.sendPacket
import me.codedbyyou.os.core.interfaces.server.toPacket
import me.codedbyyou.os.server.Server
import me.codedbyyou.os.server.enums.impl.GameRoom
import me.codedbyyou.os.server.player.manager.PlayerManager
import java.io.OutputStream
import java.net.Socket
import me.codedbyyou.os.core.interfaces.server.PacketType.*
import me.codedbyyou.os.core.models.serialized
import me.codedbyyou.os.server.command.CommandManager
import me.codedbyyou.os.server.enums.impl.toGameRoomInfo
import me.codedbyyou.os.server.events.custom.PlayerChatEvent
import me.codedbyyou.os.server.events.custom.PlayerGuessEvent
import me.codedbyyou.os.server.events.custom.PlayerLoseEvent
import me.codedbyyou.os.server.events.custom.PlayerWinEvent
import me.codedbyyou.os.server.events.enums.WinLoseType
import me.codedbyyou.os.server.exceptions.TicketOutOfBoundsException
import java.util.concurrent.Executors

class GamePlayerClientHandler(val socket: Socket) : Runnable {
    private val logger = Server.logger
    private var nickname: String = ""
    private var ticket: String = ""
    private var player: Player? = null
    private var gameRoomID: Int = -1

    override fun run() {
        /**
         * task is to double-check packet processing and handling (data variable is the packet data)
         */
        logger.info("Client connected from ${socket.inetAddress.hostAddress}")
        if (socket.isConnected){
            val input = socket.getInputStream()
            val output = socket.getOutputStream()
            val buffer = ByteArray(4096)
            var read : Int
            // To know where cpu is being used most in the code, i need to use a profiler, such as visualvm
            // This note is for later
            while (input.read(buffer).also { read = it } != -1) {
                val data = String(buffer, 0, read)
                val packet = Packet.fromPacket(data)
                val packetType = packet.packetType
                val packetData = packet.packetData
                if(packetType != LEADERBOARD) {
                    logger.info("Received packet type: $packetType")
                    logger.info("Received ${packetData.size} packet data")
                }
                player?.let {
                    player -> Server.lastPinged[player.uniqueName] = System.currentTimeMillis()
                }
                    when (packetType) {
                        INFO_PING -> {
                            val dataSent = mapOf<String, Any>(
                                "ServerName" to Server.serverName,
                                "ServerIP" to Server.serverIP,
                                "ServerPort" to Server.serverPort,
                                "ServerDescription" to Server.serverDescription,
                                "ServerStatus" to Server.serverStatus,
                                "ServerVersion" to Server.serverVersion,
                                "ServerMaxPlayers" to Server.serverMaxPlayers,
                                "Online Players" to Server.getOnlinePlayerCount()
                            )
                            INFO_PONG
                                .toPacket(dataSent)
                                .sendPacket(output)
                        }

                        MESSAGE -> {
                            val message = packetData["message"].toString()
                            logger.info("Message received from ${socket.inetAddress.hostAddress} $nickname: $message")
                            // for now, I will forward chat messages through here
                            if (!message.startsWith("/")) {
                                val playerChatEvent = PlayerChatEvent(player as GamePlayer, message)
                                Server.eventsManager.fireEvent(playerChatEvent
                                ) {
                                    if (!playerChatEvent.isCancelled)
                                        PlayerManager.broadcastMessage(
                                            "[${nickname}]: ${playerChatEvent.message}",
                                            listOf(player!!.uniqueName)
                                        )
                                }
                                continue
                            }

                            var args = message.replaceFirst("/", "").split(" ").toMutableList()
                            val command = args[0]
                            args = args.subList(1, args.size)

                            for (i in args.indices) {
                                if (args[i].startsWith("\"")) {
                                    var j = i
                                    while (j < args.size && !args[j].endsWith("\"")) {
                                        j++
                                    }
                                    args[i] = args.subList(i, j + 1).joinToString(" ")
                                    args = (args.subList(0, i) + args.subList(j + 1, args.size)).toMutableList()
                                }
                            }

                            logger.info("Command: $command Args: ${args.joinToString(" ")}")
                            CommandManager.executeCommand(player as GamePlayer, command, args)

                            if (command == "broadcast") {
                                // reached here
                                logger.info("Broadcasting message to all players")
                                Server.broadcast("[Server] " + args.joinToString(" "))
                            }

                        }
                        CLIENT_INFO -> {
                            // send back client info, if is authenticated and if is authorized send back the player info
                            // basic logic for now
                            if (player != null) {
                                CLIENT_INFO
                                    .toPacket(
                                        mapOf(
                                            "Player" to player!!.toString()
                                        )
                                    )
                                    .sendPacket(output)
                            } else {
                                output.write("[${CLIENT_INFO}]".toByteArray())
                            }
                        }
                        PLAYER_LEAVE -> {
                            val message = data.substring(data.indexOf("]") + 1)
                            logger.info("Player $nickname has left the server: $message")
                            // fire room leave event if player is in a room and remove player from room
                            // fire player leave event
                            // move logic to the event
                            PlayerManager.broadcastMessage("$nickname has left the server")
                        }
//                    PacketPrefix.PLAYER_INFO ->
                        ROOM_INFO -> {
                            val message = data.substring(data.indexOf("]") + 1)
                            val roomNumber = message.toInt()
                            val room = Server.gameManager.getRoom(roomNumber)
                            if (room != null) {
                                ROOM_INFO
                                    .toPacket(
                                        mapOf(
                                            "room" to room.toGameRoomInfo()
                                        )
                                    ).sendPacket(output)
                            } else {
                                NO_SUCH_ROOM.sendPacket(output)
                            }
                        }

                        PLAYER_ROOM_JOIN -> {
                            // get game id, we already have the player id saved in this thread
                            val message = data.substring(data.indexOf("]") + 1)
                            // gameID should be the first part of the message
                            val gameID = message.substring(0, message.indexOf(" ")).toInt()

                            val room: GameRoom? = Server.gameManager.getRoom(gameID)
                            if (room != null) {
                                if (room.isFull()) {
                                    ROOM_FULL.sendPacket(output);
                                    return
                                }
                                if(!room.roomPlayers.contains(player!!))
                                    room.addPlayer(player!!)

                                gameRoomID = gameID
                                PLAYER_ROOM_JOIN
                                    .sendPacket(output)
                                val games = Server.gameManager.getRooms()
                                    .map { it.toGameRoomInfo() }
                                PlayerManager.getOnlinePlayers().forEach {
                                    Executors.newSingleThreadExecutor().submit {
                                        if (Server.gameManager.getRoomByPlayer(it.uniqueName) == null) {
                                            it as GamePlayer
                                            it.addPacket(
                                                GAMES_LIST
                                                    .toPacket(
                                                        mapOf(
                                                            "games" to games.serialized()
                                                        )
                                                    )
                                            )
                                        }
                                    }
                                }
                            } else {
                                NO_SUCH_ROOM.sendPacket(output)
                            }
                        }

                        GAME_PLAYER_LEAVE -> {
                            val message = data.substring(data.indexOf("]") + 1)
                            logger.info("Player $nickname has left the game: $message")
                            // broadcast to all players that player has left
                            // remove player from game
                            // handle game leave event
                        }

                        GAME_PLAYER_INFO -> {
                            // send back player info in the game
                        }
                        GAME_PLAYER_READY -> TODO()
                        GAME_PLAYER_GUESS -> {
                            val guess = packetData["guess"].toString().toInt()
                            val room = player?.let { Server.gameManager.getRoomByPlayer(it.uniqueName) }
                            val playerGuessEvent = PlayerGuessEvent(player as GamePlayer, guess)
                            Server.eventsManager.fireEvent(playerGuessEvent) {
                                if (room != null && player in room.roomPlayers) {
                                    room.guess(player as GamePlayer, guess)
                                }
                            }
                        }
                        LEADERBOARD -> {
                            val leaderboard = Server.getLeaderboard()
                            LEADERBOARD
                                .toPacket(
                                    mapOf(
                                        "leaderboard" to leaderboard.joinToString { "${it.first}:${it.second}" }
                                    )
                                ).sendPacket(output)
                        }
                        GAMES_LIST -> {
                            logger.info("Preparing game list")
                            val games = Server.gameManager.getRooms()
                                .map { it.toGameRoomInfo() }
                            GAMES_LIST
                                .toPacket(
                                    mapOf(
                                        "games" to games.serialized()
                                    )
                                ).sendPacket(output)
                            logger.info("Sent Game List")
                        }

                        GAME_CREATE -> TODO()
                        GAME_JOIN -> {
                            val roomNumber = packetData["room"].toString().toInt()
                            val room = Server.gameManager.getRoom(roomNumber)
                            if (room != null) {
                                if (room.isFull()) {
                                    ROOM_FULL.sendPacket(output)
                                    return
                                }
                                room.addPlayer(player!!)
                                gameRoomID = roomNumber
                                GAME_JOIN.sendPacket(output)
                            } else {
                                NO_SUCH_ROOM.sendPacket(output)
                            }
                        }
                        GAME_LEAVE -> {
                            val roomNumber = packetData["room"].toString().toInt()
                            val room = Server.gameManager.getRoom(roomNumber)
                            if (room != null) {
                                room.removePlayer(player!!)
                                gameRoomID = -1
                                GAME_LEAVE.sendPacket(output)
                            } else {
                                NO_SUCH_ROOM.sendPacket(output)
                            }
                        }
                        GAME_CHAT -> TODO()
                        GAME_CHAT_PRIVATE -> TODO()
                        GAME_CHAT_PUBLIC -> TODO()
                        SERVER_CHAT -> TODO()
                        SERVER_CHAT_PRIVATE -> TODO()
                        SERVER_CHAT_PUBLIC -> TODO()
                        SERVER_PLAYER_LIST -> {
                            val players = PlayerManager.getOnlinePlayers()
                                .map { it.uniqueName }

                            SERVER_PLAYER_LIST
                                .toPacket(
                                    mapOf(
                                        "players" to players.joinToString("+")
                                    )
                                ).sendPacket(output)
                        }
                        SERVER_AUTH -> {
                            val nickTicket = packetData["nickTicket"] as String
                            val macAddress = packetData["macAddress"] as String
                            val isMacAddressRegistered = PlayerManager.isMacAddressRegistered(macAddress)
                            val isPlayerValid = PlayerManager.isValidPlayer(nickTicket)
                            if (PlayerManager.doAuth(nickTicket, macAddress)) {
                                synchronized(PlayerManager) {
                                    player = PlayerManager.connect(
                                        nickTicket,
                                        socket.inetAddress.hostAddress.toString(),
                                        macAddress,
                                        output
                                    )
                                    this.nickname = nickTicket
                                    SERVER_AUTH_SUCCESS.sendPacket(output)
                                    PlayerManager.broadcastMessage("$nickTicket has joined the server")
                                }
                            } else {
                                SERVER_AUTH_FAIL.sendPacket(output)
                            }
                        }

                        SERVER_REGISTER -> {
                            /**
                             * 1. Client sends a psuedo-name, and mac address to the server
                             * 2. Server generates a ticket and sends it back to the client with a success packet
                             * 2.a Server authenticates the client with the ticket at the same time
                             * 3. communication is now secure and known on server side
                             */
                            val pseudoName = packetData["pseudoName"] as String
                            val macAddress = packetData["machineId"] as String
                            logger.info("Registering player $pseudoName with mac address $macAddress")
                            try {
                                synchronized(PlayerManager) {
                                    val ticket = PlayerManager.registerPlayer(
                                        pseudoName, macAddress,
                                        socket.inetAddress.hostAddress.toString(),
                                        output
                                    )

                                    player = PlayerManager.getPlayer("$pseudoName#$ticket")
                                    this.nickname = "$pseudoName#$ticket"
                                    logger.info("Sending Packet: $SERVER_REGISTER_SUCCESS")
                                    Packet(SERVER_REGISTER_SUCCESS, mapOf("ticket" to ticket))
                                        .sendPacket(output)
                                    logger.info("Connection still open: ${socket.isConnected}")
                                    logger.info("Sent Packet: $SERVER_REGISTER_SUCCESS")
                                    PlayerManager.broadcastMessage("${this.nickname} has joined the server")
                                }
                            } catch (e: TicketOutOfBoundsException) {
                                /**
                                 * Psuedo name with tickets from 0 to 9999 are already taken
                                 */
                                SERVER_REGISTER_FAIL.sendPacket(output)
                            }
                        }

                        else -> output.noSuchPacketOnServer()

                    }
                }
            }
        if (player != null) {
            Server.gameManager.getRoom(gameRoomID)?.removePlayer(player!!, false)
            PlayerManager.disconnect(player!!.uniqueName)
        }

        logger.info("Client disconnected from ${socket.inetAddress.hostAddress}") // this will never be reached
    }

    private fun OutputStream.noSuchPacketOnServer(){
        NO_SUCH_PACKET.sendPacket(this)
    }
}