package me.codedbyyou.os.game.data

import androidx.compose.runtime.mutableStateListOf

object ServersViewModel {

    val servers = mutableStateListOf<Server>()

    fun upsertServer(server: Server) {
        val index = servers.indexOf(server)
        if (index >= 0) {
            servers[index] = server
        } else {
            servers.add(server)
        }
    }

}