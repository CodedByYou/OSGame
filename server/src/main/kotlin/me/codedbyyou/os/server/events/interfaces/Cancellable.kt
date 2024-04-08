package me.codedbyyou.os.server.events.interfaces

/**
 * self explanatory
 * @author abdollah kandrani
 * @version 1.0
 * @since 1.0
 * Date: 9/11/2022
 */
interface Cancellable {

    fun setCancelled(cancelled : Boolean)

    fun isCancelled() : Boolean
}