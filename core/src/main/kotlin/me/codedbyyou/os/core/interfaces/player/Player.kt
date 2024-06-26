package me.codedbyyou.os.core.interfaces.player

interface Player {
    val macAddress: String;
    val pseudoName : String
    val ticket : String
    val isOnline : Boolean
    val isPlayerBanned : Boolean
    val isPlayerMuted : Boolean
    val isPlayerOp : Boolean
    val isPlayerWhitelisted : Boolean
    val ip : String

    fun sendMessage(message: String)

    fun sendTitle(title: String, subtitle: String, duration: Float = 1f)

    fun sendActionBar(message: String)

    fun kick(reason: String, disconnect: Boolean = true)

    val uniqueName : String
        get() = "$pseudoName#$ticket"

    fun sendSound(sound: String)
}