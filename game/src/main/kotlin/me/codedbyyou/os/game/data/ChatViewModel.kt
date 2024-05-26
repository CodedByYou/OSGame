package me.codedbyyou.os.game.data

import androidx.compose.runtime.mutableStateListOf

object ChatViewModel  {
    val chatMessages = mutableStateListOf<String>(
        "[Server] Hello! How can I help you today?",
        "[Server] I'm here to help you with any questions you have."
    )

    fun addMessage(message: String) {
        chatMessages.add(message)
    }
}
