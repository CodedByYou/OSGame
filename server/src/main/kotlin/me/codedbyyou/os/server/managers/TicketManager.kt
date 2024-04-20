package me.codedbyyou.os.server.managers
import me.codedbyyou.os.server.exceptions.TicketOutOfBoundsException
import me.codedbyyou.os.server.player.manager.PlayerManager

/**
 * TicketManager.kt
 * Generates and validates tickets for players
 * @author Abdollah Kandrani
 * @since 1.0.0
 * @see PlayerManager
 */
object TicketManager {

    @Throws(TicketOutOfBoundsException::class)
    fun generateTicket(nickname: String): String {
        var ticket = "0000"
        while(!isTicketValid(nickname, ticket)) {
            ticket = (ticket.toInt() + 1).toString().padStart(4, '0')
            if (ticket.toInt() > 9999) throw TicketOutOfBoundsException()
        }
        return ticket
    }

    private fun isTicketValid(nickname: String, ticket: String): Boolean {
        return validateTicket("$nickname#$ticket")
    }

    private fun validateTicket(nickTicket: String): Boolean {
        return PlayerManager.getPlayer(nickTicket) == null;
    }
}