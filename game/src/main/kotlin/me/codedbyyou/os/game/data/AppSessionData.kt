package me.codedbyyou.os.game.data

import androidx.compose.runtime.*

/**
 * An Object class to hold the session data of the application
 * @author Abdollah Kandrani
 * @since 0.0.1
 */
object AppSessionData {

    val showMenu: MutableState<Boolean> = mutableStateOf(false)
    val showSettings = mutableStateOf(false)
    val showTabList: MutableState<Boolean> = mutableStateOf(false)
    val registerErrorMessage = mutableStateOf<String?>(null)


    private var currentServer = true
    var exitHandle: (() -> Unit)? = null
        set(value) {
            if (exitHandle == null) {
                field = value
            }
        }


}