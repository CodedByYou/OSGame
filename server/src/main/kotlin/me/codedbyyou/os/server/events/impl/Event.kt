package me.codedbyyou.os.server.events.impl

import me.codedbyyou.os.server.events.interfaces.Cancellable

/**
 * The father of all events in the event system, the Event class
 * @author abdollah kandrani 202104093
 * @version 1.0
 * @since 1.0
 * Date: 9/11/2022
 */
abstract class Event {

    /**
     * @return the name of the event
     */
    open fun getName() : String {
        return this.javaClass.simpleName
    }

    /**
     * @return whether the event is cancellable or not
     */
    fun isCancellable() : Boolean { return this is Cancellable
    }
}