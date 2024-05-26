package me.codedbyyou.os.game.data

import androidx.compose.runtime.Composable

/**
 * a data class to hold the data of the navigation host
 * @param title the title of the navigation host
 * @param content the content of the navigation host
 * @author Abdollah Kandrani
 */
data class NavigationHost(
    val title: String,
    val content: @Composable (sharedData: Map<String, Any>) -> Unit = {}
)

data class Server(var name: String? = null,
                  val ip: String,
                  val port: Int,
                  var psuedoName: String? = null,
                  var ticket: String?=null,
                  var description: String?=null,
                  var status: String?=null,
                  var maxPlayers: Int?=null,
                  var onlinePlayers: Int?=null){
    override fun equals(other: Any?): Boolean {
        if (other is Server) {
            return other.ip == ip && other.port == port
        }
        return false
    }
}


data class User(
    val psuedoName: String,
    val ticket: String,
    var inputLastMoved: Long = System.currentTimeMillis()
){
    fun isAFK() = System.currentTimeMillis() - inputLastMoved > 1000 * 60 * 5

    val nickTicket
        get() = "$psuedoName#$ticket"
}