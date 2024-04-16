package me.codedbyyou.os.server.events.impl

import me.codedbyyou.os.server.events.impl.Event
import me.codedbyyou.os.server.events.interfaces.Cancellable

/**
 * Cancellable events that implements the Cancellable interface with a controller variable to check if the event is cancelled
 * @param name the name of the event
 * @author abdollah kandrani
 * @version 1.0
 * @since 1.0
 * Date: 9/11/2022
 */
abstract class CancellableEvent(name: String) : Event(name), Cancellable {

    override var isCancelled: Boolean = false
        set(value) {
            // REDUNDANT IN THIS CASE BUT
            // ADD SOME LOGIC HERE WHEN OVERRIDING
            field = value
        }

}