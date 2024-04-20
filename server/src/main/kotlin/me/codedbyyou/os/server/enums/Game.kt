package me.codedbyyou.os.server.enums

import me.codedbyyou.os.core.enums.RoomStatus
import me.codedbyyou.os.core.interfaces.player.Player

/**
 * interface Game, represents a game in the server.
 * a game that has rounds
 */
interface Game {

    val roomName : String
    val roomNumber : Int
    val roomDescription : String
    val roundsNumber : Int
    var roomStatus : RoomStatus
    val roomVersion : String
    val roomMaxPlayers : Int
    val roomMinPlayers : Int
    val roomPlayers : MutableList<Player>
    var roomPlayerCount : Int
    val roundResults : MutableMap<Player, MutableList<Int>>
    val roundWinners : MutableList<Player>
    val spectators : MutableList<Player>
    suspend fun start()

    fun hasStarted() : Boolean

    fun hasEnded() : Boolean

    fun end()

}