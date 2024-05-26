package me.codedbyyou.os.game.utils

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * An object to handle logging
 * @author Abdollah Kandrani
 */
object Logger {
    val main: KLogger = KotlinLogging.logger("Main").also {
        it.info{"Logger initialized"}
    }
    val ui: KLogger = KotlinLogging.logger("UI")
    val data: KLogger = KotlinLogging.logger("Data")
    val navController: KLogger = KotlinLogging.logger("NavController")
    val audioPlayer: KLogger = KotlinLogging.logger("AudioPlayer")
    val config: KLogger = KotlinLogging.logger("Config")
    val network : KLogger = KotlinLogging.logger("Network")
    val configuration: KLogger = KotlinLogging.logger("Configuration")
}