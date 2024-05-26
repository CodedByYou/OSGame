package me.codedbyyou.os.game.data

import androidx.compose.runtime.mutableStateOf

object DialogViewModel {
    var showDialog =  mutableStateOf(false)
    var leaderboard = mutableStateOf(listOf<Pair<String, String>>())
}
