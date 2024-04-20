package me.codedbyyou.os.server.exceptions

/**
 * TicketOutOfBoundsException.kt
 * Thrown when a ticket of certain nickname has reached the maximum limit of 9999
 * meaning that the ticket generation has failed due to running out of ticket for x name.
 * @author Abdollah Kandrani
 * @since 1.0.0
 */
class TicketOutOfBoundsException : Exception("Ticket generation failed, please try again with a different psuedo name.")