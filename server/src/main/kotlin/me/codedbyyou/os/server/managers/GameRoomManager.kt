package me.codedbyyou.os.server.managers

import me.codedbyyou.os.core.enums.RoomStatus
import me.codedbyyou.os.server.enums.impl.GameRoom

class GameRoomManager {

//    HANDLE GAMEROOM IDS

    private var roomId = 0

    private val rooms: MutableMap<Int, GameRoom> = mutableMapOf()

    init{
        addRoom("Default Room", "Default Room Description", 3, "1.0", 10, 2, 0)
    }

    fun addRoom(
        roomName: String,
        roomDescription: String,
        roundsNumber: Int,
        roomVersion: String,
        roomMaxPlayers: Int,
        roomMinPlayers: Int,
        roomPlayerCount: Int
    ) {
        val roomID = ++roomId
        rooms[roomID] = GameRoom(roomName, roomID, roomDescription, roundsNumber, RoomStatus.NOT_STARTED, roomVersion, roomMaxPlayers, roomMinPlayers, mutableListOf(),roomPlayerCount)
    }


    fun removeRoom(roomNumber: Int){
        rooms.remove(roomNumber)
    }

    fun getRoom(roomNumber: Int): GameRoom? {
        return rooms[roomNumber]
    }

    fun getRooms(): List<GameRoom> {
        return rooms.values.toList()
    }

    fun getRoomCount(): Int {
        return rooms.size
    }

    fun clearRooms() {
        rooms.clear()
        addRoom("Default Room", "Default Room Description", 3, "1.0", 10, 2, 0)
    }

    fun getRoomByPlayer(uniqueName: String): GameRoom? {
        return rooms.values.find { it.roomPlayers.any { player -> player.uniqueName == uniqueName } }
    }




}