package me.codedbyyou.os.core.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import me.codedbyyou.os.core.enums.RoomStatus

/**
 * data class GameRoom
 * Represents a game room in the server, with all the necessary information to be displayed to the client.
 */
@Serializable()
data class GameRoomInfo(
    val roomName: String,
    val roomNumber: Int,
    val roomDescription: String,
    val roundsNumber: Int,
    val roomStatus: RoomStatus,
    val roomMaxPlayers: Int,
    val roomPlayerCount: Int,
    val players : List<String>
)

fun GameRoomInfo.serialized() : String {
    return "$roomName#$roomNumber#$roomDescription#$roundsNumber#$roomStatus#$roomMaxPlayers#$roomPlayerCount#${players.joinToString("*")}"
}

fun Array<GameRoomInfo>.serialized() : String {
    return this.joinToString("|") { it.serialized() }
}

fun List<GameRoomInfo>.serialized() : String {
    return this.joinToString("|") { it.serialized() }
}

fun String.deserialized() : Array<GameRoomInfo> {
    return this.split("|").map {
        val parts = it.split("#")
        GameRoomInfo(parts[0], parts[1].toInt(), parts[2], parts[3].toInt(), RoomStatus.valueOf(parts[4]), parts[5].toInt(), parts[6].toInt(), parts[7].split("*"))
    }.toTypedArray()
}

fun String.deserializedSingle() : GameRoomInfo {
    val parts = this.split("#")
    return GameRoomInfo(parts[0], parts[1].toInt(), parts[2], parts[3].toInt(), RoomStatus.valueOf(parts[4]), parts[5].toInt(), parts[6].toInt(), parts[7].split("*"))
}