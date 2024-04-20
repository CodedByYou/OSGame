package me.codedbyyou.os.core.interfaces.server

import kotlinx.serialization.Serializable
import java.io.OutputStream

/**
 * PacketType enum class
 * Shared between server and client to identify packet types, and to have a common ground for communication
 * less error-prone and more readable code, in the long run, to use enums instead of magic numbers
 * it is also easier to maintain and refactor code, it is placed in the core module to be shared between server and client
 * @param type Int the type of the packet
 * @author Abdollah Kandrani
 * @since 1.0
 * @version 1.0
 */
enum class PacketType(val type: Int) {
    /**
     * Server info packets
     */
    INFO_PING(-1), INFO_PONG(-2),
    /**
     * Client info packets
     */
    MESSAGE(type = 2),  ACTION_BAR(3), TITLE(4), KICK(5),

    /**
     * Server packets
     */
//    SERVER_CLIENT_CONNECT(6), SERVER_CLIENT_DISCONNECT(7),
    CLIENT_INFO(8),

    /**
     * 1. Server Events packets
     */
//    PLAYER_JOIN(9), not needed we have SERVER_AUTH_SUCCESS
    PLAYER_LEAVE(10),
//    PLAYER_INFO(11), not needed we have CLIENT_INFO

    /**
     * 1.a Game Events packets
     */
    GAME_START(12), GAME_ROUND_START(13), GAME_ROUND_END(14),
    GAME_END(15), ROOM_INFO(16), PLAYER_ROOM_JOIN(17),
    GAME_PLAYER_LEAVE(18), GAME_PLAYER_INFO(19), GAME_PLAYER_READY(20),
    GAME_PLAYER_GUESS(21), GAME_PLAYER_WIN(22), GAME_PLAYER_LOSE(23),
    GAMES_LIST(24), GAME_CREATE(25), GAME_JOIN(26), GAME_LEAVE(27),
    GAME_CHAT(28), GAME_CHAT_PRIVATE(29), GAME_CHAT_PUBLIC(30),

    /**
     * 2. Server Chat packets under 1.
     */
    SERVER_CHAT(31), SERVER_CHAT_PRIVATE(32), SERVER_CHAT_PUBLIC(33),

    /**
     * 3. Server Authentication packets under 1.
     * what would be the best way to handle authentication with tickets for usernames?
     * 1. Client sends a name ticket combination to the server with mac address
     * 2. Server checks if the ticket is valid and if the mac address is the same
     * 3. Server replies with a success or fail packet
     * 4. Now every packet sent by client is known on server side
     */
    SERVER_AUTH(34), SERVER_AUTH_SUCCESS(35), SERVER_AUTH_FAIL(36),

    /**
     * 4. Server Registration packets under 1.
     * what would be the best way to handle registration with tickets for usernames?
     * 1. Client sends a psuedo-name, and mac address to the server
     * 2. Server generates a ticket and sends it back to the client with a success packet
     * 2.a Server authenticates the client with the ticket at the same time
     * 3. communication is now secure and known on server side
     */
    SERVER_REGISTER(37), SERVER_REGISTER_SUCCESS(38), SERVER_REGISTER_FAIL(39),

    /**
     * 5. Server Error packets under 1.
     */
    NO_SUCH_ROOM(-100), NO_SUCH_PLAYER(-101), ROOM_FULL(-102),
    ROOM_ALREADY_STARTED(-103),
    NO_SUCH_PACKET(-999);

}

data class Packet(val packetType: PacketType) {

    val packetData = mutableMapOf<String, Any>()

    constructor(packetType: PacketType, packetData: Map<String, Any>) : this(packetType) {
        this.packetData.putAll(packetData)
    }

    constructor(packetType: PacketType, data: String) : this(packetType) {
        packetData["default"] = data
    }

    fun sendPacket(output: OutputStream) {
        val dataString = packetData.map { "${it.key}${Packet.DELLIMITER}${it.value}" }.joinToString("\n")
        val packet = "[${packetType.type}]$dataString"
        output.write(packet.toByteArray())
    }

    companion object {
        private val DELLIMITER = 'ß';
        fun fromPacket(packet: String): Packet {
            val data = packet.substring(packet.indexOf("]") + 1)
            return Packet(getPacketData(packet), getPacketDataMapped(data))
        }

        private fun getPacketData(packet: String): PacketType {
            val packetType = packet.substring(packet.indexOf("[") + 1, packet.indexOf("]")).toInt()
            return PacketType.values().find { it.type == packetType } ?: PacketType.NO_SUCH_PACKET
        }

        private fun getPacketDataMapped(packet: String): Map<String, String> {
            if (packet.isEmpty() || packet.isBlank()) return mapOf()
            return packet.split("\n").associate {
                val split = it.split(DELLIMITER)
                split[0] to split[1]
            }
        }
    }
}

fun PacketType.sendPacket(output: OutputStream) {
    val packet = Packet(this)
    packet.sendPacket(output)
}

fun PacketType.toPacket(): Packet {
    return Packet(this)
}

fun PacketType.toPacket(data: Map<String, Any>): Packet {
    return Packet(this, data)
}