package me.codedbyyou.os.server.managers
import me.codedbyyou.os.server.player.manager.PlayerManager

object TicketManager {

    fun generateTicket(nickname: String): String {
        var ticket = "0000"
        while(isTicketValid(nickname, ticket)) {
            ticket = (ticket.toInt() + 1).toString().padStart(4, '0')
        }
        return "$nickname#$ticket"
    }

    private fun isTicketValid(nickname: String, ticket: String): Boolean {
        return validateTicket("$nickname#$ticket")
    }

    fun validateTicket(nickTicket: String): Boolean {
        return PlayerManager.getPlayer(nickTicket) != null;
    }
}