package me.codedbyyou.os.server.events.annotations

import me.codedbyyou.os.server.events.enums.EventPriority

/**
 * Helper Annotation to mark a method as an event handler in an event listener
 * @param priority the priority of the event handler
 * @see EventPriority
 * @see EventListener
 * @author abdollah kandrani
 * @version 1.0
 * @since 1.0
 * Date: 9/11/2022
 */
@Retention(AnnotationRetention.RUNTIME)
annotation class EventHandler(val priority : EventPriority = EventPriority.NORMAL)