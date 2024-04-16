package me.codedbyyou.os.client.game.manager

import com.lehaine.littlekt.async.KtScope
import com.lehaine.littlekt.async.newSingleThreadAsyncContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.codedbyyou.os.client.game.runtime.client.Client
import me.codedbyyou.os.client.ui.dialog.Server
import me.codedbyyou.os.core.interfaces.server.Packet
import me.codedbyyou.os.core.interfaces.server.PacketType.*
import me.codedbyyou.os.core.interfaces.server.sendPacket
import java.io.OutputStream
import java.net.Socket
import java.util.logging.Logger

class ConnectionManager {
    private val logger = Logger.getLogger("ConnectionManager")
    private var server : Server? = null
    private var connection: Socket? = null
    private var connectionJob: Job? = null
    private var output: OutputStream? = null
    val connectedToIP : String?
        get() = server?.ip

    val channel: Channel<Packet> = Channel()

    fun connectTo(_server: Server) : ConnectionStatus{
        if (server != null && server != _server) {
            logger.info("Disconnecting from server ${server!!.ip}")
            disconnect()
        }
        if (server == _server && connection != null && connection!!.isConnected) {
            logger.info("Already connected to server ${server!!.ip}")
            return ConnectionStatus.ALREADY_CONNECTED
        }
        server = _server
        logger.info("Connecting to server ${_server.ip}")
        connectionJob = GlobalScope.launch {
            connection = Socket(_server.ip, _server.port)
            handleConnection(connection!!)
        }
        logger.info("Connected to server ${_server.ip}")
        return ConnectionStatus.CONNECTED
    }

    fun disconnect() {
        connectionJob?.cancel()
        connection?.close()
        logger.info("Disconnected from server ${server!!.ip}")
        server = null
    }

    private fun handleConnection(socket: Socket) {
        val input = socket.getInputStream()
        output = socket.getOutputStream()
        val channelExecutor = newSingleThreadAsyncContext()
        val buffer = ByteArray(1024)
        var read : Int
        try {
            while (input.read(buffer).also { read = it } != -1) {
                var data = String(buffer, 0, read)
                val packet = Packet.fromPacket(data)
                val packetType = packet.packetType
                val packetData = packet.packetData
                KtScope.launch {
                    withContext(channelExecutor){
                        logger.info("Received packet type: $packetType")
                        logger.info("Received ${packetData.size} packet data")
                    }
                }

                when (packetType) {
                    INFO_PING -> {
                        if (Client.user != null && !Client.user!!.isAFK()){
                            INFO_PONG.sendPacket(output!!)
                        }
                    }
                    INFO_PONG -> {
                        KtScope.launch {
                            withContext(channelExecutor){
                                logger.info("Server information received")
                            }
                        }
                    }
                    MESSAGE -> {
                        KtScope.launch {
                            withContext(channelExecutor){
                                logger.info("Message received: ${packetData["message"]}")
                                // TODO: Handle Messages in another QUEUE Thread, for chat etc.
                                // as for now, just logging is fine
                            }
                        }
                    }

                    ACTION_BAR -> TODO("Implement Action Bar Messages in the Game")
                    TITLE -> TODO("Implement Title Messages in the Game")
                    KICK -> TODO("Implement Kick UI in the Client")
                    CLIENT_INFO -> TODO()
                    PLAYER_LEAVE -> TODO("Implement Player Leave in the Game")
                    GAME_START -> TODO()
                    GAME_ROUND_START -> TODO()
                    GAME_ROUND_END -> TODO()
                    GAME_END -> TODO()
                    ROOM_INFO -> TODO()
                    PLAYER_ROOM_JOIN -> TODO()
                    GAME_PLAYER_LEAVE -> TODO()
                    GAME_PLAYER_INFO -> TODO()
                    GAME_PLAYER_READY -> TODO()
                    GAME_PLAYER_GUESS -> TODO()
                    GAME_PLAYER_WIN -> TODO()
                    GAME_PLAYER_LOSE -> TODO()
                    GAMES_LIST -> TODO()
                    GAME_CREATE -> TODO()
                    GAME_JOIN -> TODO()
                    GAME_LEAVE -> TODO()
                    GAME_CHAT -> TODO()
                    GAME_CHAT_PRIVATE -> TODO()
                    GAME_CHAT_PUBLIC -> TODO()
                    SERVER_CHAT -> TODO()
                    SERVER_CHAT_PRIVATE -> TODO()
                    SERVER_CHAT_PUBLIC -> TODO()
                    SERVER_AUTH -> {}
                    SERVER_AUTH_SUCCESS -> {
                        KtScope.launch {
                            withContext(channelExecutor) {
                                logger.info("Authenticated with server ${server?.ip}")
                                channel.send(packet)
                            }
                        }
                    }
                    SERVER_AUTH_FAIL -> {
                        KtScope.launch {
                            withContext(channelExecutor) {
                                logger.warning("Failed to authenticate with server ${server?.ip}")
                                channel.send(packet)
                            }
                        }
                    }
                    SERVER_REGISTER -> TODO()
                    SERVER_REGISTER_SUCCESS -> {
                        KtScope.launch {
                            withContext(channelExecutor) {
                                logger.info("Registered with server ${server?.ip}")
                                channel.send(packet)
                            }
                        }
                    }
                    SERVER_REGISTER_FAIL -> {

                        KtScope.launch {
                            withContext(channelExecutor) {
                                logger.warning("Failed to register with server ${server?.ip}")
                                channel.send(packet)
                            }
                        }
                    }
                    NO_SUCH_ROOM -> TODO()
                    NO_SUCH_PLAYER -> TODO()
                    ROOM_FULL -> TODO()
                    ROOM_ALREADY_STARTED -> TODO()
                    NO_SUCH_PACKET -> TODO()
                }

            }
        } catch (e: Exception) {
            logger.info("Server disconnected ${server?.ip}")
        }
    }

    fun sendPacket(packet: Packet) {
        packet.sendPacket(output!!)
    }

    fun isConnected(): Boolean {
        return connection != null && connection!!.isConnected
    }

    enum class ConnectionStatus {
        CONNECTED,
        ALREADY_CONNECTED,
        DISCONNECTED
    }
}