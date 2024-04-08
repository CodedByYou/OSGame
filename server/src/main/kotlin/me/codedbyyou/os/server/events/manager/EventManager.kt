package me.codedbyyou.os.server.events.manager

import me.codedbyyou.os.server.events.impl.Event
import me.codedbyyou.os.server.events.annotations.EventHandler
import me.codedbyyou.os.server.events.interfaces.EventListener
import java.lang.reflect.InvocationTargetException

/**
 * class responsible for managing events
 * allows firing and handling of events
 * @author abdollah kandrani
 * @version 1.0
 * @since 1.0
 * Date: 9/11/2022
 */
class EventManager {

    private val listeners = mutableListOf<EventListener>()

    /**
     * registers an event listener
     * @param listener the listener to register
     */
    fun register(listener : EventListener) {
        listeners.add(listener)
    }

    /**
     * this method fires an event in all registered listeners
     * @param event the event to fire
     * @see Event
     * @see EventListener
     * @see EventHandler
     * @author abdollah kandrani
     * @version 1.0
     * @since 1.0
     * Date: 9/11/2022
     */
    fun fireEvent(event : Event) {
        for (listener in listeners) {
            for (method in listener.javaClass.declaredMethods) {
                if (!method.isAnnotationPresent(EventHandler::class.java) || method.parameterCount != 1)
                    continue
                if (method.parameterTypes[0] !is Event)
                    continue
                try {
                    method.invoke(listener, event)
                } catch (e :  IllegalAccessException) {
                    throw RuntimeException(e)
                } catch (e : InvocationTargetException) {
                    throw RuntimeException(e)
                }
            }
        }
    }

}