package me.codedbyyou.os.client.game

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.ContextListener
import com.lehaine.littlekt.async.newSingleThreadAsyncContext
import com.lehaine.littlekt.file.vfs.readAudioClip


enum class GameState {
    MENU,
    PLAYING,
    ROUND_OVER,
    LEADERBOARD,
    GAME_OVER
}

enum class GameMode {
    SINGLE_PLAYER,
    MULTI_PLAYER
}

class Game(context: Context) : ContextListener(context) {

    var gameState = GameState.MENU
    var gameMode = GameMode.SINGLE_PLAYER

    override suspend fun Context.start() {
        // load assets

        // audio
        val audioContext = newSingleThreadAsyncContext()
        val menuSound = resourcesVfs["menu.mp3"].readAudioClip()
        val gameSound = resourcesVfs["game.mp3"].readAudioClip()

        // load config
        // load leaderboard
        // load game state
        // load game mode
        // load player state
        // load player mode
        // load player score
        // load player health
        // load player position
        // load player velocity
        // load player acceleration
        // load player rotation
        // load player scale
        // load player color
        // load player sprite
        // load player animation
        // load player frame
        // load player frame rate
        // load player frame count
        // load player frame index
        // load player frame width
        // load player frame height
        // load player frame offset
        // load player frame origin
        // load player frame scale
        // load player frame rotation
        // load player frame color
        // load player frame sprite
        // load player frame animation
        // load player frame frame
        // load player frame frame rate
        // load player frame frame count
        // load player frame frame index
        // load player frame frame width
        // load player frame frame height
        // load player frame frame offset
        // load player frame frame origin
        // load player frame frame scale
        // load player frame frame rotation
        // load player frame frame color
        // load player frame frame sprite
        // load player frame frame animation
        // load player frame frame frame
        // load player frame frame frame rate
        // load player frame frame frame count
        // load player frame frame frame index
        // load player frame frame frame width
        // load player frame frame frame height
        // load player frame frame frame offset
        // load player frame frame frame origin
        // load player frame frame frame scale
        // load player frame frame frame rotation
        // load player frame frame frame color
        // load player frame frame frame sprite
        // load player frame frame frame animation
        // load player frame frame frame frame
        // load player frame frame frame frame rate
        // load player frame frame frame frame count
        // load player frame frame frame frame index
        // load player frame frame frame frame width
        // load player frame frame frame frame height
        // load player frame frame frame frame offset
        // load player frame frame frame frame origin
        // load player frame frame frame frame scale
        // load player frame frame frame frame rotation
    }

    fun Context.render() {
        when (gameState) {
            GameState.MENU -> {
                // render menu
            }
            GameState.PLAYING -> {
                // render game
            }
            GameState.ROUND_OVER -> {
                // render round over
            }
            GameState.LEADERBOARD -> {
                // render leaderboard
            }
            GameState.GAME_OVER -> {
                // render game over
            }
        }
    }



}