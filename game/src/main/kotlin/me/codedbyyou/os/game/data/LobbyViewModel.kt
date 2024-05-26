package me.codedbyyou.os.game.data

import androidx.compose.runtime.mutableStateListOf
import me.codedbyyou.os.core.models.GameRoomInfo

object LobbyViewModel {
    val gameRooms = mutableStateListOf<GameRoomInfo>()
    val leaderboard = mutableStateListOf<Pair<String, String>>()
}