package me.codedbyyou.os.server.player

import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
import me.codedbyyou.os.core.models.GameRoomInfo
import me.codedbyyou.os.core.models.serialized
import me.codedbyyou.os.server.command.CommandManager
import me.codedbyyou.os.server.enums.impl.toGameRoomInfo
import me.codedbyyou.os.server.exceptions.TicketOutOfBoundsException
import java.util.concurrent.Executors
import kotlin.math.log

class GamePlayerClientHandler(val socket: Socket) : Runnable {
    private val logger = Server.logger
    private var nickname: String = ""
    private var ticket: String = ""
    private var player: Player? = null
    private var gameRoomID: Int = -1

    private val TEMP_ROOM_CMD_USAGE = "[Usage] room create <roomName> <maxPlayers> <rounds> <optional: description>"
    override fun run() {
        /**
         * task is to double-check packet processing and handling (data variable is the packet data)
         */
        logger.info("Client connected from ${socket.inetAddress.hostAddress}")
        if (socket.isConnected){
            val input = socket.getInputStream()
            val output = socket.getOutputStream()
            val buffer = ByteArray(1024)
            var read : Int
            // To know where cpu is being used most in the code, i need to use a profiler, such as visualvm
            // This note is for later
            while (input.read(buffer).also { read = it } != -1) {
                val data = String(buffer, 0, read)
                val packet = Packet.fromPacket(data)
                val packetType = packet.packetType
                val packetData = packet.packetData
                logger.info("Received packet type: $packetType")
                logger.info("Received ${packetData.size} packet data")

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
                        if (!message.startsWith("/"))
                            return

                        var args = message.replaceFirst("/","").split(" ").toMutableList()
                        val command = args[0]
                        args = args.subList(1, args.size)

                        for (i in args.indices){
                            if (args[i].startsWith("\"")){
                                var j = i
                                while (j < args.size && !args[j].endsWith("\"")){
                                    j++
                                }
                                args[i] = args.subList(i, j+1).joinToString(" ")
                                args = (args.subList(0, i) + args.subList(j+1, args.size)).toMutableList()
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
                        if (player != null){
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
                        // fire player leave event // move logic to the event
                        PlayerManager.getPlayers().forEach { it.sendMessage("$nickname has left the server") }
                    }
//                    PacketPrefix.PLAYER_INFO ->
                    ROOM_INFO -> {
                        // send back game info
                    }
                    PLAYER_ROOM_JOIN -> {
                        // get game id, we already have the player id saved in this thread
                        val message = data.substring(data.indexOf("]") + 1)
                        // gameID should be the first part of the message
                        val gameID = message.substring(0, message.indexOf(" ")).toInt()

                        val room: GameRoom? = Server.gameManager.getRoom(gameID)
                        if (room != null) {
                            if (room.isFull()){
                                ROOM_FULL.sendPacket(output);
                                return
                            }

                            room.addPlayer(player!!)
                            gameRoomID = gameID
                            // sending back same packet prefix as success
                            output.write("[${PLAYER_ROOM_JOIN}]".toByteArray())
                            PLAYER_ROOM_JOIN
                                .sendPacket(output)
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
                    GAME_PLAYER_GUESS -> TODO()
                    GAME_PLAYER_WIN -> TODO()
                    GAME_PLAYER_LOSE -> TODO()
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
                    GAME_JOIN -> TODO()
                    GAME_LEAVE -> TODO()
                    GAME_CHAT -> TODO()
                    GAME_CHAT_PRIVATE -> TODO()
                    GAME_CHAT_PUBLIC -> TODO()
                    SERVER_CHAT -> TODO()
                    SERVER_CHAT_PRIVATE -> TODO()
                    SERVER_CHAT_PUBLIC -> TODO()
                    SERVER_AUTH -> {
                        val nickTicket = packetData["nickTicket"] as String
                        val macAddress = packetData["macAddress"] as String
                        if (PlayerManager.doAuth(nickTicket, macAddress)){
                            player = PlayerManager.connect(
                                nickTicket,
                                socket.inetAddress.hostAddress.toString(),
                                macAddress,
                                output
                            )
                            SERVER_AUTH_SUCCESS.sendPacket(output)
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
                            val ticket = PlayerManager.registerPlayer(
                                pseudoName, macAddress,
                                socket.inetAddress.hostAddress.toString(),
                                output
                            )

                            player = PlayerManager.getPlayer("$pseudoName#$ticket")
                            println("Player: $player")
                            logger.info("Sending Packet: $SERVER_REGISTER_SUCCESS")
                            Packet(SERVER_REGISTER_SUCCESS, mapOf("ticket" to ticket))
                                .sendPacket(output)
                            logger.info("Connection still open: ${socket.isConnected}")
                            logger.info("Sent Packet: $SERVER_REGISTER_SUCCESS")
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
        logger.info("Client disconnected from ${socket.inetAddress.hostAddress}") // this will never be reached
    }

    private fun OutputStream.noSuchPacketOnServer(){
        NO_SUCH_PACKET.sendPacket(this)
    }
}