package me.codedbyyou.os.game.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

object GameViewModel {

    var gameStatus = mutableStateOf("Starting Soon")
    var gameName = mutableStateOf<String?>(null)
    var secondMessage = mutableStateOf<String?>(null)

    var canGuess = mutableStateOf(false)
    var isSpectating = mutableStateOf(false)

    var chancesLeft = mutableStateOf(-1)
    var twoThirds = mutableStateOf(-1.0)

    var gamePlayerList = mutableStateListOf<String>()

    fun resetGame() {
        gameStatus.value = "Starting Soon"
        gameName.value = null
        canGuess.value = false
        isSpectating.value = false
        secondMessage.value = null
        twoThirds.value = -1.0
        chancesLeft.value = -1
    }

}